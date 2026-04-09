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

    @Update("UPDATE doc_file SET is_deleted = 1 WHERE file_id = #{fileId}")
    void deleteById(@Param("fileId") Long fileId);
}
