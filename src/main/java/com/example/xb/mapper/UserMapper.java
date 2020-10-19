package com.example.xb.mapper;

import com.example.xb.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * @author Administrator
 */
@Mapper
public interface UserMapper {
    /**
     * 根据条件分页查询用户数据
     *
     * @param user 用户信息
     * @return 用户数据集合信息
     */
    List<User> selectUserList(User user);
}
