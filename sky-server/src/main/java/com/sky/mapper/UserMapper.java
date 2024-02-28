package com.sky.mapper;

import com.sky.entity.User;
import io.lettuce.core.dynamic.annotation.Key;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Mapper
public interface UserMapper {


    @Select("select * from user where openid = #{openid};")
    User getByOpenId(String openid);


    void insert(User user);

    @Select("select * from user where id = #{userId};")
    User getById(Long userId);

    @Select("select COUNT(id) from user where create_time between #{beginTime} and #{endTime};")
    BigDecimal getUserNum(LocalDateTime beginTime, LocalDateTime endTime);
}
