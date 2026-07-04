package com.itheima.mes1.module.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.knowledge.entity.KbChunk;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface KbChunkMapper extends BaseMapper<KbChunk> {
    @Select("SELECT * FROM kb_chunk WHERE document_id = #{documentId} ORDER BY chunk_index")
    List<KbChunk> selectByDocumentId(Long documentId);

    @Delete("DELETE FROM kb_chunk WHERE document_id = #{documentId}")
    void deleteByDocumentId(Long documentId);
}
