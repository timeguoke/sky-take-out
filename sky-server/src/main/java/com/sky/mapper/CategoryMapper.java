package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 新增分类
     *
     * @param category
     */
    @Insert("insert into category(type, name, sort, status, create_time, update_time, create_user, update_user)" +
            " VALUES" +
            " (#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Category category);

    /**
     * 分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    Page<Category> Page(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 1.修改分类
     * 2.启用禁用分类
     *
     * @param category
     */
    void update(Category category);
    /**
     * 根据id删除信息
     * @param id
     *
     */
    @Delete("DELETE FROM category WHERE id = #{id};")
    void DeleteById(Long id);


    List<Category> list(Integer type);
}
