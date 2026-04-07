package com.gdplatform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.BizException;
import com.gdplatform.dto.TopicAddReq;
import com.gdplatform.dto.TopicResp;
import com.gdplatform.dto.TopicUpdateReq;
import com.gdplatform.entity.ProjectTopic;
import com.gdplatform.entity.SysUser;
import com.gdplatform.mapper.ProjectTopicMapper;
import com.gdplatform.mapper.SysUserMapper;
import com.gdplatform.service.ProjectTopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectTopicServiceImpl implements ProjectTopicService {

    private final ProjectTopicMapper projectTopicMapper;
    private final SysUserMapper sysUserMapper;

    @Value("${gd.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public Page<TopicResp> pageTopics(long current, long size, String academicYear, String status, String keyword) {
        Page<TopicResp> page = new Page<>(current, size);
        long offset = (current - 1) * size;
        List<TopicResp> records = projectTopicMapper.selectTopicPage(academicYear, status, keyword, offset, size);
        long total = projectTopicMapper.countTopicPage(academicYear, status, keyword);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public TopicResp getById(Long topicId) {
        ProjectTopic topic = projectTopicMapper.selectById(topicId);
        if (topic == null) {
            throw new BizException("课题不存在");
        }
        return toResp(topic);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(TopicAddReq req) {
        ProjectTopic topic = new ProjectTopic();
        topic.setTopicName(req.getTopicName());
        if (req.getTeacherId() != null) {
            topic.setTeacherId(req.getTeacherId());
        }
        topic.setAcademicYear(req.getAcademicYear());
        topic.setMaxStudents(req.getMaxStudents());
        topic.setDescription(req.getDescription());
        topic.setCurrentCount(0);
        topic.setStatus("OPEN");
        projectTopicMapper.insert(topic);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TopicUpdateReq req) {
        ProjectTopic topic = projectTopicMapper.selectById(req.getTopicId());
        if (topic == null) {
            throw new BizException("课题不存在");
        }

        topic.setTopicName(req.getTopicName());
        topic.setTeacherId(req.getTeacherId());
        topic.setAcademicYear(req.getAcademicYear());
        topic.setMaxStudents(req.getMaxStudents());
        topic.setDescription(req.getDescription());
        projectTopicMapper.updateById(topic);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long topicId) {
        ProjectTopic topic = projectTopicMapper.selectById(topicId);
        if (topic == null) {
            throw new BizException("课题不存在");
        }
        projectTopicMapper.deleteById(topicId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long topicId) {
        ProjectTopic topic = projectTopicMapper.selectById(topicId);
        if (topic == null) {
            throw new BizException("课题不存在");
        }
        String newStatus = "OPEN".equalsIgnoreCase(topic.getStatus()) ? "CLOSED" : "OPEN";
        topic.setStatus(newStatus);
        projectTopicMapper.updateById(topic);
    }

    @Override
    public List<TopicResp> export(String academicYear, String status, String keyword) {
        return projectTopicMapper.selectAllForExport(academicYear, status, keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTopics(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请上传文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BizException("仅支持 .xlsx 或 .xls 格式");
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2) {
                throw new BizException("文件为空或无数据行");
            }

            // 先收集所有 teacherId -> real_name 映射
            Map<String, Long> teacherNameToId = buildTeacherNameToIdMap();

            // 收集 Excel 中重复的课题名称
            Map<String, Integer> excelSeen = new HashMap<>();
            List<String> excelDuplicates = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null || isRowEmpty(r)) continue;
                String name = getCellString(r.getCell(0));
                if (!StringUtils.hasText(name)) continue;
                excelSeen.put(name.trim(), excelSeen.getOrDefault(name.trim(), 0) + 1);
            }
            for (Map.Entry<String, Integer> e : excelSeen.entrySet()) {
                if (e.getValue() > 1) {
                    excelDuplicates.add(e.getKey());
                }
            }

            // 收集数据库中已有的课题名称
            List<ProjectTopic> existingTopics = projectTopicMapper.selectList(
                    Wrappers.<ProjectTopic>lambdaQuery()
                            .select(ProjectTopic::getTopicName));
            Map<String, Long> dbTopicNameToId = new HashMap<>();
            for (ProjectTopic t : existingTopics) {
                if (StringUtils.hasText(t.getTopicName())) {
                    dbTopicNameToId.put(t.getTopicName().trim(), t.getTopicId());
                }
            }

            int successCount = 0;
            int failCount = 0;
            int skipCount = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                try {
                    String topicName = getCellString(row.getCell(0));
                    String teacherName = getCellString(row.getCell(1));
                    String academicYear = getCellString(row.getCell(2));
                    String maxStudentsStr = getCellString(row.getCell(3));
                    String description = getCellString(row.getCell(4));

                    if (!StringUtils.hasText(topicName)) {
                        errors.add("第" + (i + 1) + "行：课题名称为空");
                        failCount++;
                        continue;
                    }
                    if (excelDuplicates.contains(topicName.trim())) {
                        errors.add("第" + (i + 1) + "行：课题名称「" + topicName + "」在文件中重复");
                        failCount++;
                        continue;
                    }
                    if (dbTopicNameToId.containsKey(topicName.trim())) {
                        skipCount++;
                        continue;
                    }
                    if (!StringUtils.hasText(academicYear)) {
                        errors.add("第" + (i + 1) + "行：学年开始为空");
                        failCount++;
                        continue;
                    }

                    Long teacherId = null;
                    if (StringUtils.hasText(teacherName)) {
                        teacherId = teacherNameToId.get(teacherName.trim());
                    }

                    int maxStudents = 1;
                    if (StringUtils.hasText(maxStudentsStr)) {
                        try {
                            maxStudents = Integer.parseInt(maxStudentsStr.trim());
                        } catch (NumberFormatException e) {
                            maxStudents = 1;
                        }
                    }

                    ProjectTopic topic = new ProjectTopic();
                    topic.setTopicName(topicName.trim());
                    topic.setTeacherId(teacherId);
                    topic.setAcademicYear(academicYear.trim());
                    topic.setMaxStudents(maxStudents);
                    topic.setCurrentCount(0);
                    topic.setStatus("OPEN");
                    topic.setDescription(StringUtils.hasText(description) ? description.trim() : null);
                    projectTopicMapper.insert(topic);
                    successCount++;
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行处理异常：" + e.getMessage());
                    failCount++;
                }
            }

            if (failCount > 0) {
                log.warn("批量导入课题：成功{}条，跳过（已存在）{}条，失败{}条，错误：{}", successCount, skipCount, failCount, errors);
                throw new BizException("导入完成：成功" + successCount + "条，跳过（已存在）" + skipCount + "条，失败" + failCount + "条。失败行：" + String.join("；", errors));
            }
        } catch (IOException e) {
            throw new BizException("文件读取失败：" + e.getMessage());
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < 5; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && StringUtils.hasText(getCellString(cell))) {
                return false;
            }
        }
        return true;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private Map<String, Long> buildTeacherNameToIdMap() {
        Map<String, Long> map = new HashMap<>();
        List<SysUser> teachers = sysUserMapper.selectList(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getUserType, 2)
                        .eq(SysUser::getIsDeleted, 0));
        for (SysUser t : teachers) {
            if (StringUtils.hasText(t.getRealName())) {
                map.put(t.getRealName().trim(), t.getUserId());
            }
        }
        return map;
    }

    private TopicResp toResp(ProjectTopic topic) {
        TopicResp resp = new TopicResp();
        resp.setTopicId(topic.getTopicId());
        resp.setTopicName(topic.getTopicName());
        resp.setAcademicYear(topic.getAcademicYear());
        resp.setMaxStudents(topic.getMaxStudents());
        resp.setCurrentCount(topic.getCurrentCount());
        resp.setStatus(topic.getStatus());
        resp.setDescription(topic.getDescription());
        resp.setCreateTime(topic.getCreateTime());
        resp.setUpdateTime(topic.getUpdateTime());
        return resp;
    }
}
