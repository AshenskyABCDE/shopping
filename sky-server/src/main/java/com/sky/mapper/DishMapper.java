package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {
    // 根据分类id查询菜品数量
    @Select("select count(id) from dish where category_id = #{category_id}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> PageQuery(DishPageQueryDTO dishPageQueryDTO);

    // 根据id查找菜品
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    // 根据id删除
    @Delete("delete  from dish where  id = #{id}")
    void deleteById(Long id);


    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);
}
