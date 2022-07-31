package com.offcn.sellergoods.feign;

import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.sellergoods.pojo.Item;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "SELLERGOODS")
@RequestMapping(value = "/item")
public interface ItemFeign {

    /**
     * Item分页条件搜索实现
     *
     * @param item
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    Result<PageResult<Item>> findPage(@RequestBody(required = false) Item item, @PathVariable("page") int page, @PathVariable("size") int size);

    /**
     * Item分页搜索实现
     *
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    Result<PageResult<Item>> findPage(@PathVariable("page") int page, @PathVariable("size") int size);

    /**
     * 多条件搜索品牌数据
     *
     * @param item
     * @return
     */
    @PostMapping(value = "/search")
    Result<List<Item>> findList(@RequestBody(required = false) Item item);

    /**
     * 根据ID删除品牌数据
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    Result delete(@PathVariable("id") Long id);

    /**
     * 修改Item数据
     *
     * @param item
     * @param id
     * @return
     */
    @PutMapping(value = "/{id}")
    Result update(@RequestBody Item item, @PathVariable("id") Long id);

    /**
     * 新增Item数据
     *
     * @param item
     * @return
     */
    @PostMapping
    Result add(@RequestBody Item item);

    /**
     * 根据ID查询Item数据
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Item> findById(@PathVariable("id") Long id);

    /**
     * 查询Item全部数据
     *
     * @return
     */
    @GetMapping
    Result<List<Item>> findAll();


    @GetMapping("/status/{status}")
    Result<List<Item>> findByStatus(@PathVariable("status") String status);

    /**
     * 库存递减
     *
     * @param username
     * @return username
     */
    @GetMapping(value = "/decr/count/{username}")
    Result decrCount(@PathVariable("username") String username);
}