package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DishMapper {
    @Insert("insert into dish (name, category_id, price, image, description, status, create_time, update_time, create_user, update_user)"
            + "values "
            + "(#{name},#{categoryId},#{price},#{image},#{description},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @AutoFill(OperationType.INSERT)
    void insertDish(Dish dish);

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @Insert("insert into dish_flavor (dish_id, name, value) "
            + "values "
            + "(#{dishId},#{name},#{value})")
    void insertDishFlavor(DishFlavor dishFlavor);

    @Select("select * from dish where name = #{dishName};")
    Dish getByName(String dishName);

    Page<Dish> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    @Select("select * from dish where id = #{id};")
    Dish getById(Long id);

    @Delete("delete from dish where id = #{id};")
    void deleteById(Long id);

    @Delete("delete from dish_flavor where dish_id = #{id};")
    void deleteFlavorByDishId(Long id);

    @Select("select * from dish_flavor WHERE dish_id = #{id};")
    List<DishFlavor> getFlavorByDishId(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void updateDish(Dish dish);


    List<Dish> list(Dish dish);
}
