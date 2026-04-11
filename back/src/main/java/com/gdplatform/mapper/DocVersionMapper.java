package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.VersionPageReq;
import com.gdplatform.dto.VersionResp;
import com.gdplatform.entity.DocVersion;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocVersionMapper extends BaseMapper<DocVersion> {

    @Select("SELECT * FROM doc_version WHERE version_id = #{versionId} AND is_deleted = 0")
    DocVersion selectByVersionId(@Param("versionId") Long versionId);

    @Select("SELECT * FROM doc_version WHERE selection_id = #{selectionId} AND stage_name = #{stageName} AND is_deleted = 0 ORDER BY version_no DESC LIMIT 1")
    DocVersion selectLatestByStage(@Param("selectionId") Long selectionId, @Param("stageName") String stageName);

    List<VersionResp> selectVersionPage(@Param("req") VersionPageReq req, @Param("offset") long offset, @Param("limit") long limit);

    long countVersionPage(@Param("req") VersionPageReq req);

    List<VersionResp> selectVersionPageForTeacher(@Param("req") VersionPageReq req, @Param("offset") long offset, @Param("limit") long limit);

    long countVersionPageForTeacher(@Param("req") VersionPageReq req);

    @Update("UPDATE doc_version SET is_deleted = 1 WHERE version_id = #{versionId}")
    void deleteByVersionId(@Param("versionId") Long versionId);
}
