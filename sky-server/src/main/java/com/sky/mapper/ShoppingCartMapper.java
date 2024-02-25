package com.sky.mapper;

import com.sky.entity.SetmealDish;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    @Update("update shopping_cart set number = #{number} where id = #{id};")
    void updateNumberById(ShoppingCart shoppingCart);

    // 根据 userId, dishId或setmealId查询购物车
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    void insert(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where user_id = #{userId};")
    void cleanByUserId(Long userId);
}
