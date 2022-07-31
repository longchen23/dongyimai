package com.offcn.sellergoods.feign;

import com.offcn.entity.Result;
import com.offcn.sellergoods.pojo.ItemCat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/16 15:39
 * @version: 1.0
 */

@FeignClient(name = "sellergoods")
@RequestMapping("/itemCat")
public interface ItemCatFeign {

    /**
     * 获取分类的对象信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<ItemCat> findById(@PathVariable(name = "id") Integer id);
}
