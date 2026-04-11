package com.gdplatform.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdplatform.entity.SysOperationLog;
import com.gdplatform.service.SysOperationLogService;
import com.gdplatform.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 操作日志切面，自动记录所有 Controller 的操作行为。
 * <p>
 * 被 @OperationLog 注解标记的方法会记录详细信息；
 * 其他 Controller 方法仅记录基本信息。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogService logService;
    private final ObjectMapper objectMapper;

    /** 跳过登录认证相关的路径（避免记录登录日志造成循环） */
    private static final Pattern SKIP_PATH = Pattern.compile(
            "^/api/auth/(login|register|logout)$|^/api/auth/info$"
    );

    @Pointcut("execution(* com.gdplatform.controller..*.*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        HttpServletRequest request = getRequest();
        if (request == null) {
            return point.proceed();
        }

        String path = request.getRequestURI();
        if (SKIP_PATH.matcher(path).matches()) {
            return point.proceed();
        }

        SysOperationLog logEntity = buildBaseLog(request);

        Object result = null;
        Throwable thrown = null;
        try {
            result = point.proceed();
        } catch (Throwable t) {
            thrown = t;
        } finally {
            long cost = System.currentTimeMillis() - start;

            // 填充切面信息
            enrichFromJoinPoint(logEntity, point);
            enrichFromResult(logEntity, result, thrown);
            logEntity.setDuration(cost);
            logEntity.setOperateTime(LocalDateTime.now());

            // 异步保存日志
            saveLogAsync(logEntity);
        }

        if (thrown != null) {
            throw thrown;
        }
        return result;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private SysOperationLog buildBaseLog(HttpServletRequest request) {
        SysOperationLog entity = new SysOperationLog();
        entity.setRequestMethod(request.getMethod());
        entity.setRequestUrl(request.getRequestURI());
        entity.setIpAddress(getClientIp(request));
        entity.setIpLocation("");
        entity.setOs(parseOs(request.getHeader("User-Agent")));
        entity.setBrowser(parseBrowser(request.getHeader("User-Agent")));

        try {
            var user = SecurityUtils.currentUser();
            entity.setUserId(user.getUserId());
            entity.setUserName(user.getRealName());
            entity.setUserType(user.getUserType());
            entity.setCampusName(user.getCampusName());
        } catch (Exception e) {
            // 未登录请求
        }

        // 从 URL 推断模块
        String module = inferModule(request.getRequestURI());
        entity.setModule(module);

        return entity;
    }

    private void enrichFromJoinPoint(SysOperationLog entity, ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 操作类型
        String type = inferOperationType(entity.getRequestMethod(), entity.getRequestUrl(), method);
        entity.setOperationType(type);

        // 操作名称
        String name = inferOperationName(method);
        entity.setOperationName(name);

        // 请求参数
        try {
            Map<String, Object> paramMap = buildParamMap(point, method);
            if (!paramMap.isEmpty()) {
                entity.setRequestParams(objectMapper.writeValueAsString(paramMap));
            }
        } catch (Exception e) {
            entity.setRequestParams("");
        }
    }

    private void enrichFromResult(SysOperationLog entity, Object result, Throwable thrown) {
        if (thrown != null) {
            entity.setStatus("FAIL");
            entity.setErrorMsg(truncate(thrown.getMessage(), 500));
            return;
        }

        entity.setStatus("SUCCESS");

        try {
            String respJson = objectMapper.writeValueAsString(result);
            entity.setResponseResult(truncate(respJson, 500));
        } catch (Exception ignored) {
        }
    }

    private Map<String, Object> buildParamMap(ProceedingJoinPoint point, Method method) {
        Object[] args = point.getArgs();
        Parameter[] params = method.getParameters();
        java.util.LinkedHashMap<String, Object> map = new java.util.LinkedHashMap<>();

        // 排除 request/response/session 等不可序列化参数
        Set<Class<?>> skipTypes = Set.of(
                HttpServletRequest.class,
                jakarta.servlet.http.HttpServletResponse.class,
                jakarta.servlet.http.HttpSession.class
        );

        for (int i = 0; i < params.length; i++) {
            if (i < args.length && args[i] != null && !skipTypes.contains(args[i].getClass())
                    && !args[i].getClass().getName().startsWith("org.springframework")) {
                String key = params[i].getName();
                Object val = sanitizeValue(args[i]);
                map.put(key, val);
            }
        }
        return map;
    }

    private Object sanitizeValue(Object val) {
        if (val == null) return null;
        String cls = val.getClass().getName();
        // 跳过文件上传、byte[] 等大对象
        if (cls.startsWith("org.springframework.web.multipart")
                || cls.equals("byte[]")
                || cls.contains("InputStream")) {
            return "[binary data]";
        }
        // 过滤敏感字段
        if (val instanceof java.util.Map m) {
            m.remove("password");
            m.remove("oldPassword");
            m.remove("newPassword");
            m.remove("token");
            m.remove("secret");
            return m;
        }
        if (val instanceof String s) {
            return s.length() > 200 ? s.substring(0, 200) + "..." : s;
        }
        return val;
    }

    private String inferOperationType(String method, String url, Method javaMethod) {
        String upper = method.toUpperCase();
        if (upper.equals("POST")) {
            if (url.contains("/login") || url.contains("/auth")) return "LOGIN";
            if (url.contains("/logout")) return "LOGOUT";
            return "CREATE";
        }
        if (upper.equals("PUT") || upper.equals("PATCH")) return "UPDATE";
        if (upper.equals("DELETE")) return "DELETE";
        if (upper.equals("GET")) {
            if (url.contains("/export") || url.contains("/download")) return "EXPORT";
            if (url.contains("/page") || url.contains("/list") || url.contains("/detail")) return "QUERY";
        }
        return "OTHER";
    }

    private String inferOperationName(Method method) {
        String name = method.getName();
        // 方法名中文映射
        if (name.contains("Login")) return "登录系统";
        if (name.contains("Logout")) return "退出登录";
        if (name.contains("Register")) return "用户注册";
        if (name.contains("Add") || name.contains("Create") || name.contains("Save")) return "新增数据";
        if (name.contains("Update") || name.contains("Edit") || name.contains("Put") || name.contains("Save")) return "更新数据";
        if (name.contains("Delete") || name.contains("Remove")) return "删除数据";
        if (name.contains("Export") || name.contains("Download")) return "导出数据";
        if (name.contains("Page") || name.contains("List") || name.contains("Query")) return "查询数据";
        if (name.contains("Submit")) return "提交操作";
        if (name.contains("Approve") || name.contains("Pass")) return "审批通过";
        if (name.contains("Reject")) return "审批拒绝";
        if (name.contains("Publish")) return "发布操作";
        if (name.contains("Assign")) return "分配操作";
        return "其他操作";
    }

    private String inferModule(String url) {
        // /api/xxx/yyy => 模块名为 xxx
        String[] parts = url.split("/");
        for (String p : parts) {
            if (!p.equals("api") && !p.isEmpty()) {
                return p;
            }
        }
        return "未知模块";
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };
        for (String h : headers) {
            String ip = request.getHeader(h);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private String parseOs(String ua) {
        if (ua == null) return "Unknown";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac OS") || ua.contains("Macintosh")) return "macOS";
        if (ua.contains("Linux")) return "Linux";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        return "Unknown";
    }

    private String parseBrowser(String ua) {
        if (ua == null) return "Unknown";
        if (ua.contains("Chrome") && !ua.contains("Edg")) return "Chrome";
        if (ua.contains("Edg")) return "Edge";
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("Safari") && !ua.contains("Chrome")) return "Safari";
        if (ua.contains("IE") || ua.contains("Trident")) return "IE";
        return "Unknown";
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) : s;
    }

    @Async
    public void saveLogAsync(SysOperationLog entity) {
        try {
            logService.saveLog(entity);
        } catch (Exception e) {
            log.error("保存操作日志失败: {}", e.getMessage());
        }
    }
}
