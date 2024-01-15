package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;

import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.annotations.Cacheable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private RedisTemplate redisTemplate;

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

        deleteRedis("dish_*");

    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<Dish> page = dishMapper.pageQuery(dishPageQueryDTO);
        long total = page.getTotal();
        List<Dish> records = page.getResult();
        return new PageResult(total, records);
    }

    /**
     * 删除菜品业务规则：
     * 1.起售中(status==1)的不能删
     * 2.被套餐绑定的菜品不能删
     * 3.一旦删了菜品，对应的口味表的数据也要删
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }


        }

        List<Long> setmealIds = setmealDishMapper.getSetMealIdsByDishIds(ids);
        if(setmealIds != null && setmealIds.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        for (Long id : ids) {
            dishMapper.deleteById(id);
            dishMapper.deleteFlavorByDishId(id);
        }

        deleteRedis("dish_*");

    }

    /**
     * 根据DishId返回菜品
     * @param id
     * @return
     */
    @Override
    public DishDTO getById(Long id) {
        // 获取dish表的数据
        Dish dish = dishMapper.getById(id);
        // 获取dish_flavor表的数据
        List<DishFlavor> flavors = dishMapper.getFlavorByDishId(id);

        // 组成DishDTo
        DishDTO dishDTO = new DishDTO();
        BeanUtils.copyProperties(dish, dishDTO);
        dishDTO.setFlavors(flavors);
        return dishDTO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        // 把dishDTO转为Dish和DishFlavor

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.updateDish(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();

        // 对于DishFlavor，应该是先delete后insert
        Long dishId = dish.getId();
        dishMapper.deleteFlavorByDishId(dishId);
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
            dishMapper.insertDishFlavor(flavor);
        }

        deleteRedis("dish_*");

    }


    @Override
    public void updateSaleState(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.updateDish(dish);

        deleteRedis("dish_*");
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */

    public List<DishVO> listWithFlavor(Dish dish) {
        // 改为用 redis缓存菜品：如果redis已有，则直接拿；否则从数据库拿，返回并缓存到redis
        // 查询 redis是否有
        //RedisTemplate<String, List<DishVO>> redisTemplate = new RedisTemplate<>();
        ValueOperations<String, List<DishVO>> op = redisTemplate.opsForValue();
        List<DishVO> dishVOS = op.get("dish_"+dish.getCategoryId());
        if(dishVOS != null && dishVOS.size() > 0){
            return dishVOS;
        }
        // redis没有，接着从数据库查询
        List<Dish> dishList = dishMapper.list(dish);


        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishMapper.getFlavorByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        // 缓存到redis
        op.set("dish_"+dish.getCategoryId(), dishVOList);
        return dishVOList;
    }

    // 每当有菜品被修改时，（增删改）,就清除redis对应数据
    private void deleteRedis(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
