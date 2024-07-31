package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select  * from user where openid = #{openid}")

    User getByOpenid(String openid);

    void insert(User user);

    Integer countByMap(Map map);
}
