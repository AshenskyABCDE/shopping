package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {
    // 根据分类id查询菜品数量
    @Select("select count(id) from dish where category_id = #{category_id}")
    Integer countByCategoryId(Long categoryId);
}
