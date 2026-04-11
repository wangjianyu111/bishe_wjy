package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.OperationLogReq;
import com.gdplatform.dto.OperationLogResp;
import com.gdplatform.service.SysOperationLogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/system/operation-log")
@RequiredArgsConstructor
public class SysOperationLogController {

    private final SysOperationLogService logService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('sysops:log')")
    public R<Page<OperationLogResp>> page(OperationLogReq req) {
        return R.ok(logService.pageForAdmin(req));
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('sysops:log')")
    public R<Void> deleteBatch(@RequestBody Long[] ids) {
        logService.deleteBatch(ids);
        return R.ok();
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('sysops:log')")
    public void export(OperationLogReq req, HttpServletResponse response) throws IOException {
        req.setSize(10000);
        var page = logService.pageForAdmin(req);
        List<OperationLogResp> records = page.getRecords();

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("操作日志");
        CellStyle headerStyle = wb.createCellStyle();
        Font boldFont = wb.createFont();
        boldFont.setBold(true);
        headerStyle.setFont(boldFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        String[] headers = {"操作时间", "操作人", "身份", "学校", "操作名称", "操作类型",
                "模块", "请求方法", "请求路径", "IP地址", "操作系统", "浏览器", "耗时(ms)", "状态", "错误信息"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (int i = 0; i < records.size(); i++) {
            OperationLogResp r = records.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(r.getOperateTime() != null ? r.getOperateTime() : "");
            row.createCell(1).setCellValue(r.getUserName() != null ? r.getUserName() : "");
            row.createCell(2).setCellValue(userTypeLabel(r.getUserType()));
            row.createCell(3).setCellValue(r.getCampusName() != null ? r.getCampusName() : "");
            row.createCell(4).setCellValue(r.getOperationName() != null ? r.getOperationName() : "");
            row.createCell(5).setCellValue(r.getOperationType() != null ? r.getOperationType() : "");
            row.createCell(6).setCellValue(r.getModule() != null ? r.getModule() : "");
            row.createCell(7).setCellValue(r.getRequestMethod() != null ? r.getRequestMethod() : "");
            row.createCell(8).setCellValue(r.getRequestUrl() != null ? r.getRequestUrl() : "");
            row.createCell(9).setCellValue(r.getIpAddress() != null ? r.getIpAddress() : "");
            row.createCell(10).setCellValue(r.getOs() != null ? r.getOs() : "");
            row.createCell(11).setCellValue(r.getBrowser() != null ? r.getBrowser() : "");
            row.createCell(12).setCellValue(r.getDuration() != null ? r.getDuration() : 0);
            row.createCell(13).setCellValue("SUCCESS".equals(r.getStatus()) ? "成功" : "失败");
            row.createCell(14).setCellValue(r.getErrorMsg() != null ? r.getErrorMsg() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        String filename = "操作日志_" + java.time.LocalDate.now() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        wb.write(response.getOutputStream());
        wb.close();
    }

    private String userTypeLabel(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "学生";
            case 2 -> "教师";
            case 3 -> "管理员";
            default -> "未知";
        };
    }
}
