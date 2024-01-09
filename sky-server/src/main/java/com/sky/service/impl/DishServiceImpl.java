package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;

import com.sky.entity.DishFlavor;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional
    public void save(DishDTO dishDTO) {
        // 把DishDTO转为Dish和DishFlavor
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 这时候是没有dishId的，需要主键回填
        dishMapper.insertDish(dish);
        //Dish dish1 = dishMapper.getByName(dish.getName());
        long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        for (DishFlavor flavor : flavors) {
            System.out.println("增加口味: " + flavor.toString());
            flavor.setDishId(dishId);
            dishMapper.insertDishFlavor(flavor);
        }

    }
}
