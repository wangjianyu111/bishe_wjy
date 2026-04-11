package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.dto.ArchivePageReq;
import com.gdplatform.dto.ArchiveResp;
import com.gdplatform.entity.DocArchive;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocArchiveMapper extends BaseMapper<DocArchive> {

    @Select("SELECT * FROM doc_archive WHERE archive_id = #{archiveId} AND is_deleted = 0")
    DocArchive selectByArchiveId(@Param("archiveId") Long archiveId);

    @Select("SELECT * FROM doc_archive WHERE selection_id = #{selectionId} AND stage_name = #{stageName} AND is_deleted = 0 ORDER BY create_time DESC LIMIT 1")
    DocArchive selectBySelectionAndStage(@Param("selectionId") Long selectionId, @Param("stageName") String stageName);

    List<ArchiveResp> selectArchivePage(@Param("req") ArchivePageReq req, @Param("offset") long offset, @Param("limit") long limit);

    long countArchivePage(@Param("req") ArchivePageReq req);

    List<ArchiveResp> selectArchivePageForTeacher(@Param("req") ArchivePageReq req, @Param("offset") long offset, @Param("limit") long limit);

    long countArchivePageForTeacher(@Param("req") ArchivePageReq req);

    @Update("UPDATE doc_archive SET is_deleted = 1 WHERE archive_id = #{archiveId}")
    void deleteByArchiveId(@Param("archiveId") Long archiveId);
}
