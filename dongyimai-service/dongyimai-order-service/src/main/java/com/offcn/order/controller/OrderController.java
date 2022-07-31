package com.offcn.order.controller;

import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.order.pojo.Order;
import com.offcn.order.pojo.PayLog;
import com.offcn.order.service.OrderService;
import com.offcn.utils.TokenDecode;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/****
 * @Author:ujiuye
 * @Description:
 * @Date 2021/2/1 14:19
 *****/
@Api(tags = "OrderController")
@RestController
@RequestMapping("/order")
@CrossOrigin(maxAge = 3600)
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TokenDecode tokenDecode;

    /**
     * Order分页条件搜索实现
     *
     * @param order
     * @param page
     * @param size
     * @return
     */
    @ApiOperation(value = "Order条件分页查询", notes = "分页条件查询Order方法详情", tags = {"OrderController"})
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "当前页", required = true),
            @ApiImplicitParam(paramType = "path", name = "size", value = "每页显示条数", required = true)
    })
    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageResult<Order>> findPage(@RequestBody(required = false) @ApiParam(name = "Order对象", value = "传入JSON数据", required = false) Order order, @PathVariable("page") int page, @PathVariable("size") int size) {
        //调用OrderService实现分页条件查询Order
        PageResult<Order> pageResult = orderService.findPage(order, page, size);
        return new Result<>(true, StatusCode.OK, "查询成功", pageResult);
    }

    /**
     * Order分页搜索实现
     *
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @ApiOperation(value = "Order分页查询", notes = "分页查询Order方法详情", tags = {"OrderController"})
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "当前页", required = true),
            @ApiImplicitParam(paramType = "path", name = "size", value = "每页显示条数", required = true)
    })
    @GetMapping(value = "/search/{page}/{size}")
    public Result<PageResult<Order>> findPage(@PathVariable("page") int page, @PathVariable("size") int size) {
        //调用OrderService实现分页查询Order
        PageResult<Order> pageResult = orderService.findPage(page, size);
        return new Result<>(true, StatusCode.OK, "查询成功", pageResult);
    }

    /**
     * 多条件搜索品牌数据
     *
     * @param order
     * @return
     */
    @ApiOperation(value = "Order条件查询", notes = "条件查询Order方法详情", tags = {"OrderController"})
    @PostMapping(value = "/search")
    public Result<List<Order>> findList(@RequestBody(required = false) @ApiParam(name = "Order对象", value = "传入JSON数据", required = false) Order order) {
        //调用OrderService实现条件查询Order
        List<Order> list = orderService.findList(order);
        return new Result<>(true, StatusCode.OK, "查询成功", list);
    }

    /**
     * 根据ID删除品牌数据
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "Order根据ID删除", notes = "根据ID删除Order方法详情", tags = {"OrderController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "Long")
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable("id") Long id) {
        //调用OrderService实现根据主键删除
        orderService.delete(id);
        return new Result<>(true, StatusCode.OK, "删除成功");
    }

    /**
     * 修改Order数据
     *
     * @param order
     * @param id
     * @return
     */
    @ApiOperation(value = "Order根据ID修改", notes = "根据ID修改Order方法详情", tags = {"OrderController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "Long")
    @PutMapping(value = "/{id}")
    public Result update(@RequestBody @ApiParam(name = "Order对象", value = "传入JSON数据", required = false) Order order, @PathVariable("id") Long id) {
        //设置主键值
        order.setOrderId(id);
        //调用OrderService实现修改Order
        orderService.update(order);
        return new Result<>(true, StatusCode.OK, "修改成功");
    }

    /**
     * 新增Order数据
     *
     * @param order
     * @return
     */
    @ApiOperation(value = "Order添加", notes = "添加Order方法详情", tags = {"OrderController"})
    @PostMapping
    public Result add(@RequestBody @ApiParam(name = "Order对象", value = "传入JSON数据", required = true) Order order) {
        Map<String, String> userInfo = tokenDecode.getUserInfo();
        String username = userInfo.get("username");
        //设置购买用户
        order.setUsername(username);
        try {
            orderService.add(order);
            return new Result(true, StatusCode.OK, "下单成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, StatusCode.ERROR, "下单失败");
        }
    }

    /**
     * 根据ID查询Order数据
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "Order根据ID查询", notes = "根据ID查询Order方法详情", tags = {"OrderController"})
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "Long")
    @GetMapping("/{id}")
    public Result<Order> findById(@PathVariable("id") Long id) {
        //调用OrderService实现根据主键查询Order
        Order order = orderService.findById(id);
        return new Result<>(true, StatusCode.OK, "查询成功", order);
    }

    /**
     * 查询Order全部数据
     *
     * @return
     */
    @ApiOperation(value = "查询所有Order", notes = "查询所Order有方法详情", tags = {"OrderController"})
    @GetMapping
    public Result<List<Order>> findAll() {
        //调用OrderService实现查询所有Order
        List<Order> list = orderService.findAll();
        return new Result<>(true, StatusCode.OK, "查询成功", list);
    }

    /**
     * 查询用户的支付日志
     *
     * @return
     */
    @ApiOperation(value = "根据登录用户从redis中查询支付日志", notes = "查询用户支付日志", tags = {"OrderController"})
    @GetMapping("/searchPayLogFromRedis/{username}")
    public Result<PayLog> searchPayLogFromRedis(@PathVariable(name = "username") String username) {
        PayLog payLog = orderService.searchPayLogFromRedis(username);
        return new Result<>(true, StatusCode.OK, "查询成功", payLog);
    }

    /**
     * 修改订单的状态
     *
     * @param outTradeNo
     * @param transactionId
     * @return
     */
    @ApiOperation(value = "修改订单的状态", notes = "修改订单的状态", tags = {"OrderController"})
    @GetMapping(value = "/updateOrderStatus")
    public Result updateOrderStatus(
            @RequestParam(value = "outTradeNo") String outTradeNo,
            @RequestParam(value = "transactionId") String transactionId) {
        try {
            orderService.updateOrderStatus(outTradeNo, transactionId);
            return new Result<>(true, StatusCode.OK, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(false, StatusCode.ERROR, "修改失败");
        }
    }
}
