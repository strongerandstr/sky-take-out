package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<Long> getSetMealIdsByDishIds(List<Long> ids);

    @Insert("insert into setmeal_dish (setmeal_id, dish_id, name, price, copies) "
            +"values "+
            "(#{setmealId},#{dishId},#{name},#{price},#{copies});")
    void save(SetmealDish setmealDish);
}
