package com.offcn.search.controller;

import com.offcn.entity.Page;
import com.offcn.search.feign.SearchSkuFeign;
import com.offcn.search.pojo.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping(value = "/search")
public class SkuController {

    @Autowired
    private SearchSkuFeign searchSkuFeign;

    /**
     * 搜索
     *
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map<String, String> searchMap, Model model) {
        Map<String, Object> resultMap = searchSkuFeign.search(searchMap);
        model.addAttribute("result", resultMap);
        model.addAttribute("searchMap", searchMap);
        String url = this.setUrl(searchMap);
        model.addAttribute("url", url);
        Page<SkuInfo> infoPage = new Page<>(
                Integer.parseInt(resultMap.get("pageSize").toString()),
                Integer.parseInt(resultMap.get("pageNum").toString()),
                Integer.parseInt(resultMap.get("total").toString())
        );
        model.addAttribute("page", infoPage);
        return "search";
    }

    public String setUrl(Map<String, String> searchMap) {
        String url = "/search/list";
        if (searchMap != null && searchMap.size() > 0) {
            url += "?";
            for (Map.Entry<String, String> searchEntry : searchMap.entrySet()) {
                String value = searchEntry.getValue();
                String key = searchEntry.getKey();
                //过滤分页参数
                if ("pageNum".equals(key)) {
                    continue;
                }
                //设置价格排序
                if ("sortField".equals(searchEntry.getKey()) || "sortRule".equals(searchEntry.getKey())) {
                    continue;
                }
                url += key + "=" + value + "&";
            }
            if (url.lastIndexOf("&") != -1) {
                url = url.substring(0, url.lastIndexOf("&"));
            }
        }
        System.out.println("url: " + url);
        return url;
    }
}