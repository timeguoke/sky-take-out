package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询微信用户
     *
     * @param openid
     * @return
     */
    @Select("select * from user where openid =#{openid}")
    User getByOpenid(String openid);

    /**
     * 注册用户信息
     *
     * @param user
     */
    void insert(User user);

    /**
     * 根据id查询用户
     * @param userId
     * @return
     */
    @Select("select * from user where id =#{userId} ")
    User getById(Long userId);

    /**
     * 用户量统计
     * @param map
     * @return
     */

    Integer countByMap(Map map);
}
