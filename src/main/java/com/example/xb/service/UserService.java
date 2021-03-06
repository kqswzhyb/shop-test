package com.example.xb.service;

import com.example.xb.domain.account.LoginBody;
import com.example.xb.domain.user.User;
import com.example.xb.domain.user.Password;

import java.util.List;

/**
 * @author Administrator
 */
public interface UserService {
    /**
     * 根据条件分页查询用户数据
     *
     * @param user 用户信息
     * @return 用户数据集合信息
     */
    List<User> selectUserList(User user);

    /**
     * 根据username查询
     *
     * @param userName 用户信息
     * @return 用户数据集合信息
     */
    List<User> queryByUserName(String userName);

    /**
     * 创建新用户数据
     *
     * @param user 用户信息
     * @return 用户数据信息
     */
    int saveUser(User user);

    /**
     * 更新新用户数据
     *
     * @param user 用户信息
     * @return 用户数据信息
     */
    int updateUser(User user);


    /**
     * 通过用户ID删除角色
     *
     * @param userId 用户ID
     * @return 结果
     */
    int deleteUserById(String userId);

    /**
     * 登录
     *
     * @param loginBody 登录体
     * @return 结果
     */
    User userLogin(LoginBody loginBody);

    /**
     *  修改密码
     * @param password 密码实体
     * @return 结果
     */
    int updatePassword(Password password);

    /**
     * 通过userId找密码
     * @param userId 用户ID
     * @return 密码
     */
    String queryPasswordById(String userId);

    /**
     * 根据userName查询个数
     * @param userName 用户名
     * @return 结果
     */
    int queryCountByName(String userName);

    /**
     * 根据userId查询个数
     * @param userId 用户名
     * @return 结果
     */
    int queryCountById(String userId);
}
