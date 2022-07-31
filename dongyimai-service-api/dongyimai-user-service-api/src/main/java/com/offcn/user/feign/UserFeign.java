package com.offcn.user.feign;

import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.user.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:ujiuye
 * @Description:
 * @Date 2021/2/1 14:19
 *****/
@FeignClient(name = "USER")
@RequestMapping("/user")
public interface UserFeign {

    /**
     * User分页条件搜索实现
     *
     * @param user
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    Result<PageResult<User>> findPage(@RequestBody(required = false) User user, @PathVariable("page") int page, @PathVariable("size") int size);

    /**
     * User分页搜索实现
     *
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    Result<PageResult<User>> findPage(@PathVariable("page") int page, @PathVariable("size") int size);

    /**
     * 多条件搜索品牌数据
     *
     * @param user
     * @return
     */
    @PostMapping(value = "/search")
    Result<List<User>> findList(@RequestBody(required = false) User user);

    /**
     * 根据ID删除品牌数据
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    Result delete(@PathVariable("id") Long id);

    /**
     * 修改User数据
     *
     * @param user
     * @param id
     * @return
     */
    @PutMapping(value = "/{id}")
    Result update(@RequestBody User user, @PathVariable("id") Long id);

    /**
     * 新增User数据
     *
     * @param user
     * @return
     */
    @PostMapping
    Result add(@RequestBody User user);

    /**
     * 根据ID查询User数据
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<User> findById(@PathVariable("id") Long id);

    /**
     * 查询User全部数据
     *
     * @return
     */
    @GetMapping
    Result<List<User>> findAll();

    /**
     * 根据username查询用户信息
     *
     * @param username
     * @return
     */
    @GetMapping("/load/{username}")
    Result<User> findByUsername(@PathVariable("username") String username);

    /**
     * 添加用户积分
     *
     * @param points
     * @return points
     */
    @GetMapping(value = "/points/add")
    Result addPoints(@RequestParam(value = "points") Integer points);
}