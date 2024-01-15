package com.sky.mapper;

import com.sky.entity.User;
import io.lettuce.core.dynamic.annotation.Key;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface UserMapper {


    @Select("select * from user where openid = #{openid};")
    User getByOpenId(String openid);


    void insert(User user);
}