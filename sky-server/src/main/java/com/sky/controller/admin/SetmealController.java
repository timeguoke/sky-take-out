package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;


    /**
     * 添加套餐
     *
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("添加套餐")
    @CacheEvict(cacheNames = "Setmeal", key="#setmealDTO.categoryId" )
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("添加套餐:{}", setmealDTO);
        setmealService.save(setmealDTO);
        return Result.success();

    }

    /**
     * 套餐分页
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("套餐分页")
    public Result<PageResult> Page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页:{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.Page(setmealPageQueryDTO);
        return Result.success(pageResult);

    }

    /**
     * 根据id批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id批量删除")
    @CacheEvict(cacheNames = "Setmeal",allEntries = true)
    public Result DeleteByIds(@RequestParam List<Long> ids) {
        log.info("根据id批量删除:{}", ids);
        setmealService.DeleteByIds(ids);
        return Result.success();

    }

    /**
     * 根据id查询套餐(回显套餐数据)
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐(回显套餐数据)")
    public Result<SetmealVO> SelectById(@PathVariable Long id) {
        log.info("根据id查询套餐(回显套餐数据):{}", id);
        SetmealVO setmealVO = setmealService.SelectByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "Setmeal",allEntries = true)
    public Result Update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐:{}", setmealDTO);
        setmealService.Update(setmealDTO);
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
    @CacheEvict(cacheNames = "Setmeal",allEntries = true)
    public Result StartOrStop(@PathVariable Integer status, Long id) {
        log.info("起售停售设置:{}{}", status, id);
        setmealService.StartOrStop(status, id);
        return Result.success();
    }
}
