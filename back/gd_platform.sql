/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3307
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : localhost:3307
 Source Schema         : gd_platform

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 12/04/2026 01:12:46
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for achievement_submission
-- ----------------------------
DROP TABLE IF EXISTS `achievement_submission`;
CREATE TABLE `achievement_submission`  (
  `ach_sub_id` bigint NOT NULL AUTO_INCREMENT,
  `selection_id` bigint NOT NULL,
  `submit_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CODE代码 THESIS终稿 OTHER',
  `file_id` bigint NULL DEFAULT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'SUBMITTED',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ach_sub_id`) USING BTREE,
  INDEX `idx_achsel_selection`(`selection_id` ASC) USING BTREE,
  INDEX `fk_achsel_file`(`file_id` ASC) USING BTREE,
  CONSTRAINT `fk_achsel_file` FOREIGN KEY (`file_id`) REFERENCES `doc_file` (`file_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_achsel_selection` FOREIGN KEY (`selection_id`) REFERENCES `project_selection` (`selection_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成果提交' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of achievement_submission
-- ----------------------------

-- ----------------------------
-- Table structure for achievement_submit
-- ----------------------------
DROP TABLE IF EXISTS `achievement_submit`;
CREATE TABLE `achievement_submit`  (
  `submit_id` bigint NOT NULL AUTO_INCREMENT COMMENT '提交ID',
  `selection_id` bigint NOT NULL COMMENT '选题ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `file_id` bigint NULL DEFAULT NULL COMMENT '附件ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`submit_id`) USING BTREE,
  INDEX `idx_selection`(`selection_id` ASC) USING BTREE,
  INDEX `idx_student`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成果提交表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of achievement_submit
-- ----------------------------

-- ----------------------------
-- Table structure for approval_opinion
-- ----------------------------
DROP TABLE IF EXISTS `approval_opinion`;
CREATE TABLE `approval_opinion`  (
  `opinion_id` bigint NOT NULL AUTO_INCREMENT,
  `selection_id` bigint NOT NULL COMMENT '关联选题ID',
  `doc_id` bigint NULL DEFAULT NULL COMMENT '关联文档ID（可选）',
  `doc_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文档类型：PROPOSAL/MIDTERM/THESIS/ARCHIVE',
  `round_no` int NOT NULL DEFAULT 1 COMMENT '评审轮次',
  `reviewer_id` bigint NOT NULL COMMENT '评审教师ID',
  `score` int NULL DEFAULT NULL COMMENT '评分 0-100',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文字批注/意见',
  `file_id` bigint NULL DEFAULT NULL COMMENT '附件ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED待处理 PASSED通过 FAILED驳回',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`opinion_id`) USING BTREE,
  INDEX `idx_opinion_selection`(`selection_id` ASC) USING BTREE,
  INDEX `idx_opinion_reviewer`(`reviewer_id` ASC) USING BTREE,
  INDEX `idx_opinion_doc`(`doc_type` ASC) USING BTREE,
  CONSTRAINT `fk_opinion_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_opinion_selection` FOREIGN KEY (`selection_id`) REFERENCES `project_selection` (`selection_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批意见管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of approval_opinion
-- ----------------------------

-- ----------------------------
-- Table structure for approval_record
-- ----------------------------
DROP TABLE IF EXISTS `approval_record`;
CREATE TABLE `approval_record`  (
  `approval_id` bigint NOT NULL AUTO_INCREMENT,
  `biz_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SELECTION选题 PROPOSAL开题 MID中期 THESIS论文等',
  `biz_id` bigint NOT NULL,
  `approver_id` bigint NOT NULL,
  `action` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'APPROVE REJECT',
  `comment_text` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approval_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`approval_id`) USING BTREE,
  INDEX `idx_appr_biz`(`biz_type` ASC, `biz_id` ASC) USING BTREE,
  INDEX `fk_appr_user`(`approver_id` ASC) USING BTREE,
  CONSTRAINT `fk_appr_user` FOREIGN KEY (`approver_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批意见(通用)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of approval_record
-- ----------------------------

-- ----------------------------
-- Table structure for archive_material
-- ----------------------------
DROP TABLE IF EXISTS `archive_material`;
CREATE TABLE `archive_material`  (
  `archive_id` bigint NOT NULL AUTO_INCREMENT,
  `selection_id` bigint NOT NULL,
  `file_id` bigint NULL DEFAULT NULL,
  `archive_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '归档编号',
  `archived` tinyint NULL DEFAULT 0 COMMENT '1已归档',
  `archive_time` datetime NULL DEFAULT NULL,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`archive_id`) USING BTREE,
  UNIQUE INDEX `uk_archive_selection`(`selection_id` ASC) USING BTREE,
  INDEX `fk_arch_file`(`file_id` ASC) USING BTREE,
  CONSTRAINT `fk_arch_file` FOREIGN KEY (`file_id`) REFERENCES `doc_file` (`file_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_arch_selection` FOREIGN KEY (`selection_id`) REFERENCES `project_selection` (`selection_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '归档材料' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of archive_material
-- ----------------------------

-- ----------------------------
-- Table structure for campus_threshold
-- ----------------------------
DROP TABLE IF EXISTS `campus_threshold`;
CREATE TABLE `campus_threshold`  (
  `threshold_id` bigint NOT NULL AUTO_INCREMENT,
  `campus_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学校名称',
  `academic_year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学年',
  `excellent_score` decimal(5, 2) NOT NULL COMMENT '优秀分数线（成绩总分必须大于此分数才算优秀）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`threshold_id`) USING BTREE,
  UNIQUE INDEX `uk_campus_year`(`campus_name` ASC, `academic_year` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学校优秀分数线配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of campus_threshold
-- ----------------------------

-- ----------------------------
-- Table structure for defense_arrangement
-- ----------------------------
DROP TABLE IF EXISTS `defense_arrangement`;
CREATE TABLE `defense_arrangement`  (
  `arrange_id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '场次ID',
  `selection_id` bigint NOT NULL COMMENT '选题ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `teacher_id` bigint NULL DEFAULT NULL COMMENT '指导教师ID',
  `sort_order` int NULL DEFAULT 0 COMMENT '答辩顺序',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'SCHEDULED' COMMENT '状态：SCHEDULED待答辩 DONE已完成',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`arrange_id`) USING BTREE,
  UNIQUE INDEX `uk_defense_session_sel`(`session_id` ASC, `selection_id` ASC) USING BTREE,
  INDEX `idx_student`(`student_id` ASC) USING BTREE,
  INDEX `fk_def_sel`(`selection_id` ASC) USING BTREE,
  CONSTRAINT `fk_def_sel` FOREIGN KEY (`selection_id`) REFERENCES `project_selection` (`selection_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_def_sess` FOREIGN KEY (`session_id`) REFERENCES `defense_session` (`session_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '答辩安排' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of defense_arrangement
-- ----------------------------

-- ----------------------------
-- Table structure for defense_group
-- ----------------------------
DROP TABLE IF EXISTS `defense_group`;
CREATE TABLE `defense_group`  (
  `group_id` bigint NOT NULL AUTO_INCREMENT COMMENT '小组ID',
  `group_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '小组名称',
  `leader_id` bigint NOT NULL COMMENT '组长ID',
  `leader_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组长姓名',
  `campus_id` bigint NULL DEFAULT NULL COMMENT '学校ID',
  `campus_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学校名称',
  `academic_year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学年',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE有效 DISSOLVED已解散',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_by_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`group_id`) USING BTREE,
  INDEX `idx_dg_leader`(`leader_id` ASC) USING BTREE,
  INDEX `idx_dg_campus`(`campus_id` ASC) USING BTREE,
  INDEX `idx_dg_year`(`academic_year` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '答辩小组表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of defense_group
-- ----------------------------

-- ----------------------------
-- Table structure for defense_group_member
-- ----------------------------
DROP TABLE IF EXISTS `defense_group_member`;
CREATE TABLE `defense_group_member`  (
  `member_id` bigint NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `group_id` bigint NOT NULL COMMENT '小组ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'TEACHER教师 STUDENT学生',
  `campus_id` bigint NULL DEFAULT NULL COMMENT '学校ID',
  `campus_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学校名称',
  `create_by` bigint NULL DEFAULT NULL COMMENT '添加人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`member_id`) USING BTREE,
  INDEX `idx_dgm_group`(`group_id` ASC) USING BTREE,
  INDEX `idx_dgm_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_dgm_type`(`user_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '答辩小组成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of defense_group_member
-- ----------------------------

-- ----------------------------
-- Table structure for defense_session
-- ----------------------------
DROP TABLE IF EXISTS `defense_session`;
CREATE TABLE `defense_session`  (
  `session_id` bigint NOT NULL AUTO_INCREMENT,
  `session_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '场次名称',
  `defense_date` date NOT NULL COMMENT '答辩日期',
  `start_time` time NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` time NULL DEFAULT NULL COMMENT '结束时间',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '答辩地点',
  `defense_form` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '答辩形式：ONLINE线上 OFFLINE线下',
  `academic_year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学年',
  `file_id` bigint NULL DEFAULT NULL COMMENT '附件ID',
  `teacher_id` bigint NULL DEFAULT NULL COMMENT '发布教师ID',
  `campus_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属学校',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`session_id`) USING BTREE,
  INDEX `idx_teacher`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_campus`(`campus_name` ASC) USING BTREE,
  INDEX `idx_date`(`defense_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '答辩场次' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of defense_session
-- ----------------------------

-- ----------------------------
-- Table structure for doc_archive
-- ----------------------------
DROP TABLE IF EXISTS `doc_archive`;
CREATE TABLE `doc_archive`  (
  `archive_id` bigint NOT NULL AUTO_INCREMENT COMMENT '归档ID',
  `selection_id` bigint NOT NULL COMMENT '选题ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `stage_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '归档阶段名称',
  `file_id` bigint NULL DEFAULT NULL COMMENT '附件ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态：SUBMITTED待审核 PASSED已通过 FAILED已驳回',
  `reviewer_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `reviewer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核人姓名',
  `review_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核意见',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`archive_id`) USING BTREE,
  INDEX `idx_selection`(`selection_id` ASC) USING BTREE,
  INDEX `idx_student`(`student_id` ASC) USING BTREE,
  INDEX `idx_stage`(`stage_name` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文档归档表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doc_archive
-- ----------------------------

-- ----------------------------
-- Table structure for doc_file
-- ----------------------------
DROP TABLE IF EXISTS `doc_file`;
CREATE TABLE `doc_file`  (
  `file_id` bigint NOT NULL AUTO_INCREMENT,
  `original_name` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `stored_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储文件名',
  `file_path` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '相对或绝对路径',
  `file_size` bigint NULL DEFAULT 0,
  `mime_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `uploader_id` bigint NOT NULL,
  `biz_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型',
  `biz_id` bigint NULL DEFAULT NULL,
  `selection_id` bigint NULL DEFAULT NULL,
  `version_no` int NOT NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`file_id`) USING BTREE,
  INDEX `idx_doc_selection`(`selection_id` ASC) USING BTREE,
  INDEX `idx_doc_biz`(`biz_type` ASC, `biz_id` ASC) USING BTREE,
  INDEX `fk_doc_uploader`(`uploader_id` ASC) USING BTREE,
  CONSTRAINT `fk_doc_selection` FOREIGN KEY (`selection_id`) REFERENCES `project_selection` (`selection_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_doc_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文档文件' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doc_file
-- ----------------------------

-- ----------------------------
-- Table structure for doc_template
-- ----------------------------
DROP TABLE IF EXISTS `doc_template`;
CREATE TABLE `doc_template`  (
  `template_id` bigint NOT NULL AUTO_INCREMENT,
  `template_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `biz_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'PROPOSAL开题 THESIS论文 MID中期 OTHER',
  `file_id` bigint NULL DEFAULT NULL COMMENT '模板文件',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`template_id`) USING BTREE,
  INDEX `fk_tpl_file`(`file_id` ASC) USING BTREE,
  CONSTRAINT `fk_tpl_file` FOREIGN KEY (`file_id`) REFERENCES `doc_file` (`file_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模板文件' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doc_template
-- ----------------------------

-- ----------------------------
-- Table structure for doc_version
-- ----------------------------
DROP TABLE IF EXISTS `doc_version`;
CREATE TABLE `doc_version`  (
  `version_id` bigint NOT NULL AUTO_INCREMENT,
  `selection_id` bigint NOT NULL COMMENT '选题ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `stage_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '毕设阶段名称（如：开题报告、中期检查、论文定稿）',
  `version_no` int NOT NULL DEFAULT 1 COMMENT '版本号',
  `file_id` bigint NULL DEFAULT NULL COMMENT '附件ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态：SUBMITTED已提交/PASSED通过/FAILED驳回',
  `reviewer_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `review_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核意见',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`version_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文档版本管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doc_version
-- ----------------------------
INSERT INTO `doc_version` VALUES (1, 1, 3, '开题报告', 1, NULL, '测试版本', 'SUBMITTED', NULL, NULL, NULL, '2026-04-12 01:04:50', '2026-04-12 01:04:50', 0);

-- ----------------------------
-- Table structure for excellent_achievement
-- ----------------------------
DROP TABLE IF EXISTS `excellent_achievement`;
CREATE TABLE `excellent_achievement`  (
  `excellent_id` bigint NOT NULL AUTO_INCREMENT,
  `grade_summary_id` bigint NOT NULL COMMENT '关联的成绩汇总ID',
  `selection_id` bigint NOT NULL COMMENT '选题ID（冗余）',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `final_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '最终认定总分',
  `final_grade_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最终认定等级（固定为优秀）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'APPROVED' COMMENT '状态：PENDING/APPROVED/REJECTED',
  `approver_id` bigint NULL DEFAULT NULL COMMENT '认定人ID',
  `approver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '认定人姓名',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '认定备注',
  `is_exported` tinyint NULL DEFAULT 0 COMMENT '是否已导出：0否 1是',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '认定时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`excellent_id`) USING BTREE,
  INDEX `idx_grade_summary`(`grade_summary_id` ASC) USING BTREE,
  INDEX `idx_student`(`student_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优秀成果认定表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of excellent_achievement
-- ----------------------------

-- ----------------------------
-- Table structure for grade_evaluation
-- ----------------------------
DROP TABLE IF EXISTS `grade_evaluation`;
CREATE TABLE `grade_evaluation`  (
  `grade_id` bigint NOT NULL AUTO_INCREMENT,
  `selection_id` bigint NOT NULL COMMENT '选题ID',
  `student_id` bigint NOT NULL COMMENT '学生ID（冗余）',
  `evaluator_id` bigint NOT NULL COMMENT '评分教师ID',
  `evaluator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评分教师姓名（冗余）',
  `regular_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '平时成绩（0-100）',
  `thesis_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '论文成绩（0-100）',
  `defense_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '答辩成绩（0-100）',
  `total_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '本次评分总分',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评语',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'SUBMITTED' COMMENT '状态（SUBMITTED草稿/已提交）',
  `is_locked` tinyint NULL DEFAULT 0 COMMENT '是否已锁定（0未锁定 1已锁定）',
  `lock_time` datetime NULL DEFAULT NULL,
  `locked_by` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`grade_id`) USING BTREE,
  INDEX `idx_grade_selection`(`selection_id` ASC) USING BTREE,
  INDEX `idx_grade_student`(`student_id` ASC) USING BTREE,
  INDEX `idx_grade_evaluator`(`evaluator_id` ASC) USING BTREE,
  CONSTRAINT `fk_grade_evaluator` FOREIGN KEY (`evaluator_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_grade_selection` FOREIGN KEY (`selection_id`) REFERENCES `project_selection` (`selection_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_grade_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成绩评审（每位教师对每个学生的单次评分）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of grade_evaluation
-- ----------------------------

-- ----------------------------
-- Table structure for grade_summary
-- ----------------------------
DROP TABLE IF EXISTS `grade_summary`;
CREATE TABLE `grade_summary`  (
  `summary_id` bigint NOT NULL AUTO_INCREMENT,
  `selection_id` bigint NOT NULL COMMENT '选题ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `evaluator_count` int NULL DEFAULT 0 COMMENT '参与评分的教师数量',
  `avg_regular_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '各教师平时均分',
  `avg_thesis_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '各教师论文均分',
  `avg_defense_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '各教师答辩均分',
  `total_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '系统计算总分（权重：平时30%+论文40%+答辩30%）',
  `grade_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '优秀/良好/中等/及格/不及格',
  `record_count` int NULL DEFAULT 0 COMMENT '各教师评分记录数量',
  `adjusted_total_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '管理员调整后的总分',
  `final_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '最终确认总分（优先调整值）',
  `adjusted_grade_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管理员调整后的等级',
  `final_grade_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最终确认等级',
  `is_adjusted` tinyint NULL DEFAULT 0 COMMENT '是否有管理员调整（0否 1是）',
  `is_locked` tinyint NULL DEFAULT 0 COMMENT '是否已锁定（0未锁定 1已锁定）',
  `lock_time` datetime NULL DEFAULT NULL COMMENT '锁定时间',
  `locked_by` bigint NULL DEFAULT NULL COMMENT '锁定人',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管理员备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`summary_id`) USING BTREE,
  UNIQUE INDEX `uk_grade_selection`(`selection_id` ASC) USING BTREE,
  INDEX `fk_summary_student`(`student_id` ASC) USING BTREE,
  INDEX `fk_summary_locked`(`locked_by` ASC) USING BTREE,
  CONSTRAINT `fk_summary_locked` FOREIGN KEY (`locked_by`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_summary_sel` FOREIGN KEY (`selection_id`) REFERENCES `project_selection` (`selection_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_summary_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成绩汇总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of grade_summary
-- ----------------------------

-- ----------------------------
-- Table structure for guidance_record
-- ----------------------------
DROP TABLE IF EXISTS `guidance_record`;
CREATE TABLE `guidance_record`  (
  `guide_id` bigint NOT NULL AUTO_INCREMENT,
  `selection_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `guide_time` datetime NOT NULL COMMENT '指导时间',
  `place` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '指导地点',
  `duration_minutes` int NULL DEFAULT NULL COMMENT '指导时长（分钟）',
  `guidance_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'GUIDANCE' COMMENT 'GUIDANCE指导 VISIT走访 GROUP座谈 ONLINE线上 OTHER其他',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '指导内容',
  `attachment_id` bigint NULL DEFAULT NULL COMMENT '附件ID（可选）',
  `student_feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '学生反馈',
  `feedback_time` datetime NULL DEFAULT NULL COMMENT '学生反馈时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PUBLISHED' COMMENT 'DRAFT草稿 PUBLISHED已发布',
  `academic_year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学年（从选题同步）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`guide_id`) USING BTREE,
  INDEX `idx_guide_selection`(`selection_id` ASC) USING BTREE,
  INDEX `idx_guide_teacher`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_guide_student`(`student_id` ASC) USING BTREE,
  CONSTRAINT `fk_guide_sel` FOREIGN KEY (`selection_id`) REFERENCES `project_selection` (`selection_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_guide_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_guide_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '指导记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of guidance_record
-- ----------------------------

-- ----------------------------
-- Table structure for guidance_relation
-- ----------------------------
DROP TABLE IF EXISTS `guidance_relation`;
CREATE TABLE `guidance_relation`  (
  `relation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学生姓名',
  `student_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号',
  `teacher_id` bigint NOT NULL COMMENT '指导教师ID',
  `teacher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '教师姓名',
  `teacher_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工号',
  `campus_id` bigint NULL DEFAULT NULL COMMENT '学校ID',
  `campus_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学校名称',
  `academic_year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学年',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE有效 TERMINATED已解除',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_by_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`relation_id`) USING BTREE,
  INDEX `idx_grd_student`(`student_id` ASC) USING BTREE,
  INDEX `idx_grd_teacher`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_grd_year`(`academic_year` ASC) USING BTREE,
  INDEX `idx_grd_campus`(`campus_id` ASC) USING BTREE,
  INDEX `idx_grd_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '指导关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of guidance_relation
-- ----------------------------

-- ----------------------------
-- Table structure for guidance_relation_apply
-- ----------------------------
DROP TABLE IF EXISTS `guidance_relation_apply`;
CREATE TABLE `guidance_relation_apply`  (
  `apply_id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `from_user_id` bigint NOT NULL COMMENT '申请方用户ID',
  `from_user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请方姓名',
  `from_user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请方类型：TEACHER教师 STUDENT学生',
  `to_user_id` bigint NOT NULL COMMENT '接收方用户ID',
  `to_user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收方姓名',
  `to_user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接收方类型：TEACHER教师 STUDENT学生',
  `campus_id` bigint NULL DEFAULT NULL COMMENT '学校ID',
  `campus_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学校名称',
  `academic_year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学年',
  `message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请留言',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING待处理 APPROVED已同意 REJECTED已拒绝 CANCELLED已取消 EXPIRED已过期',
  `handle_by` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `handle_by_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理人姓名',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `handle_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`apply_id`) USING BTREE,
  INDEX `idx_gra_from`(`from_user_id` ASC) USING BTREE,
  INDEX `idx_gra_to`(`to_user_id` ASC) USING BTREE,
  INDEX `idx_gra_status`(`status` ASC) USING BTREE,
  INDEX `idx_gra_year`(`academic_year` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '指导关系申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of guidance_relation_apply
-- ----------------------------

-- ----------------------------
-- Table structure for project_mid_term
-- ----------------------------
DROP TABLE IF EXISTS `project_mid_term`;
CREATE TABLE `project_mid_term`  (
  `mid_id` bigint NOT NULL AUTO_INCREMENT,
  `selection_id` bigint NOT NULL,
  `report_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `file_id` bigint NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT 'PENDING待审 PASSED通过 FAILED驳回',
  `inspector_id` bigint NULL DEFAULT NULL COMMENT '检查人(教师/管理员)',
  `inspect_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `inspect_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`mid_id`) USING BTREE,
  INDEX `idx_mid_selection`(`selection_id` ASC) USING BTREE,
  INDEX `fk_mid_inspector`(`inspector_id` ASC) USING BTREE,
  INDEX `fk_mid_file`(`file_id` ASC) USING BTREE,
  CONSTRAINT `fk_mid_file` FOREIGN KEY (`file_id`) REFERENCES `doc_file` (`file_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_mid_inspector` FOREIGN KEY (`inspector_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_mid_selection` FOREIGN KEY (`selection_id`) REFERENCES `project_selection` (`selection_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '中期检查' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of project_mid_term
-- ----------------------------

-- ----------------------------
-- Table structure for project_progress
-- ----------------------------
DROP TABLE IF EXISTS `project_progress`;
CREATE TABLE `project_progress`  (
  `progress_id` bigint NOT NULL AUTO_INCREMENT,
  `selection_id` bigint NOT NULL,
  `phase` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '阶段名称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `plan_date` date NULL DEFAULT NULL,
  `actual_date` date NULL DEFAULT NULL,
  `percent_done` tinyint NULL DEFAULT 0 COMMENT '0-100',
  `submit_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`progress_id`) USING BTREE,
  INDEX `idx_prog_selection`(`selection_id` ASC) USING BTREE,
  CONSTRAINT `fk_prog_selection` FOREIGN KEY (`selection_id`) REFERENCES `project_selection` (`selection_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目进度' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of project_progress
-- ----------------------------

-- ----------------------------
-- Table structure for project_selection
-- ----------------------------
DROP TABLE IF EXISTS `project_selection`;
CREATE TABLE `project_selection`  (
  `selection_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `topic_id` bigint NULL DEFAULT NULL COMMENT '来自题目库的课题ID，自填时为NULL',
  `campus_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请学校名称',
  `teacher_id` bigint NULL DEFAULT NULL COMMENT '指导教师',
  `is_custom_topic` tinyint NULL DEFAULT 0 COMMENT '是否自填课题 0否 1是',
  `custom_topic_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自填课题名称',
  `custom_topic_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '自填课题简介',
  `academic_year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING待审 APPROVED通过 REJECTED驳回 WITHDRAWN撤回',
  `apply_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '驳回原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`selection_id`) USING BTREE,
  UNIQUE INDEX `uk_student_year`(`student_id` ASC, `academic_year` ASC) USING BTREE,
  INDEX `idx_sel_topic`(`topic_id` ASC) USING BTREE,
  INDEX `idx_sel_teacher`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_sel_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_sel_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '选题申请' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of project_selection
-- ----------------------------

-- ----------------------------
-- Table structure for project_topic
-- ----------------------------
DROP TABLE IF EXISTS `project_topic`;
CREATE TABLE `project_topic`  (
  `topic_id` bigint NOT NULL AUTO_INCREMENT,
  `topic_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `teacher_id` bigint NULL DEFAULT NULL COMMENT '指导教师（可为空）',
  `academic_year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学年 如2024-2025',
  `max_students` int NOT NULL DEFAULT 1,
  `current_count` int NOT NULL DEFAULT 0 COMMENT '已确认人数',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN已发布 CLOSED关闭 DRAFT草稿',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `campus_id` bigint NULL DEFAULT NULL COMMENT '所属校区',
  `campus_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学校名称',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`topic_id`) USING BTREE,
  INDEX `idx_topic_teacher`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_topic_year`(`academic_year` ASC) USING BTREE,
  INDEX `idx_topic_campus`(`campus_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '毕业设计课题' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of project_topic
-- ----------------------------

-- ----------------------------
-- Table structure for proposal_report
-- ----------------------------
DROP TABLE IF EXISTS `proposal_report`;
CREATE TABLE `proposal_report`  (
  `proposal_id` bigint NOT NULL AUTO_INCREMENT,
  `selection_id` bigint NOT NULL,
  `report_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `file_id` bigint NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT 'PENDING待审 PASSED通过 FAILED驳回',
  `inspector_id` bigint NULL DEFAULT NULL COMMENT '审核人(教师/管理员)',
  `inspect_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核意见',
  `inspect_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`proposal_id`) USING BTREE,
  INDEX `idx_prop_selection`(`selection_id` ASC) USING BTREE,
  INDEX `idx_prop_inspector`(`inspector_id` ASC) USING BTREE,
  INDEX `fk_prop_file`(`file_id` ASC) USING BTREE,
  CONSTRAINT `fk_prop_file` FOREIGN KEY (`file_id`) REFERENCES `doc_file` (`file_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_prop_inspector` FOREIGN KEY (`inspector_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_prop_selection` FOREIGN KEY (`selection_id`) REFERENCES `project_selection` (`selection_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '开题报告' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of proposal_report
-- ----------------------------

-- ----------------------------
-- Table structure for quality_warning
-- ----------------------------
DROP TABLE IF EXISTS `quality_warning`;
CREATE TABLE `quality_warning`  (
  `warn_id` bigint NOT NULL AUTO_INCREMENT,
  `selection_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL COMMENT '发起预警的教师ID',
  `student_id` bigint NULL DEFAULT NULL COMMENT '关联学生ID',
  `warn_level` tinyint NOT NULL DEFAULT 1 COMMENT '1提醒 2警告 3严重',
  `warn_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'OTHER' COMMENT 'NO_GUIDANCE长期未指导 DOC_DELAY文档提交滞后 PROGRESS_DELAY进度滞后 LOW_FREQUENCY指导频率不足 OTHER其他',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'OPEN' COMMENT 'OPEN已开启 CLOSED已关闭',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `handle_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '处理意见',
  `handle_time` datetime NULL DEFAULT NULL,
  `academic_year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学年',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`warn_id`) USING BTREE,
  INDEX `idx_warn_teacher`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_warn_selection`(`selection_id` ASC) USING BTREE,
  INDEX `idx_warn_status`(`status` ASC) USING BTREE,
  INDEX `idx_warn_type`(`warn_type` ASC) USING BTREE,
  INDEX `idx_warn_level`(`warn_level` ASC) USING BTREE,
  INDEX `idx_warn_academic_year`(`academic_year` ASC) USING BTREE,
  INDEX `fk_warn_handler`(`handler_id` ASC) USING BTREE,
  CONSTRAINT `fk_warn_handler` FOREIGN KEY (`handler_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_warn_selection` FOREIGN KEY (`selection_id`) REFERENCES `project_selection` (`selection_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '质量预警' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quality_warning
-- ----------------------------

-- ----------------------------
-- Table structure for sys_campus
-- ----------------------------
DROP TABLE IF EXISTS `sys_campus`;
CREATE TABLE `sys_campus`  (
  `campus_id` bigint NOT NULL AUTO_INCREMENT COMMENT '校区ID',
  `campus_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '校区名称',
  `campus_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '校区编码',
  `sort_order` int NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`campus_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '校区' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_campus
-- ----------------------------
INSERT INTO `sys_campus` VALUES (1, '校本部', 'MAIN', 0, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);

-- ----------------------------
-- Table structure for sys_college
-- ----------------------------
DROP TABLE IF EXISTS `sys_college`;
CREATE TABLE `sys_college`  (
  `college_id` bigint NOT NULL AUTO_INCREMENT COMMENT '学院ID',
  `campus_id` bigint NULL DEFAULT NULL COMMENT '所属校区',
  `college_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学院名称',
  `college_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学院编码',
  `sort_order` int NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`college_id`) USING BTREE,
  INDEX `fk_college_campus`(`campus_id` ASC) USING BTREE,
  CONSTRAINT `fk_college_campus` FOREIGN KEY (`campus_id`) REFERENCES `sys_campus` (`campus_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学院' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_college
-- ----------------------------
INSERT INTO `sys_college` VALUES (1, 1, '计算机学院', 'CS', 0, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`config_id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统参数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------

-- ----------------------------
-- Table structure for sys_major
-- ----------------------------
DROP TABLE IF EXISTS `sys_major`;
CREATE TABLE `sys_major`  (
  `major_id` bigint NOT NULL AUTO_INCREMENT COMMENT '专业ID',
  `college_id` bigint NOT NULL COMMENT '学院ID',
  `major_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '专业名称',
  `major_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort_order` int NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`major_id`) USING BTREE,
  INDEX `fk_major_college`(`college_id` ASC) USING BTREE,
  CONSTRAINT `fk_major_college` FOREIGN KEY (`college_id`) REFERENCES `sys_college` (`college_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '专业' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_major
-- ----------------------------
INSERT INTO `sys_major` VALUES (1, 1, '软件工程', 'SE', 0, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);

-- ----------------------------
-- Table structure for sys_notification
-- ----------------------------
DROP TABLE IF EXISTS `sys_notification`;
CREATE TABLE `sys_notification`  (
  `notice_id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知组ID，用于关联同一通知的多个接收人',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `notice_type` tinyint NULL DEFAULT 1 COMMENT '1系统 2审批 3预警',
  `sender_id` bigint NULL DEFAULT NULL,
  `receiver_id` bigint NOT NULL,
  `is_read` tinyint NULL DEFAULT 0,
  `biz_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联业务类型',
  `biz_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notice_id`) USING BTREE,
  INDEX `idx_receiver`(`receiver_id` ASC, `is_read` ASC) USING BTREE,
  INDEX `idx_group_id`(`group_id` ASC) USING BTREE,
  INDEX `fk_notice_sender`(`sender_id` ASC) USING BTREE,
  CONSTRAINT `fk_notice_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_notice_sender` FOREIGN KEY (`sender_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息通知' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notification
-- ----------------------------

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作类型：LOGIN/QUERY/CREATE/UPDATE/DELETE/EXPORT/APPROVE/OTHER',
  `operation_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作名称',
  `request_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求路径',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数',
  `response_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '响应结果',
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作人用户ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人用户名',
  `user_type` int NULL DEFAULT NULL COMMENT '用户类型：1-学生 2-教师 3-管理员',
  `campus_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人所属学校',
  `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `ip_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP归属地',
  `os` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统',
  `browser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器',
  `duration` bigint NULL DEFAULT NULL COMMENT '执行时长(毫秒)',
  `module` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务模块',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作状态：SUCCESS/FAIL',
  `error_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `operate_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_log_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_log_time`(`operate_time` ASC) USING BTREE,
  INDEX `idx_log_module`(`module` ASC) USING BTREE,
  INDEX `idx_log_type`(`operation_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_operation_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `perm_id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父级ID，0为根',
  `perm_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
  `perm_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限标识，如 system:user:list',
  `perm_type` tinyint NOT NULL DEFAULT 2 COMMENT '1目录 2菜单 3按钮 4接口',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端路由',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端组件路径',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort_order` int NULL DEFAULT 0,
  `visible` tinyint NULL DEFAULT 1 COMMENT '1显示 0隐藏',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`perm_id`) USING BTREE,
  UNIQUE INDEX `uk_perm_code`(`perm_code` ASC) USING BTREE,
  INDEX `idx_parent`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 218 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限/菜单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, 0, '用户与权限管理', 'sys', 1, '/sys', NULL, 'Setting', 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (2, 1, '用户管理', 'sys:user', 2, '/sys/user', 'system/user/index', 'User', 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (3, 1, '角色管理', 'sys:role', 2, '/sys/role', 'system/role/index', 'UserFilled', 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (4, 1, '权限管理', 'sys:permission', 2, '/sys/permission', 'system/permission/index', 'Lock', 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (5, 1, '消息通知管理', 'sys:notification', 2, '/sys/notification', 'sys/notification/index', 'Bell', 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (10, 0, '毕业设计项目管理', 'project', 1, '/project', NULL, 'Notebook', 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (11, 10, '选题发布管理', 'project:topic', 2, '/project/topic', 'project/topic/index', 'Document', 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (12, 10, '选题申请管理', 'project:selection', 2, '/project/selection', 'project/selection/index', 'EditPen', 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (13, 10, '选题审批管理', 'project:approval', 2, '/project/approval', 'project/approval/index', 'CircleCheck', 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (14, 10, '项目进度管理', 'project:progress', 2, '/project/progress', 'project/progress/index', 'TrendCharts', 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (15, 10, '中期检查管理', 'project:midterm', 2, '/project/midterm', 'project/midterm/index', 'Finished', 5, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (20, 0, '文档与材料管理', 'doc', 1, '/doc', NULL, 'FolderOpened', 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (21, 20, '模板文件管理', 'doc:template', 2, '/doc/template', 'doc/template/index', 'Files', 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (22, 20, '开题报告管理', 'doc:proposal', 2, '/doc/proposal', 'doc/proposal/index', 'Reading', 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (23, 20, '论文文档管理', 'doc:thesis', 2, '/doc/thesis', 'doc/thesis/index', 'DocumentCopy', 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (24, 20, '文档版本管理', 'doc:version', 2, '/doc/version', 'doc/version/index', 'Collection', 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (25, 20, '归档材料管理', 'doc:archive', 2, '/doc/archive', 'doc/archive/index', 'Box', 5, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (30, 0, '成果与审批管理', 'achievement', 1, '/achievement', NULL, 'Trophy', 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (31, 30, '成果提交管理', 'achievement:submit', 2, '/achievement/submit', 'achievement/submit/index', 'Upload', 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (32, 30, '答辩安排管理', 'achievement:defense', 2, '/achievement/defense', 'achievement/defense/index', 'Microphone', 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (33, 30, '审批意见管理', 'achievement:approval', 2, '/achievement/approval', 'achievement/approval/index', 'Select', 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (34, 30, '成绩评审管理', 'achievement:grade', 2, '/achievement/grade', 'achievement/grade/index', 'Star', 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (40, 0, '指导与质量监控', 'guidance', 1, '/guidance', NULL, 'ChatDotRound', 5, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (41, 40, '指导记录管理', 'guidance:record', 2, '/guidance/record', 'guidance/record/index', 'Memo', 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (42, 40, '教师反馈管理', 'guidance:feedback', 2, '/guidance/feedback', 'guidance/feedback/index', 'ChatLineRound', 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (43, 40, '质量预警管理', 'guidance:warning', 2, '/guidance/warning', 'guidance/warning/index', 'Warning', 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (44, 40, '指导关系管理', 'guidance:relation', 2, '/guidance/relation', 'guidance/relation/index', 'Connection', 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (50, 0, '系统运维管理', 'sysops', 1, '/sysops', NULL, 'Tools', 6, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (51, 50, '系统参数管理', 'sysops:param', 2, '/sysops/param', NULL, 'Setting', 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (52, 50, '系统监控管理', 'sysops:monitor', 2, '/sysops/monitor', NULL, 'Monitor', 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (53, 50, '版本更新管理', 'sysops:version', 2, '/sysops/version', NULL, 'Refresh', 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (54, 50, '操作日志管理', 'sysops:log', 2, '/sysops/log', 'system/operation-log/index', 'List', 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (55, 0, '首页', 'dashboard', 2, '/dashboard', 'Dashboard', 'HomeFilled', 0, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (101, 2, '用户列表', 'sys:user:list', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (102, 2, '用户新增', 'sys:user:add', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (103, 2, '用户编辑', 'sys:user:edit', 4, NULL, NULL, NULL, 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (104, 2, '用户删除', 'sys:user:del', 4, NULL, NULL, NULL, 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (105, 3, '角色列表', 'sys:role:list', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (106, 3, '角色新增', 'sys:role:add', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (107, 3, '角色编辑', 'sys:role:edit', 4, NULL, NULL, NULL, 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (108, 3, '角色删除', 'sys:role:del', 4, NULL, NULL, NULL, 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (109, 3, '分配权限', 'sys:role:assign', 4, NULL, NULL, NULL, 5, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (110, 11, '课题发布', 'project:topic:add', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (111, 11, '课题编辑', 'project:topic:edit', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (112, 11, '课题删除', 'project:topic:del', 4, NULL, NULL, NULL, 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (113, 12, '提交申请', 'project:selection:apply', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (114, 12, '撤回申请', 'project:selection:recall', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (115, 13, '通过选题', 'project:approval:pass', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (116, 13, '驳回选题', 'project:approval:reject', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (117, 15, '提交中期报告', 'project:midterm:submit', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (118, 15, '审核中期检查', 'project:midterm:review', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (119, 21, '上传模板', 'doc:template:upload', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (120, 22, '提交开题报告', 'doc:proposal:submit', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (122, 23, '上传论文', 'doc:thesis:upload', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (124, 25, '归档', 'doc:archive:archive', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (125, 31, '成果管理', 'achievement:submit:manage', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (126, 32, '安排答辩', 'achievement:defense:arrange', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (127, 33, '审批', 'achievement:approval:do', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (128, 33, '审批管理', 'achievement:approval:manage', 4, NULL, NULL, NULL, 0, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (129, 35, '评定优秀', 'achievement:excellent:add', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (130, 41, '添加记录', 'guidance:record:add', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (131, 41, '查看记录', 'guidance:record:view', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (132, 41, '编辑记录', 'guidance:record:edit', 4, NULL, NULL, NULL, 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (133, 41, '删除记录', 'guidance:record:del', 4, NULL, NULL, NULL, 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (134, 44, '分配指导教师', 'guidance:relation:assign', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (135, 42, '教师反馈', 'guidance:feedback:add', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (136, 14, '进度列表', 'project:progress:list', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (137, 41, '填写反馈', 'guidance:record:feedback', 4, NULL, NULL, NULL, 5, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (138, 14, '删除进度', 'project:progress:del', 4, NULL, NULL, NULL, 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (139, 21, '模板列表', 'doc:template:list', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (140, 21, '编辑模板', 'doc:template:edit', 4, NULL, NULL, NULL, 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (141, 11, '课题列表', 'project:topic:list', 4, NULL, NULL, NULL, 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (142, 14, '添加进度', 'project:progress:add', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (143, 22, '审核开题报告', 'doc:proposal:review', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (144, 15, '查看中期', 'project:midterm:view', 4, NULL, NULL, NULL, 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (145, 12, '选题列表', 'project:selection:list', 4, NULL, NULL, NULL, 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (146, 21, '删除模板', 'doc:template:del', 4, NULL, NULL, NULL, 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (147, 21, '下载模板', 'doc:template:download', 4, NULL, NULL, NULL, 5, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (148, 23, '提交论文', 'doc:thesis:submit', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (149, 23, '审核论文', 'doc:thesis:review', 4, NULL, NULL, NULL, 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (150, 24, '上传版本', 'doc:version:submit', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (151, 24, '审核版本', 'doc:version:review', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (152, 24, '归档列表', 'doc:archive:list', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (153, 24, '上传归档', 'doc:archive:submit', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (154, 24, '撤回归档', 'doc:archive:recall', 4, NULL, NULL, NULL, 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (155, 24, '审核归档', 'doc:archive:review', 4, NULL, NULL, NULL, 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (156, 24, '下载归档', 'doc:archive:download', 4, NULL, NULL, NULL, 5, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (157, 31, '我的成果列表', 'achievement:submit:list', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (158, 31, '成果上传', 'achievement:submit:upload', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (159, 31, '成果删除', 'achievement:submit:del', 4, NULL, NULL, NULL, 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (160, 31, '成果下载', 'achievement:submit:download', 4, NULL, NULL, NULL, 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (161, 32, '答辩列表', 'achievement:defense:list', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (162, 32, '查看明细', 'achievement:defense:detail', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (163, 33, '查看意见', 'achievement:approval:view', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (164, 33, '添加意见', 'achievement:approval:add', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (165, 33, '编辑意见', 'achievement:approval:edit', 4, NULL, NULL, NULL, 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (166, 33, '删除意见', 'achievement:approval:del', 4, NULL, NULL, NULL, 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (167, 33, '更新状态', 'achievement:approval:status', 4, NULL, NULL, NULL, 5, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (169, 34, '成绩查看', 'achievement:grade:view', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (170, 34, '录入成绩', 'achievement:grade:input', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (171, 34, '调整成绩', 'achievement:grade:adjust', 4, NULL, NULL, NULL, 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (172, 34, '锁定成绩', 'achievement:grade:lock', 4, NULL, NULL, NULL, 4, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (173, 34, '成绩管理', 'achievement:grade:manage', 4, NULL, NULL, NULL, 0, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (174, 43, '发起预警', 'guidance:warning:add', 4, NULL, NULL, NULL, 1, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (175, 43, '处理预警', 'guidance:warning:handle', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (176, 43, '删除预警', 'guidance:warning:del', 4, NULL, NULL, NULL, 3, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (177, 42, '处理反馈', 'guidance:feedback:handle', 4, NULL, NULL, NULL, 2, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_permission` VALUES (202, 30, '优秀成果管理', 'achievement:excellent:list', 2, '/achievement/excellent', 'achievement/excellent/index', 'Medal', 5, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `user_type` tinyint NULL DEFAULT NULL COMMENT '该角色对应的账号类型：1学生 2教师 3管理员（优先级取最高）',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '管理员', 'ROLE_ADMIN', 3, '系统管理', '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_role` VALUES (2, '指导教师', 'ROLE_TEACHER', 2, '教师', '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_role` VALUES (3, '学生', 'ROLE_STUDENT', 1, '学生', '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_role` VALUES (4, '待分配', 'ROLE_PENDING', NULL, '新注册未分配权限的用户', '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `perm_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_perm`(`role_id` ASC, `perm_id` ASC) USING BTREE,
  INDEX `fk_rp_perm`(`perm_id` ASC) USING BTREE,
  CONSTRAINT `fk_rp_perm` FOREIGN KEY (`perm_id`) REFERENCES `sys_permission` (`perm_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`role_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 340 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (1, 1, 1, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (2, 1, 10, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (3, 1, 20, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (4, 1, 30, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (5, 1, 40, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (6, 1, 50, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (7, 1, 55, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (8, 1, 2, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (9, 1, 3, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (10, 1, 4, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (11, 1, 5, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (12, 1, 101, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (13, 1, 102, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (14, 1, 103, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (15, 1, 104, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (16, 1, 105, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (17, 1, 106, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (18, 1, 107, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (19, 1, 108, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (20, 1, 109, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (21, 1, 11, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (22, 1, 12, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (23, 1, 13, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (24, 1, 14, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (25, 1, 15, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (26, 1, 110, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (27, 1, 111, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (28, 1, 112, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (29, 1, 141, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (30, 1, 113, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (31, 1, 114, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (32, 1, 145, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (33, 1, 115, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (34, 1, 116, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (35, 1, 136, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (36, 1, 138, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (37, 1, 142, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (38, 1, 117, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (39, 1, 118, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (40, 1, 144, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (41, 1, 21, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (42, 1, 22, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (43, 1, 23, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (44, 1, 24, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (45, 1, 25, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (46, 1, 119, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (47, 1, 139, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (48, 1, 140, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (49, 1, 146, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (50, 1, 147, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (51, 1, 120, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (52, 1, 143, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (53, 1, 122, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (54, 1, 148, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (55, 1, 149, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (56, 1, 150, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (57, 1, 151, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (58, 1, 152, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (59, 1, 153, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (60, 1, 154, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (61, 1, 155, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (62, 1, 156, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (63, 1, 124, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (64, 1, 31, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (65, 1, 32, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (66, 1, 33, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (67, 1, 34, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (68, 1, 125, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (69, 1, 157, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (70, 1, 158, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (71, 1, 159, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (72, 1, 160, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (73, 1, 126, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (74, 1, 161, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (75, 1, 162, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (76, 1, 127, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (77, 1, 128, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (78, 1, 163, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (79, 1, 164, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (80, 1, 165, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (81, 1, 166, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (82, 1, 167, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (83, 1, 169, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (84, 1, 170, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (85, 1, 171, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (86, 1, 172, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (87, 1, 173, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (88, 1, 129, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (89, 1, 41, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (90, 1, 42, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (91, 1, 43, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (92, 1, 44, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (93, 1, 202, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (94, 1, 130, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (95, 1, 131, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (96, 1, 132, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (97, 1, 133, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (98, 1, 137, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (108, 1, 135, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (99, 1, 177, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (100, 1, 174, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (101, 1, 175, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (102, 1, 176, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (103, 1, 134, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (104, 1, 51, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (105, 1, 52, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (106, 1, 53, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (107, 1, 54, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (128, 2, 1, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (129, 2, 5, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (130, 2, 10, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (131, 2, 11, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (132, 2, 12, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (133, 2, 13, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (134, 2, 14, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (135, 2, 15, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (136, 2, 20, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (137, 2, 21, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (138, 2, 22, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (139, 2, 23, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (140, 2, 24, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (141, 2, 25, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (142, 2, 30, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (143, 2, 31, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (144, 2, 32, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (145, 2, 33, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (146, 2, 34, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (147, 2, 40, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (148, 2, 41, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (149, 2, 42, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (150, 2, 43, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (151, 2, 44, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (152, 2, 50, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (154, 2, 55, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (155, 2, 101, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (156, 2, 102, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (157, 2, 103, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (158, 2, 104, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (159, 2, 105, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (160, 2, 106, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (161, 2, 107, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (162, 2, 108, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (163, 2, 109, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (164, 2, 110, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (165, 2, 111, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (166, 2, 112, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (167, 2, 141, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (168, 2, 113, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (169, 2, 114, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (170, 2, 145, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (171, 2, 115, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (172, 2, 116, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (173, 2, 136, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (174, 2, 138, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (175, 2, 142, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (176, 2, 117, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (177, 2, 118, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (178, 2, 144, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (179, 2, 119, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (180, 2, 139, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (181, 2, 140, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (182, 2, 146, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (183, 2, 147, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (184, 2, 120, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (185, 2, 143, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (186, 2, 122, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (187, 2, 148, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (188, 2, 149, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (189, 2, 150, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (190, 2, 151, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (191, 2, 152, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (192, 2, 153, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (193, 2, 154, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (194, 2, 155, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (195, 2, 156, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (196, 2, 124, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (197, 2, 125, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (198, 2, 157, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (199, 2, 158, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (200, 2, 159, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (201, 2, 160, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (202, 2, 126, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (203, 2, 161, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (204, 2, 162, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (205, 2, 127, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (206, 2, 128, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (207, 2, 163, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (208, 2, 164, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (209, 2, 165, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (210, 2, 166, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (211, 2, 167, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (212, 2, 169, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (213, 2, 170, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (214, 2, 171, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (215, 2, 172, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (216, 2, 173, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (217, 2, 129, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (218, 2, 130, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (219, 2, 131, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (220, 2, 132, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (221, 2, 133, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (222, 2, 137, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (223, 2, 135, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (224, 2, 134, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (255, 3, 10, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (256, 3, 11, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (257, 3, 12, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (258, 3, 14, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (259, 3, 15, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (260, 3, 20, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (261, 3, 21, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (262, 3, 22, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (263, 3, 23, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (264, 3, 25, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (265, 3, 30, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (266, 3, 31, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (267, 3, 33, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (268, 3, 34, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (269, 3, 40, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (270, 3, 41, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (271, 3, 42, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (272, 3, 43, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (273, 3, 113, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (274, 3, 114, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (275, 3, 145, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (276, 3, 136, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (277, 3, 138, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (278, 3, 117, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (279, 3, 144, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (280, 3, 139, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (281, 3, 146, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (282, 3, 147, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (283, 3, 120, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (284, 3, 143, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (285, 3, 122, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (286, 3, 148, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (287, 3, 150, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (288, 3, 152, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (289, 3, 153, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (290, 3, 154, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (291, 3, 156, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (292, 3, 157, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (293, 3, 158, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (294, 3, 159, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (295, 3, 160, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (296, 3, 161, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (297, 3, 127, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (298, 3, 163, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (299, 3, 169, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (300, 3, 129, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (301, 3, 130, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (302, 3, 131, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (303, 3, 132, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (304, 3, 133, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (305, 3, 137, '2026-04-12 01:04:51');
INSERT INTO `sys_role_permission` VALUES (306, 3, 134, '2026-04-12 01:04:51');

-- ----------------------------
-- Table structure for sys_template_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_template_file`;
CREATE TABLE `sys_template_file`  (
  `template_id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `phase` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '毕设阶段',
  `campus_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学校名称',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务器文件名',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件存储路径',
  `file_size` bigint NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '文件扩展名',
  `uploader_id` bigint NULL DEFAULT NULL COMMENT '上传人ID',
  `uploader_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上传人姓名',
  `upload_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`template_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模板文件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_template_file
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录名',
  `user_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'BCrypt密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `user_type` tinyint NOT NULL DEFAULT 1 COMMENT '1学生 2教师 3管理员',
  `student_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号',
  `teacher_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工号',
  `college_id` bigint NULL DEFAULT NULL,
  `campus_id` bigint NULL DEFAULT NULL COMMENT '所属校区',
  `campus_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学校名称（冗余字段）',
  `major_id` bigint NULL DEFAULT NULL COMMENT '学生所属专业',
  `user_avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1 COMMENT '1正常 0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_user_name`(`user_name` ASC) USING BTREE,
  INDEX `idx_student_no`(`student_no` ASC) USING BTREE,
  INDEX `idx_teacher_no`(`teacher_no` ASC) USING BTREE,
  INDEX `fk_user_college`(`college_id` ASC) USING BTREE,
  INDEX `fk_user_campus`(`campus_id` ASC) USING BTREE,
  INDEX `fk_user_major`(`major_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_campus` FOREIGN KEY (`campus_id`) REFERENCES `sys_campus` (`campus_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_college` FOREIGN KEY (`college_id`) REFERENCES `sys_college` (`college_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_major` FOREIGN KEY (`major_id`) REFERENCES `sys_major` (`major_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2b$12$FKzjFp99xf/G2PUBnKYuy.lkJUuxmaWuLDReNuvXaF3WsC6meVTFC', '系统管理员', 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '13800000000', NULL, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_user` VALUES (2, 'teacher1', '$2b$12$FKzjFp99xf/G2PUBnKYuy.lkJUuxmaWuLDReNuvXaF3WsC6meVTFC', '张教授', 2, NULL, NULL, 1, NULL, '哈尔滨信息工程学院', 1, NULL, '13800000001', NULL, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);
INSERT INTO `sys_user` VALUES (3, 'student1', '$2b$12$FKzjFp99xf/G2PUBnKYuy.lkJUuxmaWuLDReNuvXaF3WsC6meVTFC', '李同学', 1, '2021001', NULL, NULL, 1, '哈尔滨信息工程学院', 1, NULL, '13800000002', NULL, 1, '2026-04-12 01:04:51', '2026-04-12 01:04:51', 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `fk_ur_role`(`role_id` ASC) USING BTREE,
  CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`role_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1, '2026-04-12 01:04:51');
INSERT INTO `sys_user_role` VALUES (2, 2, 2, '2026-04-12 01:04:51');
INSERT INTO `sys_user_role` VALUES (3, 3, 3, '2026-04-12 01:04:51');

-- ----------------------------
-- Table structure for teacher_feedback
-- ----------------------------
DROP TABLE IF EXISTS `teacher_feedback`;
CREATE TABLE `teacher_feedback`  (
  `fb_id` bigint NOT NULL AUTO_INCREMENT,
  `selection_id` bigint NOT NULL,
  `student_id` bigint NULL DEFAULT NULL COMMENT '关联选题后自动填充',
  `teacher_id` bigint NOT NULL,
  `feedback_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'OTHER' COMMENT 'TEACHING_QUALITY教学质量 STUDENT_ISSUE学生问题 SYSTEM_IMPROVE系统改进 RESOURCE_LACK资源不足 OTHER其他',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING待处理 HANDLING处理中 RESOLVED已解决 REJECTED已驳回',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `handle_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '处理意见',
  `handle_time` datetime NULL DEFAULT NULL,
  `academic_year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学年',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`fb_id`) USING BTREE,
  INDEX `idx_fb_teacher`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_fb_selection`(`selection_id` ASC) USING BTREE,
  INDEX `idx_fb_status`(`status` ASC) USING BTREE,
  INDEX `idx_fb_type`(`feedback_type` ASC) USING BTREE,
  INDEX `idx_fb_academic_year`(`academic_year` ASC) USING BTREE,
  CONSTRAINT `fk_fb_selection` FOREIGN KEY (`selection_id`) REFERENCES `project_selection` (`selection_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_fb_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教师反馈' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher_feedback
-- ----------------------------

-- ----------------------------
-- Table structure for thesis_document
-- ----------------------------
DROP TABLE IF EXISTS `thesis_document`;
CREATE TABLE `thesis_document`  (
  `thesis_id` bigint NOT NULL AUTO_INCREMENT,
  `selection_id` bigint NOT NULL COMMENT '选题ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `report_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '论文说明文字',
  `file_id` bigint NULL DEFAULT NULL COMMENT '附件ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT 'PENDING待审核 PASSED已通过 FAILED已驳回',
  `inspector_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `inspect_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '审核意见',
  `inspect_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `version_no` int NULL DEFAULT 1 COMMENT '版本号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`thesis_id`) USING BTREE,
  INDEX `idx_thesis_selection`(`selection_id` ASC) USING BTREE,
  INDEX `idx_thesis_student`(`student_id` ASC) USING BTREE,
  CONSTRAINT `fk_thesis_selection` FOREIGN KEY (`selection_id`) REFERENCES `project_selection` (`selection_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '论文文档' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of thesis_document
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
