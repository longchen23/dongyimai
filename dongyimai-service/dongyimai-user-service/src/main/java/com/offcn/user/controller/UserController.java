package com.offcn.user.controller;

import com.alibaba.fastjson.JSON;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.user.pojo.User;
import com.offcn.user.service.UserService;
import com.offcn.utils.JwtUtil;
import com.offcn.utils.PhoneFormatCheckUtils;
import com.offcn.utils.TokenDecode;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/****
 * @Author:ujiuye
 * @Description:
 * @Date 2021/2/1 14:19
 *****/
@Api(tags = "UserController")
@RestController
@RequestMapping("/user")
@CrossOrigin(maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenDecode tokenDecode;

    /**
     * User分页条件搜索实现
     *
     * @param user
     * @param page
     * @param size
     * @return
     */
    @ApiOperation(value = "User条件分页查询", notes = "分页条件查询User方法详情", tags = {"UserController"})
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "当前页", required = true),
            @ApiImplicitParam(paramType = "path", name = "size", value = "每页显示条数", required = true)
    })
    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageResult<User>> findPage(@RequestBody(required = false) @ApiParam(name = "User对象", value = "传入JSON数据", required = false) User user, @PathVariable("page") int page, @PathVariable("size") int size) {
        //调用UserService实现分页条件查询User
        PageResult<User> pageResult = userService.findPage(user, page, size);
        return new Result<>(true, StatusCode.OK, "查询成功", pageResult);
    }

    /**
     * User分页搜索实现
     *
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @ApiOperation(value = "User分页查询", notes = "分页查询User方法详情", tags = {"UserController"})
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "当前页", required = true),
            @ApiImplicitParam(paramType = "path", name = "size", value = "每页显示条数", required = true)
    })
    @GetMapping(value = "/search/{page}/{size}")
    public Result<PageResult<User>> findPage(@PathVariable("page") int page, @PathVariable("size") int size) {
        //调用UserService实现分页查询User
        PageResult<User> pageResult = userService.findPage(page, size);
        return new Result<>(true, StatusCode.OK, "查询成功", pageResult);
    }

    /***
     * 多条件搜索品牌数据
     * @param user
     * @return
     */
    @ApiOperation(value = "User条件查询", notes = "条件查询User方法详情", tags = {"UserController"})
    @PostMapping(value = "/search")
    public Result<List<User>> findList(@RequestBody(required = false) @ApiParam(name = "User对象", value = "传入JSON数据", required = false) User user) {
        //调用UserService实现条件查询User
        List<User> list = userService.findList(user);
        return new Result<>(true, StatusCode.OK, "查询成功", list);
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @ApiOperation(value = "User根据ID删除", notes = "根据ID删除User方法详情", tags = {"UserController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "Long")
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable("id") Long id) {
        //调用UserService实现根据主键删除
        userService.delete(id);
        return new Result<>(true, StatusCode.OK, "删除成功");
    }

    /***
     * 修改User数据
     * @param user
     * @param id
     * @return
     */
    @ApiOperation(value = "User根据ID修改", notes = "根据ID修改User方法详情", tags = {"UserController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "Long")
    @PutMapping(value = "/{id}")
    public Result update(@RequestBody @ApiParam(name = "User对象", value = "传入JSON数据", required = false) User user, @PathVariable("id") Long id) {
        //设置主键值
        user.setId(id);
        //调用UserService实现修改User
        userService.update(user);
        return new Result<>(true, StatusCode.OK, "修改成功");
    }

    /***
     * 新增User数据
     * @param user
     * @return
     */
    @ApiOperation(value = "User添加", notes = "添加User方法详情", tags = {"UserController"})
    @PostMapping
    public Result add(@RequestBody @ApiParam(name = "User对象", value = "传入JSON数据", required = true) User user) {
        //调用UserService实现添加User
        userService.add(user);
        return new Result<>(true, StatusCode.OK, "添加成功");
    }

    /***
     * 根据ID查询User数据
     * @param id
     * @return
     */
    @ApiOperation(value = "User根据ID查询", notes = "根据ID查询User方法详情", tags = {"UserController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "Long")
    @PreAuthorize("hasAnyAuthority('admin','user')")
    @GetMapping("/{id}")
    public Result<User> findById(@PathVariable("id") Long id) {
        //调用UserService实现根据主键查询User
        User user = userService.findById(id);
        return new Result<>(true, StatusCode.OK, "查询成功", user);
    }

    /***
     * 查询User全部数据
     * @return
     */
    @ApiOperation(value = "查询所有User", notes = "查询所User有方法详情", tags = {"UserController"})
    @GetMapping
    public Result<List<User>> findAll(HttpServletRequest request) {
//        String authorization = request.getHeader("Authorization");
//        Map map = JSON.parseObject(authorization, Map.class);
//        System.out.println("username:" + map.get("username"));
//        System.out.println("令牌信息:" + authorization);
        //调用UserService实现查询所有User
        List<User> list = userService.findAll();
        return new Result<>(true, StatusCode.OK, "查询成功", list);
    }

    /**
     * 发送短信验证码
     *
     * @param phone
     * @return
     */
    @GetMapping("/sendCode")
    public Result sendCode(String phone) {
        if (!PhoneFormatCheckUtils.isPhoneLegal(phone)) {
            return new Result<>(false, StatusCode.ERROR, "手机号格式不正确");
        }
        try {
            userService.createSmsCode(phone);
            return new Result<>(true, StatusCode.OK, "验证码发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(false, StatusCode.ERROR, "验证码发送失败");
        }
    }

    /**
     * 增加
     *
     * @param user
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody User user, String smscode) {
        boolean checkSmsCode = userService.checkSmsCode(user.getPhone(), smscode);
        if (!checkSmsCode) {
            return new Result<>(false, StatusCode.ERROR, "验证码输入错误！");
        }
        try {
            userService.add(user);
            return new Result<>(true, StatusCode.OK, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(false, StatusCode.ERROR, "增加失败");
        }
    }

    @RequestMapping(value = "/login")
    public Result login(String username, String password, HttpServletResponse response) {
        User byUsername = userService.findByUsername(username);
        if (byUsername != null) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(password, byUsername.getPassword());
            if (matches) {
                //设置令牌信息
                HashMap<String, Object> map = new HashMap<>();
                map.put("role", "USER");
                map.put("success", "SUCCESS");
                map.put("username", username);
                //生成令牌
                String jwt = JwtUtil.createJWT(UUID.randomUUID().toString(), JSON.toJSONString(map), null);
                //创建cookie对象
                Cookie cookie = new Cookie("Authorization", jwt);
                //设置cookie路径
                cookie.setPath("/");
                //把cookie响应给浏览器
                response.addCookie(cookie);
                return new Result<>(true, StatusCode.OK, "登陆成功", jwt);
            }
        }
        return new Result<>(false, StatusCode.ERROR, "账号名或密码错误");
    }

    @GetMapping("/load/{username}")
    public Result<User> findByUsername(@PathVariable("username") String username) {
        //调用UserService实现根据主键查询User
        User user = userService.findByUsername(username);
        return new Result<>(true, StatusCode.OK, "查询成功", user);
    }

    /**
     * 增加用户积分
     *
     * @param points
     */
    @GetMapping(value = "/points/add")
    public Result addPoints(@RequestParam(value = "points") Integer points) {
        Map<String, String> userMap = tokenDecode.getUserInfo();
        String username = userMap.get("username");
        userService.addUserPoints(username, points);
        return new Result<>(true, StatusCode.OK, "添加积分成功");
    }

}
