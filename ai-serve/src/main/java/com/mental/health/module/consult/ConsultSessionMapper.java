package com.mental.health.module.consult;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 咨询会话 Mapper
 */
@Mapper
public interface ConsultSessionMapper extends BaseMapper<ConsultSession> {

    /** 消息数原子自增 */
    @Update("UPDATE t_consult_session SET message_count = message_count + 1 WHERE id = #{id}")
    int incrementMessageCount(@Param("id") Long id);
}
