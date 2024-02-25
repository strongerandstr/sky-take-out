package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);
        dishService.save(dishDTO);
        return Result.success();
    }


    /**
     * 菜品分页查询
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询:{}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);

        return Result.success(pageResult);
    }
    @DeleteMapping()
    @ApiOperation("删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("删除菜品:菜品id = {}",ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping(value = "/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishDTO> getById(@PathVariable Long id){
        log.info("查询菜品:菜品id = {}",id);
        DishDTO dishDTO = dishService.getById(id);
        return Result.success(dishDTO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品为:{}", dishDTO);
        dishService.update(dishDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result updateSaleState(@PathVariable Integer status, Long id) {
        dishService.updateSaleState(status, id);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        List<DishVO> list = dishService.listWithFlavor(dish);

        return Result.success(list);
    }
}
