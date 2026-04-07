package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.TopicAddReq;
import com.gdplatform.dto.TopicResp;
import com.gdplatform.dto.TopicUpdateReq;
import com.gdplatform.service.ProjectTopicService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/project/topics")
@RequiredArgsConstructor
public class ProjectTopicController {

    private final ProjectTopicService projectTopicService;

    @GetMapping
    @PreAuthorize("hasAuthority('project:topic:list')")
    public R<Page<TopicResp>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return R.ok(projectTopicService.pageTopics(current, size, campusId, campusName, academicYear, status, keyword));
    }

    @GetMapping("/{topicId}")
    @PreAuthorize("hasAuthority('project:topic:list')")
    public R<TopicResp> getById(@PathVariable Long topicId) {
        return R.ok(projectTopicService.getById(topicId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('project:topic:add')")
    public R<Void> add(@Valid @RequestBody TopicAddReq req) {
        projectTopicService.add(req);
        return R.ok();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('project:topic:edit')")
    public R<Void> update(@Valid @RequestBody TopicUpdateReq req) {
        projectTopicService.update(req);
        return R.ok();
    }

    @DeleteMapping("/{topicId}")
    @PreAuthorize("hasAuthority('project:topic:del')")
    public R<Void> delete(@PathVariable Long topicId) {
        projectTopicService.delete(topicId);
        return R.ok();
    }

    @PutMapping("/toggle-status/{topicId}")
    @PreAuthorize("hasAuthority('project:topic:edit')")
    public R<Void> toggleStatus(@PathVariable Long topicId) {
        projectTopicService.toggleStatus(topicId);
        return R.ok();
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('project:topic:list')")
    public void export(
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            HttpServletResponse response) throws IOException {
        List<TopicResp> list = projectTopicService.export(campusId, campusName, academicYear, status, keyword);
        exportExcel(list, response);
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('project:topic:add')")
    public R<Void> importTopics(@RequestParam("file") MultipartFile file) {
        projectTopicService.importTopics(file);
        return R.ok();
    }

    private void exportExcel(List<TopicResp> list, HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("课题列表");

            // 表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(headerStyle);

            // 数据样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(dataStyle);

            String[] headers = {"课题名称", "学年", "最大人数", "已选人数", "状态", "简介", "学校名称", "创建时间"};
            int[] widths = {4000, 2000, 1500, 1500, 1500, 5000, 3000, 3000};

            // 写表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, widths[i]);
            }

            // 写数据
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (int i = 0; i < list.size(); i++) {
                TopicResp topic = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(topic.getTopicName() != null ? topic.getTopicName() : "");
                row.createCell(1).setCellValue(topic.getAcademicYear() != null ? topic.getAcademicYear() : "");
                row.createCell(2).setCellValue(topic.getMaxStudents() != null ? topic.getMaxStudents() : 0);
                row.createCell(3).setCellValue(topic.getCurrentCount() != null ? topic.getCurrentCount() : 0);
                row.createCell(4).setCellValue(statusLabel(topic.getStatus()));
                row.createCell(5).setCellValue(topic.getDescription() != null ? topic.getDescription() : "");
                row.createCell(6).setCellValue(topic.getCampusName() != null ? topic.getCampusName() : "");
                row.createCell(7).setCellValue(topic.getCreateTime() != null ? topic.getCreateTime().format(fmt) : "");
                for (int j = 0; j < 8; j++) {
                    row.getCell(j).setCellStyle(dataStyle);
                }
            }

            String filename = "课题列表_" + System.currentTimeMillis() + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            workbook.write(response.getOutputStream());
        }
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private String statusLabel(String status) {
        if (status == null) return "—";
        return switch (status.toUpperCase()) {
            case "OPEN" -> "已发布";
            case "CLOSED" -> "已禁用";
            case "DRAFT" -> "草稿";
            default -> status;
        };
    }
}
