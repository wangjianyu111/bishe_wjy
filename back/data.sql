CREATE DATABASE IF NOT EXISTS gd_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gd_platform;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 成果提交表
-- ============================================================
DROP TABLE IF EXISTS achievement_submit;
CREATE TABLE achievement_submit (
    submit_id        BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提交ID',
    selection_id     BIGINT NOT NULL COMMENT '选题ID',
    student_id       BIGINT NOT NULL COMMENT '学生ID',
    file_id          BIGINT DEFAULT NULL COMMENT '附件ID',
    remark           VARCHAR(500) DEFAULT NULL COMMENT '备注说明',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted       TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    KEY idx_selection (selection_id),
    KEY idx_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成果提交表';

-- ============================================================
-- 文档归档表
-- ============================================================
DROP TABLE IF EXISTS doc_archive;
CREATE TABLE doc_archive (
    archive_id       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '归档ID',
    selection_id     BIGINT NOT NULL COMMENT '选题ID',
    student_id       BIGINT NOT NULL COMMENT '学生ID',
    stage_name       VARCHAR(100) NOT NULL COMMENT '归档阶段名称',
    file_id          BIGINT DEFAULT NULL COMMENT '附件ID',
    remark           VARCHAR(500) DEFAULT NULL COMMENT '备注说明',
    status           VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态：SUBMITTED待审核 PASSED已通过 FAILED已驳回',
    reviewer_id      BIGINT DEFAULT NULL COMMENT '审核人ID',
    reviewer_name    VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
    review_comment   VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    review_time      DATETIME DEFAULT NULL COMMENT '审核时间',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted       TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    KEY idx_selection (selection_id),
    KEY idx_student (student_id),
    KEY idx_stage (stage_name),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档归档表';

-- ============================================================
-- 文档版本表（保留）
-- ============================================================
DROP TABLE IF EXISTS doc_version;
CREATE TABLE doc_version (
    version_id       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '版本ID',
    selection_id     BIGINT NOT NULL COMMENT '选题ID',
    student_id       BIGINT NOT NULL COMMENT '学生ID',
    stage_name       VARCHAR(100) NOT NULL COMMENT '阶段名称',
    version_no       INT NOT NULL DEFAULT 1 COMMENT '版本号',
    file_id          BIGINT DEFAULT NULL COMMENT '附件ID',
    remark           VARCHAR(500) DEFAULT NULL COMMENT '备注说明',
    status           VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态：SUBMITTED待审核 PASSED已通过 FAILED已驳回',
    reviewer_id      BIGINT DEFAULT NULL COMMENT '审核人ID',
    review_comment   VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    review_time      DATETIME DEFAULT NULL COMMENT '审核时间',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted       TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    KEY idx_selection (selection_id),
    KEY idx_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档版本表';

-- ============================================================
-- 文档文件表
-- ============================================================
DROP TABLE IF EXISTS doc_file;
CREATE TABLE doc_file (
    file_id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID',
    original_name    VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name      VARCHAR(255) NOT NULL COMMENT '服务器文件名',
    file_path        VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_size        BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    mime_type        VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型',
    uploader_id      BIGINT NOT NULL COMMENT '上传人ID',
    biz_type         VARCHAR(50) DEFAULT NULL COMMENT '业务类型：ARCHIVE归档 VERSION版本',
    biz_id           BIGINT DEFAULT NULL COMMENT '业务ID',
    selection_id     BIGINT DEFAULT NULL COMMENT '选题ID',
    version_no       INT DEFAULT 1 COMMENT '版本号',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted       TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    KEY idx_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档文件表';

-- ============================================================
-- 模板文件表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_template_file (
    template_id      BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    phase            VARCHAR(100) NOT NULL COMMENT '毕设阶段',
    campus_name      VARCHAR(100) NOT NULL COMMENT '学校名称',
    file_name        VARCHAR(255) NOT NULL COMMENT '服务器文件名',
    original_name    VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path        VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_size        BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    file_type        VARCHAR(50)  DEFAULT '' COMMENT '文件扩展名',
    uploader_id      BIGINT COMMENT '上传人ID',
    uploader_name    VARCHAR(50) COMMENT '上传人姓名',
    upload_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    is_deleted       TINYINT DEFAULT 0 COMMENT '0正常 1删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板文件表';

-- ============================================================
-- 大学生毕业设计审批一体化平台 - MySQL 8 数据库脚本
-- 说明：密码字段存 BCrypt；业务主键统一 BIGINT；逻辑删除用 is_deleted
-- ============================================================

-- ---------- 学院 / 专业 / 校区 ----------
DROP TABLE IF EXISTS sys_major;
DROP TABLE IF EXISTS sys_college;
DROP TABLE IF EXISTS sys_campus;
CREATE TABLE sys_campus (
  campus_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '校区ID',
  campus_name VARCHAR(100) NOT NULL COMMENT '校区名称',
  campus_code VARCHAR(50) DEFAULT NULL COMMENT '校区编码',
  sort_order INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0 COMMENT '0正常 1删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校区';

CREATE TABLE sys_college (
  college_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学院ID',
  campus_id BIGINT DEFAULT NULL COMMENT '所属校区',
  college_name VARCHAR(100) NOT NULL COMMENT '学院名称',
  college_code VARCHAR(50) DEFAULT NULL COMMENT '学院编码',
  sort_order INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0 COMMENT '0正常 1删除',
  CONSTRAINT fk_college_campus FOREIGN KEY (campus_id) REFERENCES sys_campus (campus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院';

CREATE TABLE sys_major (
  major_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '专业ID',
  college_id BIGINT NOT NULL COMMENT '学院ID',
  major_name VARCHAR(100) NOT NULL COMMENT '专业名称',
  major_code VARCHAR(50) DEFAULT NULL,
  sort_order INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  CONSTRAINT fk_major_college FOREIGN KEY (college_id) REFERENCES sys_college (college_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业';

-- ---------- 用户 ----------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  user_name VARCHAR(50) NOT NULL COMMENT '登录名',
  user_password VARCHAR(255) NOT NULL COMMENT 'BCrypt密码',
  real_name VARCHAR(50) NOT NULL COMMENT '姓名',
  user_type TINYINT NOT NULL DEFAULT 1 COMMENT '1学生 2教师 3管理员',
  student_no VARCHAR(32) DEFAULT NULL COMMENT '学号',
  teacher_no VARCHAR(32) DEFAULT NULL COMMENT '工号',
  college_id BIGINT DEFAULT NULL,
  campus_id BIGINT DEFAULT NULL COMMENT '所属校区',
  campus_name VARCHAR(100) DEFAULT NULL COMMENT '学校名称（冗余字段）',
  major_id BIGINT DEFAULT NULL COMMENT '学生所属专业',
  user_avatar VARCHAR(512) DEFAULT NULL,
  user_phone VARCHAR(20) DEFAULT NULL,
  user_email VARCHAR(100) DEFAULT NULL,
  status TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  UNIQUE KEY uk_user_name (user_name),
  KEY idx_student_no (student_no),
  KEY idx_teacher_no (teacher_no),
  CONSTRAINT fk_user_college FOREIGN KEY (college_id) REFERENCES sys_college (college_id),
  CONSTRAINT fk_user_campus FOREIGN KEY (campus_id) REFERENCES sys_campus (campus_id),
  CONSTRAINT fk_user_major FOREIGN KEY (major_id) REFERENCES sys_major (major_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- ---------- RBAC ----------
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;

CREATE TABLE sys_role (
  role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
  role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
  user_type TINYINT DEFAULT NULL COMMENT '该角色对应的账号类型：1学生 2教师 3管理员（优先级取最高）',
  remark VARCHAR(255) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

CREATE TABLE sys_permission (
  perm_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT DEFAULT 0 COMMENT '父级ID，0为根',
  perm_name VARCHAR(100) NOT NULL COMMENT '名称',
  perm_code VARCHAR(100) NOT NULL COMMENT '权限标识，如 system:user:list',
  perm_type TINYINT NOT NULL DEFAULT 2 COMMENT '1目录 2菜单 3按钮 4接口',
  path VARCHAR(255) DEFAULT NULL COMMENT '前端路由',
  component VARCHAR(255) DEFAULT NULL COMMENT '前端组件路径',
  icon VARCHAR(100) DEFAULT NULL,
  sort_order INT DEFAULT 0,
  visible TINYINT DEFAULT 1 COMMENT '1显示 0隐藏',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  UNIQUE KEY uk_perm_code (perm_code),
  KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限/菜单';

CREATE TABLE sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_role (user_id, role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES sys_user (user_id),
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES sys_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色';

CREATE TABLE sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  perm_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_role_perm (role_id, perm_id),
  CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES sys_role (role_id),
  CONSTRAINT fk_rp_perm FOREIGN KEY (perm_id) REFERENCES sys_permission (perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限';

-- ---------- 消息通知 ----------
DROP TABLE IF EXISTS sys_notification;
CREATE TABLE sys_notification (
  notice_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  group_id VARCHAR(36) DEFAULT NULL COMMENT '通知组ID，用于关联同一通知的多个接收人',
  title VARCHAR(200) NOT NULL,
  content TEXT,
  notice_type TINYINT DEFAULT 1 COMMENT '1系统 2审批 3预警',
  sender_id BIGINT DEFAULT NULL,
  receiver_id BIGINT NOT NULL,
  is_read TINYINT DEFAULT 0,
  biz_type VARCHAR(50) DEFAULT NULL COMMENT '关联业务类型',
  biz_id BIGINT DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_receiver (receiver_id, is_read),
  KEY idx_group_id (group_id),
  CONSTRAINT fk_notice_sender FOREIGN KEY (sender_id) REFERENCES sys_user (user_id),
  CONSTRAINT fk_notice_receiver FOREIGN KEY (receiver_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知';

-- ---------- 课题 / 选题 / 进度 / 中期 ----------
DROP TABLE IF EXISTS project_mid_term;
DROP TABLE IF EXISTS project_progress;
DROP TABLE IF EXISTS project_selection;
DROP TABLE IF EXISTS project_topic;

CREATE TABLE project_topic (
  topic_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  topic_name VARCHAR(255) NOT NULL,
  teacher_id BIGINT DEFAULT NULL COMMENT '指导教师（可为空）',
  academic_year VARCHAR(20) NOT NULL COMMENT '学年 如2024-2025',
  max_students INT NOT NULL DEFAULT 1,
  current_count INT NOT NULL DEFAULT 0 COMMENT '已确认人数',
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN已发布 CLOSED关闭 DRAFT草稿',
  description TEXT,
  campus_id BIGINT DEFAULT NULL COMMENT '所属校区',
  campus_name VARCHAR(100) DEFAULT NULL COMMENT '学校名称',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  KEY idx_topic_teacher (teacher_id),
  KEY idx_topic_year (academic_year),
  KEY idx_topic_campus (campus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='毕业设计课题';

CREATE TABLE project_selection (
  selection_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  topic_id BIGINT DEFAULT NULL COMMENT '来自题目库的课题ID，自填时为NULL',
  campus_name VARCHAR(100) DEFAULT NULL COMMENT '申请学校名称',
  teacher_id BIGINT DEFAULT NULL COMMENT '指导教师',
  is_custom_topic TINYINT DEFAULT 0 COMMENT '是否自填课题 0否 1是',
  custom_topic_name VARCHAR(255) DEFAULT NULL COMMENT '自填课题名称',
  custom_topic_description TEXT DEFAULT NULL COMMENT '自填课题简介',
  academic_year VARCHAR(20) NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING待审 APPROVED通过 REJECTED驳回 WITHDRAWN撤回',
  apply_reason VARCHAR(500) DEFAULT NULL,
  reject_reason VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  UNIQUE KEY uk_student_year (student_id, academic_year),
  KEY idx_sel_topic (topic_id),
  KEY idx_sel_teacher (teacher_id),
  KEY idx_sel_status (status),
  CONSTRAINT fk_sel_student FOREIGN KEY (student_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选题申请';

CREATE TABLE project_progress (
  progress_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  phase VARCHAR(50) NOT NULL COMMENT '阶段名称',
  content TEXT,
  plan_date DATE DEFAULT NULL,
  actual_date DATE DEFAULT NULL,
  percent_done TINYINT DEFAULT 0 COMMENT '0-100',
  submit_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0 COMMENT '0正常 1删除',
  KEY idx_prog_selection (selection_id),
  CONSTRAINT fk_prog_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目进度';

CREATE TABLE project_mid_term (
  mid_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  report_content TEXT,
  file_id BIGINT DEFAULT NULL,
  status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待审 PASSED通过 FAILED驳回',
  inspector_id BIGINT DEFAULT NULL COMMENT '检查人(教师/管理员)',
  inspect_comment VARCHAR(500) DEFAULT NULL,
  inspect_time DATETIME DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_mid_selection (selection_id),
  CONSTRAINT fk_mid_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_mid_inspector FOREIGN KEY (inspector_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中期检查';

-- ---------- 文档 / 模板 / 版本 ----------
DROP TABLE IF EXISTS doc_version;
DROP TABLE IF EXISTS archive_material;
DROP TABLE IF EXISTS thesis_document;
DROP TABLE IF EXISTS proposal_report;
DROP TABLE IF EXISTS doc_file;
DROP TABLE IF EXISTS doc_template;

CREATE TABLE doc_template (
  template_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_name VARCHAR(200) NOT NULL,
  biz_category VARCHAR(50) NOT NULL COMMENT 'PROPOSAL开题 THESIS论文 MID中期 OTHER',
  file_id BIGINT DEFAULT NULL COMMENT '模板文件',
  description VARCHAR(500) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板文件';

DROP TABLE IF EXISTS doc_file;
CREATE TABLE doc_file (
  file_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  original_name VARCHAR(512) NOT NULL,
  stored_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
  file_path VARCHAR(1024) NOT NULL COMMENT '相对或绝对路径',
  file_size BIGINT DEFAULT 0,
  mime_type VARCHAR(128) DEFAULT NULL,
  uploader_id BIGINT NOT NULL,
  biz_type VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
  biz_id BIGINT DEFAULT NULL,
  selection_id BIGINT DEFAULT NULL,
  version_no INT NOT NULL DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  KEY idx_doc_selection (selection_id),
  KEY idx_doc_biz (biz_type, biz_id),
  CONSTRAINT fk_doc_uploader FOREIGN KEY (uploader_id) REFERENCES sys_user (user_id),
  CONSTRAINT fk_doc_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档文件';

ALTER TABLE doc_template ADD CONSTRAINT fk_tpl_file FOREIGN KEY (file_id) REFERENCES doc_file (file_id);

ALTER TABLE project_mid_term ADD CONSTRAINT fk_mid_file FOREIGN KEY (file_id) REFERENCES doc_file (file_id);

CREATE TABLE IF NOT EXISTS doc_version (
    version_id       BIGINT       AUTO_INCREMENT  PRIMARY KEY,
    selection_id    BIGINT       NOT NULL        COMMENT '选题ID',
    student_id      BIGINT       NOT NULL        COMMENT '学生ID',
    stage_name      VARCHAR(100) NOT NULL        COMMENT '毕设阶段名称（如：开题报告、中期检查、论文定稿）',
    version_no      INT          NOT NULL DEFAULT 1 COMMENT '版本号',
    file_id         BIGINT       NULL            COMMENT '附件ID',
    remark          VARCHAR(500) NULL            COMMENT '备注说明',
    status          VARCHAR(20)  NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态：SUBMITTED已提交/PASSED通过/FAILED驳回',
    reviewer_id     BIGINT       NULL            COMMENT '审核人ID',
    review_comment  VARCHAR(500) NULL            COMMENT '审核意见',
    review_time     DATETIME     NULL            COMMENT '审核时间',
    create_time     DATETIME     NOT NULL        COMMENT '创建时间',
    update_time     DATETIME     NOT NULL        COMMENT '更新时间',
    is_deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档版本管理';

INSERT INTO doc_version (version_id, selection_id, student_id, stage_name, version_no, file_id, remark, status, create_time, update_time)
SELECT 1, 1, 3, '开题报告', 1, NULL, '测试版本', 'SUBMITTED', NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM doc_version WHERE version_id = 1);

CREATE TABLE proposal_report (
  proposal_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  report_content TEXT DEFAULT NULL,
  file_id BIGINT DEFAULT NULL,
  status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待审 PASSED通过 FAILED驳回',
  inspector_id BIGINT DEFAULT NULL COMMENT '审核人(教师/管理员)',
  inspect_comment VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  inspect_time DATETIME DEFAULT NULL COMMENT '审核时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_prop_selection (selection_id),
  KEY idx_prop_inspector (inspector_id),
  CONSTRAINT fk_prop_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_prop_file FOREIGN KEY (file_id) REFERENCES doc_file (file_id),
  CONSTRAINT fk_prop_inspector FOREIGN KEY (inspector_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开题报告';

DROP TABLE IF EXISTS thesis_document;
CREATE TABLE thesis_document (
  thesis_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL COMMENT '选题ID',
  student_id BIGINT NOT NULL COMMENT '学生ID',
  report_content TEXT DEFAULT NULL COMMENT '论文说明文字',
  file_id BIGINT DEFAULT NULL COMMENT '附件ID',
  status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待审核 PASSED已通过 FAILED已驳回',
  inspector_id BIGINT DEFAULT NULL COMMENT '审核人ID',
  inspect_comment TEXT DEFAULT NULL COMMENT '审核意见',
  inspect_time DATETIME DEFAULT NULL COMMENT '审核时间',
  version_no INT DEFAULT 1 COMMENT '版本号',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0 COMMENT '0正常 1删除',
  KEY idx_thesis_selection (selection_id),
  KEY idx_thesis_student (student_id),
  CONSTRAINT fk_thesis_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论文文档';

CREATE TABLE archive_material (
  archive_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  file_id BIGINT DEFAULT NULL,
  archive_no VARCHAR(64) DEFAULT NULL COMMENT '归档编号',
  archived TINYINT DEFAULT 0 COMMENT '1已归档',
  archive_time DATETIME DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_archive_selection (selection_id),
  CONSTRAINT fk_arch_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_arch_file FOREIGN KEY (file_id) REFERENCES doc_file (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归档材料';

-- ---------- 成果 / 答辩 / 成绩 / 优秀 ----------
DROP TABLE IF EXISTS excellent_achievement;
DROP TABLE IF EXISTS grade_evaluation;
DROP TABLE IF EXISTS defense_arrangement;
DROP TABLE IF EXISTS defense_session;
DROP TABLE IF EXISTS achievement_submission;
DROP TABLE IF EXISTS approval_record;

CREATE TABLE achievement_submission (
  ach_sub_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  submit_type VARCHAR(50) NOT NULL COMMENT 'CODE代码 THESIS终稿 OTHER',
  file_id BIGINT DEFAULT NULL,
  description VARCHAR(500) DEFAULT NULL,
  status VARCHAR(20) DEFAULT 'SUBMITTED',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_achsel_selection (selection_id),
  CONSTRAINT fk_achsel_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_achsel_file FOREIGN KEY (file_id) REFERENCES doc_file (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成果提交';

CREATE TABLE defense_session (
  session_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_name VARCHAR(200) NOT NULL COMMENT '场次名称',
  defense_date DATE NOT NULL COMMENT '答辩日期',
  start_time TIME DEFAULT NULL COMMENT '开始时间',
  end_time TIME DEFAULT NULL COMMENT '结束时间',
  location VARCHAR(200) DEFAULT NULL COMMENT '答辩地点',
  defense_form VARCHAR(20) DEFAULT NULL COMMENT '答辩形式：ONLINE线上 OFFLINE线下',
  academic_year VARCHAR(20) NOT NULL COMMENT '学年',
  file_id BIGINT DEFAULT NULL COMMENT '附件ID',
  teacher_id BIGINT DEFAULT NULL COMMENT '发布教师ID',
  campus_name VARCHAR(100) DEFAULT NULL COMMENT '所属学校',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注说明',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  KEY idx_teacher (teacher_id),
  KEY idx_campus (campus_name),
  KEY idx_date (defense_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答辩场次';

CREATE TABLE defense_arrangement (
  arrange_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT NOT NULL COMMENT '场次ID',
  selection_id BIGINT NOT NULL COMMENT '选题ID',
  student_id BIGINT NOT NULL COMMENT '学生ID',
  teacher_id BIGINT DEFAULT NULL COMMENT '指导教师ID',
  sort_order INT DEFAULT 0 COMMENT '答辩顺序',
  status VARCHAR(20) DEFAULT 'SCHEDULED' COMMENT '状态：SCHEDULED待答辩 DONE已完成',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
  UNIQUE KEY uk_defense_session_sel (session_id, selection_id),
  KEY idx_student (student_id),
  CONSTRAINT fk_def_sess FOREIGN KEY (session_id) REFERENCES defense_session (session_id),
  CONSTRAINT fk_def_sel FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答辩安排';

CREATE TABLE approval_record (
  approval_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  biz_type VARCHAR(50) NOT NULL COMMENT 'SELECTION选题 PROPOSAL开题 MID中期 THESIS论文等',
  biz_id BIGINT NOT NULL,
  approver_id BIGINT NOT NULL,
  action VARCHAR(30) NOT NULL COMMENT 'APPROVE REJECT',
  comment_text VARCHAR(1000) DEFAULT NULL,
  approval_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_appr_biz (biz_type, biz_id),
  CONSTRAINT fk_appr_user FOREIGN KEY (approver_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批意见(通用)';

CREATE TABLE IF NOT EXISTS grade_summary (
  summary_id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id         BIGINT NOT NULL COMMENT '选题ID',
  student_id          BIGINT NOT NULL COMMENT '学生ID',
  evaluator_count     INT DEFAULT 0 COMMENT '参与评分的教师数量',
  avg_regular_score   DECIMAL(5,2) DEFAULT NULL COMMENT '各教师平时均分',
  avg_thesis_score    DECIMAL(5,2) DEFAULT NULL COMMENT '各教师论文均分',
  avg_defense_score   DECIMAL(5,2) DEFAULT NULL COMMENT '各教师答辩均分',
  total_score         DECIMAL(5,2) DEFAULT NULL COMMENT '系统计算总分（权重：平时30%+论文40%+答辩30%）',
  grade_level         VARCHAR(20) DEFAULT NULL COMMENT '优秀/良好/中等/及格/不及格',
  record_count        INT DEFAULT 0 COMMENT '各教师评分记录数量',
  adjusted_total_score DECIMAL(5,2) DEFAULT NULL COMMENT '管理员调整后的总分',
  final_score         DECIMAL(5,2) DEFAULT NULL COMMENT '最终确认总分（优先调整值）',
  adjusted_grade_level VARCHAR(20) DEFAULT NULL COMMENT '管理员调整后的等级',
  final_grade_level   VARCHAR(20) DEFAULT NULL COMMENT '最终确认等级',
  is_adjusted         TINYINT DEFAULT 0 COMMENT '是否有管理员调整（0否 1是）',
  is_locked           TINYINT DEFAULT 0 COMMENT '是否已锁定（0未锁定 1已锁定）',
  lock_time           DATETIME DEFAULT NULL COMMENT '锁定时间',
  locked_by           BIGINT DEFAULT NULL COMMENT '锁定人',
  remark              VARCHAR(500) DEFAULT NULL COMMENT '管理员备注',
  create_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted          TINYINT DEFAULT 0 COMMENT '0正常 1删除',
  UNIQUE KEY uk_grade_selection (selection_id),
  CONSTRAINT fk_summary_sel FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_summary_student FOREIGN KEY (student_id) REFERENCES sys_user (user_id),
  CONSTRAINT fk_summary_locked FOREIGN KEY (locked_by) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩汇总表';

-- ============================================================
-- 教师评分明细表
-- ============================================================
DROP TABLE IF EXISTS grade_evaluation;
CREATE TABLE grade_evaluation (
  grade_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL COMMENT '选题ID',
  student_id BIGINT NOT NULL COMMENT '学生ID（冗余）',
  evaluator_id BIGINT NOT NULL COMMENT '评分教师ID',
  evaluator_name VARCHAR(50) DEFAULT NULL COMMENT '评分教师姓名（冗余）',
  regular_score DECIMAL(5,2) DEFAULT NULL COMMENT '平时成绩（0-100）',
  thesis_score DECIMAL(5,2) DEFAULT NULL COMMENT '论文成绩（0-100）',
  defense_score DECIMAL(5,2) DEFAULT NULL COMMENT '答辩成绩（0-100）',
  total_score DECIMAL(5,2) DEFAULT NULL COMMENT '本次评分总分',
  comment VARCHAR(500) DEFAULT NULL COMMENT '评语',
  status VARCHAR(20) DEFAULT 'SUBMITTED' COMMENT '状态（SUBMITTED草稿/已提交）',
  is_locked TINYINT DEFAULT 0 COMMENT '是否已锁定（0未锁定 1已锁定）',
  lock_time DATETIME DEFAULT NULL,
  locked_by BIGINT DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0 COMMENT '0正常 1删除',
  KEY idx_grade_selection (selection_id),
  KEY idx_grade_student (student_id),
  KEY idx_grade_evaluator (evaluator_id),
  CONSTRAINT fk_grade_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_grade_student FOREIGN KEY (student_id) REFERENCES sys_user (user_id),
  CONSTRAINT fk_grade_evaluator FOREIGN KEY (evaluator_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩评审（每位教师对每个学生的单次评分）';

DROP TABLE IF EXISTS excellent_achievement;
CREATE TABLE excellent_achievement (
  excellent_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
  grade_summary_id  BIGINT NOT NULL COMMENT '关联的成绩汇总ID',
  selection_id       BIGINT NOT NULL COMMENT '选题ID（冗余）',
  student_id         BIGINT NOT NULL COMMENT '学生ID',
  final_score        DECIMAL(5,2) DEFAULT NULL COMMENT '最终认定总分',
  final_grade_level  VARCHAR(20) DEFAULT NULL COMMENT '最终认定等级（固定为优秀）',
  status             VARCHAR(20) NOT NULL DEFAULT 'APPROVED' COMMENT '状态：PENDING/APPROVED/REJECTED',
  approver_id        BIGINT DEFAULT NULL COMMENT '认定人ID',
  approver_name      VARCHAR(50) DEFAULT NULL COMMENT '认定人姓名',
  remark             VARCHAR(500) DEFAULT NULL COMMENT '认定备注',
  is_exported        TINYINT DEFAULT 0 COMMENT '是否已导出：0否 1是',
  approve_time       DATETIME DEFAULT NULL COMMENT '认定时间',
  create_time        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted         TINYINT DEFAULT 0 COMMENT '0正常 1删除',
  KEY idx_grade_summary (grade_summary_id),
  KEY idx_student (student_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优秀成果认定表';

-- ============================================================
-- 学校优秀分数线配置表
-- ============================================================
DROP TABLE IF EXISTS campus_threshold;
CREATE TABLE campus_threshold (
  threshold_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
  campus_name        VARCHAR(100) NOT NULL COMMENT '学校名称',
  academic_year      VARCHAR(20) NOT NULL COMMENT '学年',
  excellent_score    DECIMAL(5,2) NOT NULL COMMENT '优秀分数线（成绩总分必须大于此分数才算优秀）',
  remark             VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_time        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted         TINYINT DEFAULT 0 COMMENT '0正常 1删除',
  UNIQUE KEY uk_campus_year (campus_name, academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校优秀分数线配置表';

-- ---------- 指导 / 反馈 / 预警 / 指导关系 ----------
DROP TABLE IF EXISTS quality_warning;
DROP TABLE IF EXISTS teacher_feedback;
DROP TABLE IF EXISTS guidance_record;
DROP TABLE IF EXISTS guidance_relation;

CREATE TABLE guidance_relation (
  rel_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  rel_type VARCHAR(20) NOT NULL DEFAULT 'PRIMARY' COMMENT 'PRIMARY主指导 SECONDARY副指导',
  academic_year VARCHAR(20) DEFAULT NULL COMMENT '学年',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE有效 INACTIVE失效',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_guidance (selection_id, teacher_id, rel_type),
  KEY idx_gr_teacher (teacher_id),
  KEY idx_gr_selection (selection_id),
  CONSTRAINT fk_gr_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_gr_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指导关系';

CREATE TABLE guidance_record (
  guide_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  guide_time DATETIME NOT NULL COMMENT '指导时间',
  place VARCHAR(200) DEFAULT NULL COMMENT '指导地点',
  duration_minutes INT DEFAULT NULL COMMENT '指导时长（分钟）',
  guidance_type VARCHAR(20) NOT NULL DEFAULT 'GUIDANCE' COMMENT 'GUIDANCE指导 VISIT走访 GROUP座谈 ONLINE线上 OTHER其他',
  content TEXT NOT NULL COMMENT '指导内容',
  attachment_id BIGINT DEFAULT NULL COMMENT '附件ID（可选）',
  student_feedback TEXT DEFAULT NULL COMMENT '学生反馈',
  feedback_time DATETIME DEFAULT NULL COMMENT '学生反馈时间',
  status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT 'DRAFT草稿 PUBLISHED已发布',
  academic_year VARCHAR(20) DEFAULT NULL COMMENT '学年（从选题同步）',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0 COMMENT '0正常 1删除',
  KEY idx_guide_selection (selection_id),
  KEY idx_guide_teacher (teacher_id),
  KEY idx_guide_student (student_id),
  CONSTRAINT fk_guide_sel FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_guide_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user (user_id),
  CONSTRAINT fk_guide_student FOREIGN KEY (student_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指导记录';

CREATE TABLE teacher_feedback (
  fb_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  student_id BIGINT DEFAULT NULL COMMENT '关联选题后自动填充',
  teacher_id BIGINT NOT NULL,
  feedback_type VARCHAR(30) NOT NULL DEFAULT 'OTHER' COMMENT 'TEACHING_QUALITY教学质量 STUDENT_ISSUE学生问题 SYSTEM_IMPROVE系统改进 RESOURCE_LACK资源不足 OTHER其他',
  content TEXT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING待处理 HANDLING处理中 RESOLVED已解决 REJECTED已驳回',
  handler_id BIGINT DEFAULT NULL COMMENT '处理人ID',
  handle_comment TEXT DEFAULT NULL COMMENT '处理意见',
  handle_time DATETIME DEFAULT NULL,
  academic_year VARCHAR(20) DEFAULT NULL COMMENT '学年',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  KEY idx_fb_teacher (teacher_id),
  KEY idx_fb_selection (selection_id),
  KEY idx_fb_status (status),
  KEY idx_fb_type (feedback_type),
  KEY idx_fb_academic_year (academic_year),
  CONSTRAINT fk_fb_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_fb_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师反馈';

CREATE TABLE quality_warning (
  warn_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL COMMENT '发起预警的教师ID',
  student_id BIGINT DEFAULT NULL COMMENT '关联学生ID',
  warn_level TINYINT NOT NULL DEFAULT 1 COMMENT '1提醒 2警告 3严重',
  warn_type VARCHAR(30) NOT NULL DEFAULT 'OTHER' COMMENT 'NO_GUIDANCE长期未指导 DOC_DELAY文档提交滞后 PROGRESS_DELAY进度滞后 LOW_FREQUENCY指导频率不足 OTHER其他',
  reason VARCHAR(500) NOT NULL,
  status VARCHAR(20) DEFAULT 'OPEN' COMMENT 'OPEN已开启 CLOSED已关闭',
  handler_id BIGINT DEFAULT NULL COMMENT '处理人ID',
  handle_comment TEXT DEFAULT NULL COMMENT '处理意见',
  handle_time DATETIME DEFAULT NULL,
  academic_year VARCHAR(20) DEFAULT NULL COMMENT '学年',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  KEY idx_warn_teacher (teacher_id),
  KEY idx_warn_selection (selection_id),
  KEY idx_warn_status (status),
  KEY idx_warn_type (warn_type),
  KEY idx_warn_level (warn_level),
  KEY idx_warn_academic_year (academic_year),
  CONSTRAINT fk_warn_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_warn_handler FOREIGN KEY (handler_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量预警';

-- ---------- 审批意见管理 ----------
CREATE TABLE approval_opinion (
  opinion_id    BIGINT       PRIMARY KEY AUTO_INCREMENT,
  selection_id  BIGINT       NOT NULL COMMENT '关联选题ID',
  doc_id        BIGINT       DEFAULT NULL COMMENT '关联文档ID（可选）',
  doc_type      VARCHAR(50)  DEFAULT NULL COMMENT '文档类型：PROPOSAL/MIDTERM/THESIS/ARCHIVE',
  round_no      INT          NOT NULL DEFAULT 1 COMMENT '评审轮次',
  reviewer_id   BIGINT       NOT NULL COMMENT '评审教师ID',
  score         INT          DEFAULT NULL COMMENT '评分 0-100',
  comment       TEXT         DEFAULT NULL COMMENT '文字批注/意见',
  file_id       BIGINT       DEFAULT NULL COMMENT '附件ID',
  status        VARCHAR(20)  NOT NULL DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED待处理 PASSED通过 FAILED驳回',
  create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  is_deleted    TINYINT      NOT NULL DEFAULT 0,
  KEY idx_opinion_selection (selection_id),
  KEY idx_opinion_reviewer (reviewer_id),
  KEY idx_opinion_doc (doc_type),
  CONSTRAINT fk_opinion_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_opinion_reviewer FOREIGN KEY (reviewer_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批意见管理';

-- ---------- 运维 ----------
DROP TABLE IF EXISTS sys_operation_log;
DROP TABLE IF EXISTS sys_config;

CREATE TABLE sys_config (
  config_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key VARCHAR(100) NOT NULL,
  config_value TEXT,
  remark VARCHAR(255) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数';

CREATE TABLE sys_operation_log (
  log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT DEFAULT NULL,
  username VARCHAR(50) DEFAULT NULL,
  module VARCHAR(100) DEFAULT NULL,
  operation VARCHAR(200) DEFAULT NULL,
  method VARCHAR(20) DEFAULT NULL,
  req_uri VARCHAR(500) DEFAULT NULL,
  req_ip VARCHAR(64) DEFAULT NULL,
  cost_ms INT DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_log_user (user_id),
  KEY idx_log_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- ============================================================
-- 初始化数据
-- ============================================================

INSERT INTO sys_campus (campus_id, campus_name, campus_code) VALUES (1, '校本部', 'MAIN');

INSERT INTO sys_college (college_id, campus_id, college_name, college_code) VALUES (1, 1, '计算机学院', 'CS');
INSERT INTO sys_major (major_id, college_id, major_name, major_code) VALUES (1, 1, '软件工程', 'SE');

-- 密码均为 123456（BCrypt）
INSERT INTO sys_user (user_id, user_name, user_password, real_name, user_type, student_no, teacher_no, college_id, campus_id, campus_name, major_id, user_phone, status) VALUES
(1, 'admin', '$2b$12$FKzjFp99xf/G2PUBnKYuy.lkJUuxmaWuLDReNuvXaF3WsC6meVTFC', '系统管理员', 3, NULL, NULL, NULL, NULL, NULL, NULL, '13800000000', 1),
(2, 'teacher1', '$2b$12$FKzjFp99xf/G2PUBnKYuy.lkJUuxmaWuLDReNuvXaF3WsC6meVTFC', '张教授', 2, NULL, NULL, 1, NULL, '哈尔滨信息工程学院', 1, '13800000001', 1),
(3, 'student1', '$2b$12$FKzjFp99xf/G2PUBnKYuy.lkJUuxmaWuLDReNuvXaF3WsC6meVTFC', '李同学', 1, '2021001', NULL, NULL, 1, '哈尔滨信息工程学院', 1, '13800000002', 1);

INSERT INTO sys_role (role_id, role_name, role_code, user_type, remark) VALUES
(1, '管理员', 'ROLE_ADMIN', 3, '系统管理'),
(2, '指导教师', 'ROLE_TEACHER', 2, '教师'),
(3, '学生', 'ROLE_STUDENT', 1, '学生'),
(4, '待分配', 'ROLE_PENDING', NULL, '新注册未分配权限的用户');

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1), (2, 2), (3, 3);

-- ============================================================
-- 菜单结构（按 design.md 六大模块设计）
-- perm_type: 1=目录 2=菜单 3=按钮 4=接口
-- ============================================================

-- （一）用户与权限管理
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order) VALUES
(1,  0, '用户与权限管理',  'sys',                        1, '/sys',                  NULL,                   'Setting',             1),
(2,  1, '用户管理',        'sys:user',                   2, '/sys/user',             'system/user/index',       'User',                1),
(3,  1, '角色管理',        'sys:role',                   2, '/sys/role',             'system/role/index',       'UserFilled',          2),
(4,  1, '权限管理',        'sys:permission',              2, '/sys/permission',       'system/permission/index', 'Lock',                3),
(5,  1, '消息通知管理',    'sys:notification',            2, '/sys/notification',     'sys/notification/index',  'Bell',                4)
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), perm_name = VALUES(perm_name), perm_code = VALUES(perm_code), perm_type = VALUES(perm_type), path = VALUES(path), component = VALUES(component), icon = VALUES(icon), sort_order = VALUES(sort_order);

-- （二）毕业设计项目管理
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order) VALUES
(10, 0, '毕业设计项目管理','project',                    1, '/project',              NULL,                  'Notebook',            2),
(11, 10,'选题发布管理',    'project:topic',              2, '/project/topic',         'project/topic/index',  'Document',            1),
(12, 10,'选题申请管理',    'project:selection',          2, '/project/selection',      'project/selection/index','EditPen',           2),
(13, 10,'选题审批管理',    'project:approval',           2, '/project/approval',      'project/approval/index','CircleCheck',        3),
(14, 10,'项目进度管理',    'project:progress',           2, '/project/progress',      'project/progress/index', 'TrendCharts',        4),
(15, 10,'中期检查管理',    'project:midterm',            2, '/project/midterm',       'project/midterm/index',  'Finished',           5)
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), perm_name = VALUES(perm_name), perm_code = VALUES(perm_code), perm_type = VALUES(perm_type), path = VALUES(path), component = VALUES(component), icon = VALUES(icon), sort_order = VALUES(sort_order);

-- （三）文档与材料管理
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order) VALUES
(20, 0, '文档与材料管理',  'doc',                        1, '/doc',                  NULL,                  'FolderOpened',        3),
(21, 20,'模板文件管理',    'doc:template',              2, '/doc/template',         'doc/template/index',     'Files',               1),
(22, 20,'开题报告管理',    'doc:proposal',            2, '/doc/proposal',         'doc/proposal/index',      'Reading',             2),
(23, 20,'论文文档管理',    'doc:thesis',            2, '/doc/thesis',         'doc/thesis/index',      'DocumentCopy',        4),
(24, 20,'文档版本管理',    'doc:version',               2, '/doc/version',          'doc/version/index',      'Collection',          4),
(25, 20,'归档材料管理',    'doc:archive',               2, '/doc/archive',          'doc/archive/index',        'Box',                 5);

-- （四）成果与审批管理
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order) VALUES
(30, 0, '成果与审批管理',  'achievement',                1, '/achievement',          NULL,                  'Trophy',              4),
(31, 30,'成果提交管理',    'achievement:submit',         2, '/achievement/submit',   'achievement/submit/index', 'Upload',              1),
(32, 30,'答辩安排管理',    'achievement:defense',        2, '/achievement/defense',  'achievement/defense/index', 'Microphone',          2),
(33, 30,'审批意见管理',    'achievement:approval', 2, '/achievement/approval', 'achievement/approval/index', 'Select',              3),
(34, 30,'成绩评审管理',    'achievement:grade',          2, '/achievement/grade',    'achievement/grade/index',      'Star',                4)
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), perm_name = VALUES(perm_name), perm_code = VALUES(perm_code), perm_type = VALUES(perm_type), path = VALUES(path), component = VALUES(component), icon = VALUES(icon), sort_order = VALUES(sort_order);

-- （五）指导与质量监控管理
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order) VALUES
(40, 0, '指导与质量监控',  'guidance',                   1, '/guidance',             NULL,                  'ChatDotRound',        5),
(41, 40,'指导记录管理',    'guidance:record',           2, '/guidance/record',      'guidance/record/index',   'Memo',                1),
(42, 40,'教师反馈管理',    'guidance:feedback',         2, '/guidance/feedback',    'guidance/feedback/index',  'ChatLineRound',       2),
(43, 40,'质量预警管理',    'guidance:warning',          2, '/guidance/warning',     'guidance/warning/index',     'Warning',             3),
(44, 40,'指导关系管理',    'guidance:relation',          2, '/guidance/relation',    NULL,                  'Connection',          4)
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), perm_name = VALUES(perm_name), perm_code = VALUES(perm_code), perm_type = VALUES(perm_type), path = VALUES(path), component = VALUES(component), icon = VALUES(icon), sort_order = VALUES(sort_order);

-- （六）系统运维管理
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order) VALUES
(50, 0, '系统运维管理',   'sysops',                     1, '/sysops',               NULL,                  'Tools',               6),
(51, 50,'系统参数管理',   'sysops:param',              2, '/sysops/param',         NULL,                  'Setting',             1),
(52, 50,'系统监控管理',   'sysops:monitor',            2, '/sysops/monitor',       NULL,                  'Monitor',             2),
(53, 50,'版本更新管理',   'sysops:version',            2, '/sysops/version',       NULL,                  'Refresh',             3),
(54, 50,'操作日志管理',   'sysops:log',                2, '/sysops/log',           NULL,                  'List',                4)
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), perm_name = VALUES(perm_name), perm_code = VALUES(perm_code), perm_type = VALUES(perm_type), path = VALUES(path), component = VALUES(component), icon = VALUES(icon), sort_order = VALUES(sort_order);

-- 首页（Dashboard）
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order) VALUES
(55, 0, '首页',           'dashboard',                 2, '/dashboard',            'Dashboard',           'HomeFilled',          0);

-- ============================================================
-- 接口级权限（按钮/接口级别，perm_type=4，后端接口鉴权用）
-- ============================================================
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order) VALUES
(101, 2, '用户列表',   'sys:user:list',   4, NULL, NULL, NULL, 1),
(102, 2, '用户新增',   'sys:user:add',    4, NULL, NULL, NULL, 2),
(103, 2, '用户编辑',   'sys:user:edit',   4, NULL, NULL, NULL, 3),
(104, 2, '用户删除',   'sys:user:del',    4, NULL, NULL, NULL, 4),
(105, 3, '角色列表',   'sys:role:list',   4, NULL, NULL, NULL, 1),
(106, 3, '角色新增',   'sys:role:add',    4, NULL, NULL, NULL, 2),
(107, 3, '角色编辑',   'sys:role:edit',   4, NULL, NULL, NULL, 3),
(108, 3, '角色删除',   'sys:role:del',    4, NULL, NULL, NULL, 4),
(109, 3, '分配权限',   'sys:role:assign', 4, NULL, NULL, NULL, 5),
(110, 11,'课题发布',   'project:topic:add',    4, NULL, NULL, NULL, 1),
(111, 11,'课题编辑',   'project:topic:edit',   4, NULL, NULL, NULL, 2),
(112, 11,'课题删除',   'project:topic:del',    4, NULL, NULL, NULL, 3),
(113, 12,'提交申请',   'project:selection:apply', 4, NULL, NULL, NULL, 1),
(114, 12,'撤回申请',   'project:selection:recall',4, NULL, NULL, NULL, 2),
(115, 13,'通过选题',   'project:approval:pass',  4, NULL, NULL, NULL, 1),
(116, 13,'驳回选题',   'project:approval:reject',4, NULL, NULL, NULL, 2),
(117, 15,'提交中期报告','project:midterm:submit', 4, NULL, NULL, NULL, 1),
(118, 15,'审核中期检查','project:midterm:review', 4, NULL, NULL, NULL, 2),
(119, 21,'上传模板',   'doc:template:upload',  4, NULL, NULL, NULL, 1),
(120, 22,'提交开题报告','doc:proposal:submit',  4, NULL, NULL, NULL, 1),
(122, 23,'上传论文',   'doc:thesis:upload',    4, NULL, NULL, NULL, 1),
(124, 25,'归档',       'doc:archive:archive', 4, NULL, NULL, NULL, 1),
(125, 31,'成果管理',   'achievement:submit:manage', 4, NULL, NULL, NULL, 1),
(126, 32,'安排答辩',   'achievement:defense:arrange',4, NULL, NULL, NULL, 1),
(127, 33,'审批',       'achievement:approval:do',  4, NULL, NULL, NULL, 1),
(128, 33,'审批管理',  'achievement:approval:manage',4, NULL, NULL, NULL, 0),
(129, 35,'评定优秀',   'achievement:excellent:add', 4, NULL, NULL, NULL, 1),
(130, 41,'添加记录',   'guidance:record:add',   4, NULL, NULL, NULL, 1),
(131, 41,'查看记录',   'guidance:record:view',  4, NULL, NULL, NULL, 2),
(132, 41,'编辑记录',   'guidance:record:edit',  4, NULL, NULL, NULL, 3),
(133, 41,'删除记录',   'guidance:record:del',   4, NULL, NULL, NULL, 4),
(134, 44,'分配指导教师','guidance:relation:assign',4, NULL, NULL, NULL, 1),
(135, 42,'教师反馈',   'guidance:feedback:add', 4, NULL, NULL, NULL, 1),
(136, 14,'进度列表',   'project:progress:list', 4, NULL, NULL, NULL, 1),
(137, 41,'填写反馈',   'guidance:record:feedback', 4, NULL, NULL, NULL, 5),
(138, 14,'删除进度',   'project:progress:del',  4, NULL, NULL, NULL, 4),
(139, 21,'模板列表',   'doc:template:list',   4, NULL, NULL, NULL, 2),
(140, 21,'编辑模板',   'doc:template:edit',  4, NULL, NULL, NULL, 3),
(141, 11,'课题列表',   'project:topic:list',    4, NULL, NULL, NULL, 4),
(142, 14,'添加进度',   'project:progress:add',  4, NULL, NULL, NULL, 2),
(143, 22,'审核开题报告','doc:proposal:review', 4, NULL, NULL, NULL, 2),
(144, 15,'查看中期',   'project:midterm:view', 4, NULL, NULL, NULL, 3),
(145, 12,'选题列表',   'project:selection:list', 4, NULL, NULL, NULL, 3),
(146, 21,'删除模板',   'doc:template:del',    4, NULL, NULL, NULL, 4),
(147, 21,'下载模板',   'doc:template:download',4, NULL, NULL, NULL, 5),
(148, 23,'提交论文',   'doc:thesis:submit',    4, NULL, NULL, NULL, 2),
(149, 23,'审核论文',   'doc:thesis:review',   4, NULL, NULL, NULL, 3),
(150, 24,'上传版本',  'doc:version:submit',  4, NULL, NULL, NULL, 1),
(151, 24,'审核版本',  'doc:version:review',  4, NULL, NULL, NULL, 2),
(152, 24,'归档列表',   'doc:archive:list',  4, NULL, NULL, NULL, 1),
(153, 24,'上传归档',   'doc:archive:submit', 4, NULL, NULL, NULL, 2),
(154, 24,'撤回归档',   'doc:archive:recall', 4, NULL, NULL, NULL, 3),
(155, 24,'审核归档',   'doc:archive:review', 4, NULL, NULL, NULL, 4),
(156, 24,'下载归档',   'doc:archive:download',4, NULL, NULL, NULL, 5),
(157, 31,'我的成果列表','achievement:submit:list',4, NULL, NULL, NULL, 1),
(158, 31,'成果上传',   'achievement:submit:upload', 4, NULL, NULL, NULL, 2),
(159, 31,'成果删除',   'achievement:submit:del', 4, NULL, NULL, NULL, 3),
(160, 31,'成果下载',   'achievement:submit:download',4, NULL, NULL, NULL, 4),
(161, 32,'答辩列表',  'achievement:defense:list',  4, NULL, NULL, NULL, 1),
(162, 32,'查看明细',  'achievement:defense:detail', 4, NULL, NULL, NULL, 2),
(163, 33,'查看意见',  'achievement:approval:view', 4, NULL, NULL, NULL, 1),
(164, 33,'添加意见',  'achievement:approval:add',  4, NULL, NULL, NULL, 2),
(165, 33,'编辑意见',  'achievement:approval:edit', 4, NULL, NULL, NULL, 3),
(166, 33,'删除意见',  'achievement:approval:del',  4, NULL, NULL, NULL, 4),
(167, 33,'更新状态',  'achievement:approval:status',4, NULL, NULL, NULL, 5),
(168, 33,'审批管理',  'achievement:approval:manage',4, NULL, NULL, NULL, 0),
(169, 34,'成绩查看',  'achievement:grade:view',   4, NULL, NULL, NULL, 1),
(170, 34,'录入成绩',  'achievement:grade:input',  4, NULL, NULL, NULL, 2),
(171, 34,'调整成绩',  'achievement:grade:adjust',4, NULL, NULL, NULL, 3),
(172, 34,'锁定成绩',  'achievement:grade:lock',  4, NULL, NULL, NULL, 4),
(173, 34,'成绩管理',  'achievement:grade:manage',4, NULL, NULL, NULL, 0),
(174, 43,'发起预警',  'guidance:warning:add',  4, NULL, NULL, NULL, 1),
(175, 43,'处理预警',  'guidance:warning:handle',4, NULL, NULL, NULL, 2),
(176, 43,'删除预警',  'guidance:warning:del',  4, NULL, NULL, NULL, 3),
(177, 42,'处理反馈',  'guidance:feedback:handle',4, NULL, NULL, NULL, 2)
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), perm_name = VALUES(perm_name), perm_code = VALUES(perm_code), perm_type = VALUES(perm_type), path = VALUES(path), component = VALUES(component), icon = VALUES(icon), sort_order = VALUES(sort_order);

-- ============================================================
-- 角色权限分配
-- ============================================================

-- 管理员：拥有全部菜单和接口权限
INSERT IGNORE INTO sys_role_permission (role_id, perm_id)
SELECT 1, perm_id FROM sys_permission;

-- 教师：用户与权限管理（只读目录），毕业设计项目管理（全部），文档材料（全部），成果审批（全部），指导监控（全部），运维（只读日志），首页
INSERT IGNORE INTO sys_role_permission (role_id, perm_id)
SELECT 2, perm_id FROM sys_permission WHERE perm_id IN
(1, 5, 10, 11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34, 35, 40, 41, 42, 43, 44, 50, 54, 55)
UNION
SELECT 2, perm_id FROM sys_permission WHERE perm_id IN
(101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 122, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173);

-- 学生：只能访问与自己相关的模块（选题浏览、选题申请、项目进度、中期检查、文档、成果、指导）
INSERT IGNORE INTO sys_role_permission (role_id, perm_id)
SELECT 3, perm_id FROM sys_permission WHERE perm_id IN
(10, 11, 12, 14, 15, 20, 21, 22, 23, 25, 30, 31, 33, 34, 35, 40, 41, 42, 43)
UNION
SELECT 3, perm_id FROM sys_permission WHERE perm_id IN
(113, 114, 117, 120, 122, 127, 129, 130, 131, 132, 133, 134, 136, 137, 138, 139, 143, 144, 145, 146, 147, 148, 150, 152, 153, 154, 156, 157, 158, 159, 160, 161, 163, 169);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 菜单及权限配置：优秀成果管理
-- ============================================================
-- 优秀成果管理（整合到成果与审批管理下，父级 perm_id=30）
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order)
VALUES (202, 30, '优秀成果管理', 'achievement:excellent:list', 2, '/achievement/excellent', 'achievement/excellent/index', 'Medal', 5)
ON DUPLICATE KEY UPDATE parent_id = 30, perm_name = '优秀成果管理', perm_code = 'achievement:excellent:list', path = '/achievement/excellent', component = 'achievement/excellent/index', icon = 'Medal', sort_order = 5;

-- 管理员拥有优秀成果管理权限
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (1, 202);

-- 教师拥有优秀成果管理权限（查看、认定、导出）
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (2, 202);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 指导关系管理与答辩小组管理
-- ============================================================

-- 指导关系表：维护学生与指导教师之间的正式指导关系
DROP TABLE IF EXISTS guidance_relation;
CREATE TABLE guidance_relation (
    relation_id     BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    student_id      BIGINT NOT NULL COMMENT '学生ID',
    student_name    VARCHAR(50) DEFAULT NULL COMMENT '学生姓名',
    student_no      VARCHAR(32) DEFAULT NULL COMMENT '学号',
    teacher_id      BIGINT NOT NULL COMMENT '指导教师ID',
    teacher_name    VARCHAR(50) DEFAULT NULL COMMENT '教师姓名',
    teacher_no      VARCHAR(32) DEFAULT NULL COMMENT '工号',
    campus_id       BIGINT DEFAULT NULL COMMENT '学校ID',
    campus_name     VARCHAR(100) DEFAULT NULL COMMENT '学校名称',
    academic_year   VARCHAR(20) DEFAULT NULL COMMENT '学年',
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE有效 TERMINATED已解除',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_by_name  VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    KEY idx_grd_student (student_id),
    KEY idx_grd_teacher (teacher_id),
    KEY idx_grd_year (academic_year),
    KEY idx_grd_campus (campus_id),
    KEY idx_grd_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指导关系表';

-- 指导关系申请表：双向申请流程
DROP TABLE IF EXISTS guidance_relation_apply;
CREATE TABLE guidance_relation_apply (
    apply_id        BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
    from_user_id    BIGINT NOT NULL COMMENT '申请方用户ID',
    from_user_name  VARCHAR(50) DEFAULT NULL COMMENT '申请方姓名',
    from_user_type  VARCHAR(20) NOT NULL COMMENT '申请方类型：TEACHER教师 STUDENT学生',
    to_user_id      BIGINT NOT NULL COMMENT '接收方用户ID',
    to_user_name    VARCHAR(50) DEFAULT NULL COMMENT '接收方姓名',
    to_user_type    VARCHAR(20) NOT NULL COMMENT '接收方类型：TEACHER教师 STUDENT学生',
    campus_id       BIGINT DEFAULT NULL COMMENT '学校ID',
    campus_name     VARCHAR(100) DEFAULT NULL COMMENT '学校名称',
    academic_year   VARCHAR(20) DEFAULT NULL COMMENT '学年',
    message         VARCHAR(500) DEFAULT NULL COMMENT '申请留言',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING待处理 APPROVED已同意 REJECTED已拒绝 CANCELLED已取消 EXPIRED已过期',
    handle_by       BIGINT DEFAULT NULL COMMENT '处理人ID',
    handle_by_name  VARCHAR(50) DEFAULT NULL COMMENT '处理人姓名',
    handle_time     DATETIME DEFAULT NULL COMMENT '处理时间',
    handle_comment  VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    KEY idx_gra_from (from_user_id),
    KEY idx_gra_to (to_user_id),
    KEY idx_gra_status (status),
    KEY idx_gra_year (academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指导关系申请表';

-- 答辩小组表：教师创建的答辩评审小组
DROP TABLE IF EXISTS defense_group;
CREATE TABLE defense_group (
    group_id        BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '小组ID',
    group_name      VARCHAR(200) NOT NULL COMMENT '小组名称',
    leader_id       BIGINT NOT NULL COMMENT '组长ID',
    leader_name     VARCHAR(50) DEFAULT NULL COMMENT '组长姓名',
    campus_id       BIGINT DEFAULT NULL COMMENT '学校ID',
    campus_name     VARCHAR(100) DEFAULT NULL COMMENT '学校名称',
    academic_year   VARCHAR(20) DEFAULT NULL COMMENT '学年',
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE有效 DISSOLVED已解散',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_by_name  VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    KEY idx_dg_leader (leader_id),
    KEY idx_dg_campus (campus_id),
    KEY idx_dg_year (academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答辩小组表';

-- 答辩小组成员表：包含教师成员和学生成员
DROP TABLE IF EXISTS defense_group_member;
CREATE TABLE defense_group_member (
    member_id       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成员ID',
    group_id        BIGINT NOT NULL COMMENT '小组ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    user_name       VARCHAR(50) DEFAULT NULL COMMENT '用户姓名',
    user_type       VARCHAR(20) NOT NULL COMMENT 'TEACHER教师 STUDENT学生',
    campus_id       BIGINT DEFAULT NULL COMMENT '学校ID',
    campus_name     VARCHAR(100) DEFAULT NULL COMMENT '学校名称',
    create_by       BIGINT DEFAULT NULL COMMENT '添加人ID',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '0正常 1删除',
    KEY idx_dgm_group (group_id),
    KEY idx_dgm_user (user_id),
    KEY idx_dgm_type (user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答辩小组成员表';

-- ============================================================
-- 菜单及权限配置：指导关系管理 + 答辩小组管理
-- ============================================================

-- 指导关系管理（父级 perm_id=30）
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order)
VALUES (203, 30, '指导关系管理', 'guidance:relation:list', 2, '/guidance/relation', 'guidance/relation/index', 'Connection', 6)
ON DUPLICATE KEY UPDATE parent_id = 30, perm_name = '指导关系管理', perm_code = 'guidance:relation:list', path = '/guidance/relation', component = 'guidance/relation/index', icon = 'Connection', sort_order = 6;

-- 指导关系按钮权限
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, sort_order)
VALUES
(204, 203, '查看', 'guidance:relation:view', 4, 1),
(205, 203, '添加/申请', 'guidance:relation:add', 4, 2),
(206, 203, '编辑', 'guidance:relation:edit', 4, 3),
(207, 203, '删除/解绑', 'guidance:relation:del', 4, 4),
(213, 203, '处理申请', 'guidance:relation:handle', 4, 5)
ON DUPLICATE KEY UPDATE parent_id = 203;

-- 管理员拥有全部权限
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (1, 203);
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (1, 204);
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (1, 205);
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (1, 206);
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (1, 207);
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (1, 213);

-- 教师拥有指导关系管理权限
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (2, 203);
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (2, 204);
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (2, 205);
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (2, 206);
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (2, 207);
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (2, 213);

-- 学生拥有查看自己指导关系权限
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (3, 203);
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (3, 204);
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (3, 205);
INSERT IGNORE INTO sys_role_permission (role_id, perm_id) VALUES (3, 207);

SET FOREIGN_KEY_CHECKS = 1;
