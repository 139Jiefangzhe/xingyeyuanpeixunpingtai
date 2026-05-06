package com.playedu.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playedu.exam.domain.entity.Question;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    List<Question> selectByConditions(
            @Param("bankId") String bankId,
            @Param("type") Integer type,
            @Param("difficulty") Integer difficulty,
            @Param("knowledgePoint") String knowledgePoint,
            @Param("limit") Integer limit);
}
