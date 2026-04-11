package com.gdplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdplatform.common.R;
import com.gdplatform.dto.CampusThresholdReq;
import com.gdplatform.dto.ExcellentAchievementReq;
import com.gdplatform.dto.ExcellentAchievementResp;
import com.gdplatform.dto.GradeOptionResp;
import com.gdplatform.entity.CampusThreshold;
import com.gdplatform.service.ExcellentAchievementService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/api/achievement/excellent")
@RequiredArgsConstructor
public class ExcellentAchievementController {

    private final ExcellentAchievementService excellentAchievementService;

    // ==================== 优秀成果列表 ====================

    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<Page<ExcellentAchievementResp>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword) {
        return R.ok(excellentAchievementService.adminPage(current, size, campusName, academicYear, keyword));
    }

    @GetMapping("/teacher/page")
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<Page<ExcellentAchievementResp>> teacherPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String keyword) {
        return R.ok(excellentAchievementService.teacherPage(current, size, campusName, academicYear, keyword));
    }

    @GetMapping("/student/page")
    @PreAuthorize("hasAuthority('achievement:grade:view')")
    public R<Page<ExcellentAchievementResp>> studentPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return R.ok(excellentAchievementService.studentPage(current, size));
    }

    @GetMapping("/student/search")
    @PreAuthorize("hasAuthority('achievement:grade:view')")
    public R<Page<ExcellentAchievementResp>> studentSearch(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return R.ok(excellentAchievementService.studentSelfPage(current, size, keyword));
    }

    // ==================== 认定操作 ====================

    @PostMapping
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<Void> approve(@Valid @RequestBody ExcellentAchievementReq req) {
        excellentAchievementService.approve(req);
        return R.ok();
    }

    @DeleteMapping("/{excellentId}")
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<Void> revoke(@PathVariable Long excellentId) {
        excellentAchievementService.revoke(excellentId);
        return R.ok();
    }

    // ==================== 阈值管理 ====================

    @GetMapping("/threshold/list")
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<List<CampusThreshold>> listThresholds(
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear) {
        return R.ok(excellentAchievementService.listThresholds(campusName, academicYear));
    }

    @PostMapping("/threshold")
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<Void> saveThreshold(@Valid @RequestBody CampusThresholdReq req) {
        excellentAchievementService.saveThreshold(req);
        return R.ok();
    }

    @DeleteMapping("/threshold/{thresholdId}")
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<Void> deleteThreshold(@PathVariable Long thresholdId) {
        excellentAchievementService.deleteThreshold(thresholdId);
        return R.ok();
    }

    // ==================== 候选成绩 ====================

    @GetMapping("/qualified")
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public R<List<GradeOptionResp>> listQualifiedGrades(
            @RequestParam(required = false) String campusName) {
        return R.ok(excellentAchievementService.listQualifiedGrades(campusName));
    }

    // ==================== 导出 ====================

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('achievement:grade:manage')")
    public void export(
            @RequestParam(required = false) String campusName,
            @RequestParam(required = false) String academicYear,
            HttpServletResponse response) throws IOException {
        List<ExcellentAchievementResp> list = excellentAchievementService.getExportData(campusName, academicYear);
        exportExcel(list, response);
    }

    private void exportExcel(List<ExcellentAchievementResp> list, HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("优秀成果名单");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(headerStyle);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(dataStyle);

            String[] headers = {
                    "序号", "学校", "学年", "学生姓名", "学号", "课题名称",
                    "指导教师", "最终成绩", "成绩等级",
                    "平时均分", "论文均分", "答辩均分", "参与评分教师数",
                    "优秀分数线", "认定人", "认定时间", "备注"
            };
            int[] widths = {600, 2000, 1500, 1500, 2000, 4000, 1500, 1200, 1200, 1200, 1200, 1200, 1800, 1500, 1500, 2500, 3000};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, widths[i]);
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (int i = 0; i < list.size(); i++) {
                ExcellentAchievementResp item = list.get(i);
                Row row = sheet.createRow(i + 1);
                int col = 0;
                row.createCell(col++).setCellValue(i + 1);
                row.createCell(col++).setCellValue(item.getCampusName() != null ? item.getCampusName() : "");
                row.createCell(col++).setCellValue(item.getAcademicYear() != null ? item.getAcademicYear() : "");
                row.createCell(col++).setCellValue(item.getStudentName() != null ? item.getStudentName() : "");
                row.createCell(col++).setCellValue(item.getStudentNo() != null ? item.getStudentNo() : "");
                String topicName = item.getIsCustomTopic() != null && item.getIsCustomTopic() == 1
                        ? (item.getCustomTopicName() != null ? item.getCustomTopicName() : "")
                        : (item.getTopicName() != null ? item.getTopicName() : "");
                row.createCell(col++).setCellValue(topicName);
                row.createCell(col++).setCellValue(item.getTeacherName() != null ? item.getTeacherName() : "");
                row.createCell(col++).setCellValue(item.getFinalScore() != null ? item.getFinalScore().doubleValue() : 0.0);
                row.createCell(col++).setCellValue(item.getFinalGradeLevel() != null ? item.getFinalGradeLevel() : "");
                row.createCell(col++).setCellValue(item.getAvgRegularScore() != null ? item.getAvgRegularScore().doubleValue() : 0.0);
                row.createCell(col++).setCellValue(item.getAvgThesisScore() != null ? item.getAvgThesisScore().doubleValue() : 0.0);
                row.createCell(col++).setCellValue(item.getAvgDefenseScore() != null ? item.getAvgDefenseScore().doubleValue() : 0.0);
                row.createCell(col++).setCellValue(item.getEvaluatorCount() != null ? item.getEvaluatorCount() : 0);
                row.createCell(col++).setCellValue(item.getCampusThreshold() != null ? item.getCampusThreshold().doubleValue() : 0.0);
                row.createCell(col++).setCellValue(item.getApproverName() != null ? item.getApproverName() : "");
                row.createCell(col++).setCellValue(item.getApproveTime() != null ? item.getApproveTime().format(fmt) : "");
                row.createCell(col++).setCellValue(item.getRemark() != null ? item.getRemark() : "");

                for (int j = 0; j < col; j++) {
                    row.getCell(j).setCellStyle(dataStyle);
                }
            }

            String filename = "优秀成果名单_" + System.currentTimeMillis() + ".xlsx";
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
}
