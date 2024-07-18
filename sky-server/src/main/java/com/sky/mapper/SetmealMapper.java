package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {
    // 根据分类id查询数量
    @Select("select count(*) from setmeal where category_id = #{category_id}")
    Integer countByCategoryId(Long id);
}
