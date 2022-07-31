package com.offcn.order.feign;

import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.order.pojo.Order;
import com.offcn.order.pojo.PayLog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:ujiuye
 * @Description:
 * @Date 2021/2/1 14:19
 *****/
@FeignClient(name = "ORDER")
@RequestMapping("/order")
public interface OrderFeign {

    /***
     * Order分页条件搜索实现
     * @param order
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    Result<PageResult<Order>> findPage(@RequestBody(required = false) Order order, @PathVariable("page") int page, @PathVariable("size") int size);

    /***
     * Order分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    Result<PageResult<Order>> findPage(@PathVariable("page") int page, @PathVariable("size") int size);

    /***
     * 多条件搜索品牌数据
     * @param order
     * @return
     */
    @PostMapping(value = "/search")
    Result<List<Order>> findList(@RequestBody(required = false) Order order);

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    Result delete(@PathVariable("id") Long id);

    /***
     * 修改Order数据
     * @param order
     * @param id
     * @return
     */
    @PutMapping(value = "/{id}")
    Result update(@RequestBody Order order, @PathVariable("id") Long id);

    /***
     * 新增Order数据
     * @param order
     * @return
     */
    @PostMapping
    Result add(@RequestBody Order order);

    /***
     * 根据ID查询Order数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Order> findById(@PathVariable("id") Long id);

    /***
     * 查询Order全部数据
     * @return
     */
    @GetMapping
    Result<List<Order>> findAll();

    @GetMapping("/searchPayLogFromRedis/{username}")
    public Result<PayLog> searchPayLogFromRedis(@PathVariable(name = "username") String username);

    /**
     * 修改订单状态
     *
     * @param outTradeNo
     * @param transactionId
     * @return
     */
    @GetMapping(value = "/updateOrderStatus")
    Result updateOrderStatus(
            @RequestParam(value = "outTradeNo") String outTradeNo,
            @RequestParam(value = "transactionId") String transactionId);

}