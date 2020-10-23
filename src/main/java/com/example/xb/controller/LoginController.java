package com.example.xb.controller;


import com.example.xb.domain.LoginBody;
import com.example.xb.domain.LoginUser;
import com.example.xb.domain.User;
import com.example.xb.domain.page.DataDomain;
import com.example.xb.domain.result.AjaxResult;
import com.example.xb.domain.result.ResultInfo;
import com.example.xb.service.IUserService;
import com.example.xb.utils.AESUtil;
import com.example.xb.utils.JwtUtil;
import com.example.xb.utils.RedisCache;
import com.example.xb.utils.TokenUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("/v1")
@Api(tags = "登录")
public class LoginController {

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IUserService userService;

    @Value("${token.password.secret}")
    private String SECRET_KEY;

    /**
     * 登录
     *
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "登录", notes = "登录")
    public AjaxResult login(@RequestBody LoginBody loginBody) {
        ResultInfo resultInfo = new ResultInfo();

        loginBody.setPassword(AESUtil.encryptIntoHexString(loginBody.getPassword(), SECRET_KEY));

        String userId =userService.userLogin(loginBody);

        if(StringUtils.isEmptyOrWhitespace(userId)) {
            resultInfo.error("用户名或密码错误");

            return new AjaxResult(resultInfo, null);
        }else {
            User user = new User();
            user.setUserId(userId);
            user.setUserName(loginBody.getUserName());
            user.setPassword(loginBody.getPassword());
            LoginUser loginUser = new LoginUser();
            loginUser.setUser(user);
            String token = tokenUtil.createToken(loginUser);
            System.out.println(token);
            Map<String, Object> map = jwtUtil.parseToken(token);
            System.out.println(map);

            resultInfo.success("登录成功");

            return new AjaxResult(resultInfo, token);
        }
    }

    /**
     * 退出登录
     *
     * @return
     */
    @GetMapping("/logout")
    @ApiOperation(value = "退出登录", notes = "退出登录")
    public AjaxResult logout(HttpServletRequest req) {
        ResultInfo resultInfo = new ResultInfo();
        try {
            String token = jwtUtil.resolveToken(req);
            Map<String, Object> map = jwtUtil.parseToken(token);
            redisCache.deleteObject((String) map.get("user_id"));
            resultInfo.success("退出成功");
        }catch (Exception e) {
            resultInfo.error("失败");
        }
        return new AjaxResult(resultInfo, null);
    }
}
