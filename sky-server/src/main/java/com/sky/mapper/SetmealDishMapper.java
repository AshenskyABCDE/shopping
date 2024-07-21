package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    // 根据菜品id查找对应的套餐
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);
}
