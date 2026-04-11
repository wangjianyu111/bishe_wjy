package com.gdplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_version")
public class SysVersion {

    @TableId(type = IdType.AUTO)
    private Long versionId;

    /** 版本号，如 1.0.0 */
    private String versionCode;

    /** 版本名称，如 V1.0.0 正式版 */
    private String versionName;

    /** 应用类型：FRONTEND 后台前端 / BACKEND 后端服务 / APP 移动端 */
    private String appType;

    /** 状态：DEVELOPING 开发中 / RELEASED 已发布 / ROLLBACKED 已回滚 / DEPRECATED 已废弃 */
    private String status;

    /** 发布类型：STABLE 正式版 / BETA 测试版 / CANARY 灰度版 */
    private String releaseType;

    /** 灰度发布百分比 0-100 */
    private Integer rolloutPercent;

    /** 更新日志 */
    private String changelog;

    /** 下载/访问地址 */
    private String downloadUrl;

    /** 最低兼容版本 */
    private String minCompatibleVersion;

    /** 新功能列表（JSON 数组） */
    private String featureList;

    /** 部署人用户ID */
    private Long deployedBy;

    /** 部署人姓名 */
    private String deployedByName;

    /** 部署时间 */
    private LocalDateTime deployedTime;

    /** 是否强制更新：0 否 1 是 */
    private Integer forceUpdate;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
