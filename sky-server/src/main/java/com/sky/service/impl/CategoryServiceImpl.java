package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增分类
     *
     * @param categoryDTO
     */
    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        //进行数据拷贝
        BeanUtils.copyProperties(categoryDTO, category);
        //设置状态,默认为0
        category.setStatus(StatusConstant.ENABLE);
        //设置创建时间和修改时间
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        //设置创建人的id和修改人的id的信息
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        //添加
        categoryMapper.insert(category);

    }

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    public PageResult Page(CategoryPageQueryDTO categoryPageQueryDTO){
        //使用插件PageHelper进行分页
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());

        Page page =categoryMapper.Page(categoryPageQueryDTO);

        long total = page.getTotal();
        List result = page.getResult();

        return new PageResult(total,result);
    }

    public void update(CategoryDTO categoryDTO){
        Category category =new Category();
        //先进行拷贝
         BeanUtils.copyProperties(categoryDTO,category);

        //更新修改时间和修改人id
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        //更新
        categoryMapper.update(category);
    }

}
