package com.gdplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VersionRolloutReq {

    @NotNull(message = "版本ID不能为空")
    private Long versionId;

    /** 操作类型：GRAY 灰度发布 / ROLLBACK 回滚 / FULL 全量发布 */
    @NotNull(message = "操作类型不能为空")
    private String rolloutType;

    /** 灰度发布百分比，仅 GRAY 时有效 */
    private Integer rolloutPercent = 100;

    private String remark;
}
