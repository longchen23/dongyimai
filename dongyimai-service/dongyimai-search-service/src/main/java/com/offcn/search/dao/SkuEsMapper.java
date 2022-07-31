package com.offcn.search.dao;

import com.offcn.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @description: TODO
 * @author: LongChen
 * @date: 2022/7/13 14:57
 * @version: 1.0
 */

@Repository
public interface SkuEsMapper extends ElasticsearchRepository<SkuInfo, Long> {
}
