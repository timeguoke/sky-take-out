package com.sky.controller.admin;

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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加菜品
     *
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("添加菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("添加菜品{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //新增菜品开始清理缓存数据
        //应为用户端是根据分类显示的,防止显示的是旧的分类数据
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);

        return Result.success();

    }

    /**
     * 菜品分页操作
     *
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页")
    public Result<PageResult> Page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页:{}", dishPageQueryDTO);
        PageResult pageResult = dishService.page(dishPageQueryDTO);

        return Result.success(pageResult);

    }

    /**
     *
     * 菜品的批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品的批量删除")
    public Result DeleteByIds(@RequestParam List<Long> ids) {
        log.info("菜品的批量删除{}", ids);
        dishService.DeleteByIds(ids);

        //清理缓存数据
        //现获取所有的菜品所有的key
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 根据id查询菜单(回显数据)
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜单(回显数据)")
    public Result<DishVO> SelectById(@PathVariable Long id) {
        log.info("根据id查询菜单(回显数据){}", id);
        DishVO dishVO = dishService.SelectByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     *
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result Update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品{}", dishDTO);
        dishService.Update(dishDTO);

        //清理缓存数据
        cleanCache("dish_*");

        return Result.success();

    }

    /**
     *
     * 起售停售设置
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("起售停售设置")
    public Result StartOrStop(@PathVariable Integer status, Long id) {
        log.info("起售停售设置:{}{}", status, id);
        dishService.StartOrStop(status, id);
        //清理缓存数据
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     *
     * @param CategoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> SelectByCategoryId(Long CategoryId) {
        log.info("根据分类id查询菜品{}", CategoryId);
        List<Dish> dishes = dishService.SelectByCategoryId(CategoryId);
        return Result.success(dishes);
    }

    /**
     * 清理缓存数据
     *
     * @param pattern
     */
    //清理缓存数据
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);

    }


}
