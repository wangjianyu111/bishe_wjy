CREATE DATABASE IF NOT EXISTS gd_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gd_platform;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 大学生毕业设计审批一体化平台 - MySQL 8 数据库脚本
-- 说明：密码字段存 BCrypt；业务主键统一 BIGINT；逻辑删除用 is_deleted
-- ============================================================

-- ---------- 学院 / 专业（组织维度）----------
DROP TABLE IF EXISTS sys_major;
DROP TABLE IF EXISTS sys_college;
CREATE TABLE sys_college (
  college_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学院ID',
  college_name VARCHAR(100) NOT NULL COMMENT '学院名称',
  college_code VARCHAR(50) DEFAULT NULL COMMENT '学院编码',
  sort_order INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0 COMMENT '0正常 1删除'
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
  teacher_id BIGINT NOT NULL COMMENT '指导教师',
  academic_year VARCHAR(20) NOT NULL COMMENT '学年 如2024-2025',
  max_students INT NOT NULL DEFAULT 1,
  current_count INT NOT NULL DEFAULT 0 COMMENT '已确认人数',
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN已发布 CLOSED关闭 DRAFT草稿',
  description TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  KEY idx_topic_teacher (teacher_id),
  KEY idx_topic_year (academic_year),
  CONSTRAINT fk_topic_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='毕业设计课题';

CREATE TABLE project_selection (
  selection_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  topic_id BIGINT NOT NULL,
  academic_year VARCHAR(20) NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING待审 APPROVED通过 REJECTED驳回 WITHDRAWN撤回',
  apply_reason VARCHAR(500) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  UNIQUE KEY uk_student_year (student_id, academic_year),
  KEY idx_sel_topic (topic_id),
  CONSTRAINT fk_sel_student FOREIGN KEY (student_id) REFERENCES sys_user (user_id),
  CONSTRAINT fk_sel_topic FOREIGN KEY (topic_id) REFERENCES project_topic (topic_id)
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
  KEY idx_prog_selection (selection_id),
  CONSTRAINT fk_prog_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目进度';

CREATE TABLE project_mid_term (
  mid_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  report_content TEXT,
  file_id BIGINT DEFAULT NULL,
  status VARCHAR(20) DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED已交 PENDING待审 PASSED通过 FAILED不通过',
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

CREATE TABLE doc_version (
  version_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  file_id BIGINT NOT NULL COMMENT '当前文件',
  prev_file_id BIGINT DEFAULT NULL,
  version_no INT NOT NULL,
  remark VARCHAR(255) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_dv_file (file_id),
  CONSTRAINT fk_dv_file FOREIGN KEY (file_id) REFERENCES doc_file (file_id),
  CONSTRAINT fk_dv_prev FOREIGN KEY (prev_file_id) REFERENCES doc_file (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档版本链';

CREATE TABLE proposal_report (
  proposal_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  file_id BIGINT DEFAULT NULL,
  status VARCHAR(20) DEFAULT 'DRAFT' COMMENT 'DRAFT草稿 SUBMITTED已提交 APPROVED通过 REJECTED驳回',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_proposal_selection (selection_id),
  CONSTRAINT fk_prop_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_prop_file FOREIGN KEY (file_id) REFERENCES doc_file (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开题报告';

CREATE TABLE thesis_document (
  thesis_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  file_id BIGINT DEFAULT NULL,
  status VARCHAR(20) DEFAULT 'DRAFT',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_thesis_selection (selection_id),
  CONSTRAINT fk_thesis_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_thesis_file FOREIGN KEY (file_id) REFERENCES doc_file (file_id)
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
  session_name VARCHAR(200) NOT NULL,
  defense_date DATE NOT NULL,
  start_time TIME DEFAULT NULL,
  end_time TIME DEFAULT NULL,
  location VARCHAR(200) DEFAULT NULL,
  academic_year VARCHAR(20) NOT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答辩场次';

CREATE TABLE defense_arrangement (
  arrange_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  selection_id BIGINT NOT NULL,
  sort_order INT DEFAULT 0,
  status VARCHAR(20) DEFAULT 'SCHEDULED',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_defense_session_sel (session_id, selection_id),
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

CREATE TABLE grade_evaluation (
  grade_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  regular_score DECIMAL(5,2) DEFAULT NULL COMMENT '平时',
  thesis_score DECIMAL(5,2) DEFAULT NULL COMMENT '论文',
  defense_score DECIMAL(5,2) DEFAULT NULL COMMENT '答辩',
  total_score DECIMAL(5,2) DEFAULT NULL,
  grade_level VARCHAR(20) DEFAULT NULL COMMENT '优/良/中/及格/不及格',
  evaluator_id BIGINT DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_grade_selection (selection_id),
  CONSTRAINT fk_grade_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_grade_eval FOREIGN KEY (evaluator_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩评审';

CREATE TABLE excellent_achievement (
  ex_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  academic_year VARCHAR(20) NOT NULL,
  level_type VARCHAR(50) DEFAULT 'SCHOOL' COMMENT '校优/省优等',
  remark VARCHAR(500) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ex_selection (selection_id),
  CONSTRAINT fk_ex_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优秀成果';

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
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_guidance (selection_id, teacher_id, rel_type),
  CONSTRAINT fk_gr_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_gr_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指导关系';

CREATE TABLE guidance_record (
  guide_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  guide_time DATETIME NOT NULL,
  place VARCHAR(200) DEFAULT NULL,
  content TEXT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_guide_selection (selection_id),
  CONSTRAINT fk_guide_sel FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_guide_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user (user_id),
  CONSTRAINT fk_guide_student FOREIGN KEY (student_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指导记录';

CREATE TABLE teacher_feedback (
  fb_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  feedback_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_fb_selection (selection_id),
  CONSTRAINT fk_fb_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_fb_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师反馈';

CREATE TABLE quality_warning (
  warn_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  selection_id BIGINT NOT NULL,
  warn_level TINYINT NOT NULL DEFAULT 1 COMMENT '1提醒 2警告 3严重',
  reason VARCHAR(500) NOT NULL,
  status VARCHAR(20) DEFAULT 'OPEN' COMMENT 'OPEN CLOSED',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  handle_time DATETIME DEFAULT NULL,
  handler_id BIGINT DEFAULT NULL,
  KEY idx_warn_selection (selection_id),
  CONSTRAINT fk_warn_selection FOREIGN KEY (selection_id) REFERENCES project_selection (selection_id),
  CONSTRAINT fk_warn_handler FOREIGN KEY (handler_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量预警';

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

INSERT INTO sys_college (college_id, college_name, college_code) VALUES (1, '计算机学院', 'CS');
INSERT INTO sys_major (major_id, college_id, major_name, major_code) VALUES (1, 1, '软件工程', 'SE');

-- 密码均为 123456（BCrypt）
INSERT INTO sys_user (user_id, user_name, user_password, real_name, user_type, student_no, teacher_no, college_id, major_id, user_phone, status) VALUES
(1, 'admin', '$2b$12$FKzjFp99xf/G2PUBnKYuy.lkJUuxmaWuLDReNuvXaF3WsC6meVTFC', '系统管理员', 3, NULL, NULL, NULL, NULL, '13800000000', 1),
(2, 'teacher1', '$2b$12$FKzjFp99xf/G2PUBnKYuy.lkJUuxmaWuLDReNuvXaF3WsC6meVTFC', '张教授', 2, NULL, 'T001', 1, NULL, '13800000001', 1),
(3, 'student1', '$2b$12$FKzjFp99xf/G2PUBnKYuy.lkJUuxmaWuLDReNuvXaF3WsC6meVTFC', '李同学', 1, '2021001', NULL, 1, 1, '13800000002', 1);

INSERT INTO sys_role (role_id, role_name, role_code, remark) VALUES
(1, '管理员', 'ROLE_ADMIN', '系统管理'),
(2, '指导教师', 'ROLE_TEACHER', '教师'),
(3, '学生', 'ROLE_STUDENT', '学生');

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
(5,  1, '消息通知管理',    'sys:notification',            2, '/sys/notification',     NULL,                  'Bell',                4);

-- （二）毕业设计项目管理
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order) VALUES
(10, 0, '毕业设计项目管理','project',                    1, '/project',              NULL,                  'Notebook',            2),
(11, 10,'选题发布管理',    'project:topic',              2, '/project/topic',         'project/topic/index',  'Document',            1),
(12, 10,'选题申请管理',    'project:selection',          2, '/project/selection',      'project/selection/index','EditPen',           2),
(13, 10,'选题审批管理',    'project:approval',           2, '/project/approval',      'project/approval/index','CircleCheck',        3),
(14, 10,'项目进度管理',    'project:progress',           2, '/project/progress',      NULL,                  'TrendCharts',        4),
(15, 10,'中期检查管理',    'project:midterm',            2, '/project/midterm',       NULL,                  'Finished',           5);

-- （三）文档与材料管理
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order) VALUES
(20, 0, '文档与材料管理',  'doc',                        1, '/doc',                  NULL,                  'FolderOpened',        3),
(21, 20,'模板文件管理',    'doc:template',              2, '/doc/template',         NULL,                  'Files',               1),
(22, 20,'开题报告管理',    'doc:proposal',              2, '/doc/proposal',         NULL,                  'Reading',             2),
(23, 20,'论文文档管理',    'doc:thesis',               2, '/doc/thesis',           NULL,                  'DocumentCopy',        3),
(24, 20,'文档版本管理',    'doc:version',              2, '/doc/version',          NULL,                  'Collection',          4),
(25, 20,'归档材料管理',    'doc:archive',              2, '/doc/archive',          NULL,                  'Box',                 5);

-- （四）成果与审批管理
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order) VALUES
(30, 0, '成果与审批管理',  'achievement',                1, '/achievement',          NULL,                  'Trophy',              4),
(31, 30,'成果提交管理',    'achievement:submit',         2, '/achievement/submit',   NULL,                  'Upload',              1),
(32, 30,'答辩安排管理',    'achievement:defense',        2, '/achievement/defense',  NULL,                  'Microphone',          2),
(33, 30,'审批意见管理',    'achievement:approval',       2, '/achievement/approval', NULL,                  'Select',              3),
(34, 30,'成绩评审管理',    'achievement:grade',          2, '/achievement/grade',    NULL,                  'Star',                4),
(35, 30,'优秀成果管理',    'achievement:excellent',      2, '/achievement/excellent',NULL,                  'Medal',               5);

-- （五）指导与质量监控管理
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order) VALUES
(40, 0, '指导与质量监控',  'guidance',                   1, '/guidance',             NULL,                  'ChatDotRound',        5),
(41, 40,'指导记录管理',    'guidance:record',           2, '/guidance/record',      NULL,                  'Memo',                1),
(42, 40,'教师反馈管理',    'guidance:feedback',         2, '/guidance/feedback',    NULL,                  'ChatLineRound',       2),
(43, 40,'质量预警管理',    'guidance:warning',          2, '/guidance/warning',     NULL,                  'Warning',             3),
(44, 40,'指导关系管理',    'guidance:relation',          2, '/guidance/relation',    NULL,                  'Connection',          4);

-- （六）系统运维管理
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order) VALUES
(50, 0, '系统运维管理',   'sysops',                     1, '/sysops',               NULL,                  'Tools',               6),
(51, 50,'系统参数管理',   'sysops:param',              2, '/sysops/param',         NULL,                  'Setting',             1),
(52, 50,'系统监控管理',   'sysops:monitor',            2, '/sysops/monitor',       NULL,                  'Monitor',             2),
(53, 50,'版本更新管理',   'sysops:version',            2, '/sysops/version',       NULL,                  'Refresh',             3),
(54, 50,'操作日志管理',   'sysops:log',                2, '/sysops/log',           NULL,                  'List',                4);

-- ============================================================
-- 接口级权限（按钮/接口级别，perm_type=4，后端接口鉴权用）
-- ============================================================
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, sort_order) VALUES
-- 用户管理
(101, 2, '用户列表',   'sys:user:list',   4, 1),
(102, 2, '用户新增',   'sys:user:add',    4, 2),
(103, 2, '用户编辑',   'sys:user:edit',   4, 3),
(104, 2, '用户删除',   'sys:user:del',    4, 4),
-- 角色管理
(105, 3, '角色列表',   'sys:role:list',   4, 1),
(106, 3, '角色新增',   'sys:role:add',    4, 2),
(107, 3, '角色编辑',   'sys:role:edit',   4, 3),
(108, 3, '角色删除',   'sys:role:del',    4, 4),
(109, 3, '分配权限',   'sys:role:assign', 4, 5),
-- 课题管理
(110, 11,'课题发布',   'project:topic:add',    4, 1),
(111, 11,'课题编辑',   'project:topic:edit',   4, 2),
(112, 11,'课题删除',   'project:topic:del',    4, 3),
(133, 11,'课题列表',   'project:topic:list',    4, 4),
-- 选题管理
(113, 12,'提交申请',   'project:selection:apply', 4, 1),
(114, 12,'撤回申请',   'project:selection:recall',4, 2),
(134, 12,'选题列表',   'project:selection:list', 4, 3),
-- 审批管理
(115, 13,'通过选题',   'project:approval:pass',  4, 1),
(116, 13,'驳回选题',   'project:approval:reject',4, 2),
-- 中期检查
(117, 15,'提交中期报告','project:midterm:submit', 4, 1),
(118, 15,'审核中期检查','project:midterm:review', 4, 2),
-- 文档
(119, 21,'上传模板',   'doc:template:upload',  4, 1),
(120, 22,'提交开题报告','doc:proposal:submit',  4, 1),
(121, 23,'上传论文',   'doc:thesis:upload',    4, 1),
(122, 24,'查看版本',   'doc:version:view',    4, 1),
(123, 25,'归档',       'doc:archive:archive', 4, 1),
-- 成果
(124, 31,'提交成果',   'achievement:submit:add', 4, 1),
(125, 32,'安排答辩',   'achievement:defense:arrange',4,1),
(126, 33,'审批',       'achievement:approval:do',  4, 1),
(127, 34,'录入成绩',   'achievement:grade:input',   4, 1),
(128, 35,'评定优秀',   'achievement:excellent:add', 4, 1),
-- 指导
(129, 41,'添加记录',   'guidance:record:add',   4, 1),
(130, 42,'教师反馈',   'guidance:feedback:add', 4, 1),
(131, 43,'发起预警',   'guidance:warning:add',  4, 1),
(132, 44,'分配指导教师','guidance:relation:assign',4,1);

-- ============================================================
-- 角色权限分配
-- ============================================================

-- 管理员：拥有全部菜单和接口权限
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 1, perm_id FROM sys_permission;

-- 教师：用户与权限管理（只读目录），毕业设计项目管理（全部），文档材料（全部），成果审批（全部），指导监控（全部），运维（只读日志）
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 2, perm_id FROM sys_permission WHERE perm_id IN
(1, 5, 10, 11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34, 35, 40, 41, 42, 43, 44, 50, 54)
UNION
SELECT 2, perm_id FROM sys_permission WHERE perm_id IN
(101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134);

-- 学生：只能访问与自己相关的模块（选题浏览、选题申请、项目进度、中期检查、文档、成果、指导）
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 3, perm_id FROM sys_permission WHERE perm_id IN
(10, 11, 12, 14, 15, 20, 21, 22, 23, 25, 30, 31, 34, 35, 40, 41, 42, 43)
UNION
SELECT 3, perm_id FROM sys_permission WHERE perm_id IN
(113, 114, 117, 120, 121, 122, 123, 127, 129, 130, 131, 133, 134);

INSERT INTO sys_config (config_key, config_value, remark) VALUES
('system.name', '大学生毕业设计审批一体化平台', '系统名称'),
('file.upload.path', './uploads', '本地上传目录');

INSERT INTO project_topic (topic_id, topic_name, teacher_id, academic_year, max_students, current_count, status, description) VALUES
(1, '基于SpringBoot的毕设管理系统设计与实现', 2, '2024-2025', 1, 0, 'OPEN', 'Web全栈');

SET FOREIGN_KEY_CHECKS = 1;
