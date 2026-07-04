package com.itheima.mes1.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.ai.entity.AiMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AiMessageMapper extends BaseMapper<AiMessage> {
    @Select("SELECT * FROM ai_message WHERE conversation_id = #{conversationId} ORDER BY create_time")
    List<AiMessage> selectByConversationId(Long conversationId);
}
