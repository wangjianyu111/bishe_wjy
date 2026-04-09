package com.gdplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdplatform.entity.DocFile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface DocFileMapper extends BaseMapper<DocFile> {

    @Select("SELECT * FROM doc_file WHERE file_id = #{fileId} AND is_deleted = 0")
    DocFile selectByFileId(@Param("fileId") Long fileId);

    @Insert("INSERT INTO doc_file (original_name, stored_name, file_path, file_size, mime_type, " +
            "uploader_id, biz_type, biz_id, selection_id, version_no, create_time) " +
            "VALUES (#{originalName}, #{storedName}, #{filePath}, #{fileSize}, #{mimeType}, " +
            "#{uploaderId}, #{bizType}, #{bizId}, #{selectionId}, #{versionNo}, #{createTime})")
    void insertDocFile(DocFile docFile);

    @Update("UPDATE doc_file SET is_deleted = 1 WHERE file_id = #{fileId}")
    void deleteById(@Param("fileId") Long fileId);
}
