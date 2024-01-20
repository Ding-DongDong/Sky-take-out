package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    /**
     * 根据id查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenID(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);

    /**
     * 通过主键查id
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{id}")
    User getById(Long userId);
}
