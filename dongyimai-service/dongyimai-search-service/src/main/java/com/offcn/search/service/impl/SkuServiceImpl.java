package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.entity.Result;
import com.offcn.search.dao.SkuEsMapper;
import com.offcn.search.pojo.SkuInfo;
import com.offcn.search.service.SkuService;
import com.offcn.sellergoods.feign.ItemFeign;
import com.offcn.sellergoods.pojo.Item;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuEsMapper skuEsMapper;

    @Autowired
    private ItemFeign itemFeign;

    //ElasticsearchRestTemplate是Spring封装ES客户端的一些原生api模板，方便实现一些查询，和ElasticsearchTemplate一样，但是目前spring推荐使用前者，是一种更高级的REST风格api。
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public void importSku() {
        Result<List<Item>> result = itemFeign.findByStatus("1");
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(result.getData()), SkuInfo.class);
        for (SkuInfo skuInfo : skuInfoList) {
            Map<String, Object> specMap = JSON.parseObject(skuInfo.getSpec());
            skuInfo.setSpecMap(specMap);
        }
        skuEsMapper.saveAll(skuInfoList);
    }

    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {

        //1.获取关键字的值
        String keywords = searchMap.get("keywords");
        //如果搜索条件为空，赋默认值为华为
        if (StringUtils.isEmpty(keywords)) {
            keywords = "手机";
        }

        //2.创建查询对象的构建对象 NativeSearchQueryBuilder是elasticsearch中的原生查询条件类，用于建造一个NativeSearchQuery查询对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //2.1、添加分组条件,.terms("分组别名").field("分组列")
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("categorygroup").field("category"));

        //2.2、添加品牌分组条件
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("brandgroup").field("brand"));

        //2.3、添加规格分组
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpecgroup").field("spec.keyword"));

        //2.4、设置分页
        //拿到当前页数
        String pageNum = searchMap.get("pageNum");
        int pageNo = 1;
        if (!StringUtils.isEmpty(pageNum)) {
            pageNo = new Integer(pageNum);
        }
        int pageSize = 40;
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNo - 1, pageSize));

        //2.5、设置过滤条件
        //---------------------------------------------开始过滤--------------------------------------------------------//
        //创建多条件组合查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //设置品牌过滤条件
        if (!StringUtils.isEmpty(searchMap.get("category"))) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery("category", searchMap.get("category")));
        }
        //设置分类过滤条件
        if (!StringUtils.isEmpty(searchMap.get("brand"))) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery("brand", searchMap.get("brand")));
        }
        //设置规格过滤条件
        if (!StringUtils.isEmpty(String.valueOf(searchMap))) {
            for (String key : searchMap.keySet()) {
                if (key.startsWith("spec_")) {
                    boolQueryBuilder.filter(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", searchMap.get(key)));
                }
            }
        }
        //设置价格过滤条件
        String price = searchMap.get("price");
        if (!StringUtils.isEmpty(price)) {
            String[] split = price.split("-");
            if (!"*".equals(split[1])) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]).lte(split[1]));
            } else {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
            }
        }
        //关联过滤条件到查询对象
        nativeSearchQueryBuilder.withFilter(boolQueryBuilder);
        //---------------------------------------------结束过滤--------------------------------------------------------//

        //设置排序规则
        String sortRule = searchMap.get("sortRule");
        String sortField = searchMap.get("sortField");
        if (!StringUtils.isEmpty(sortRule) && !StringUtils.isEmpty(sortField)) {
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortField)
                    .order("DESC".equals(sortRule) ? SortOrder.DESC : SortOrder.ASC));
        }

        //设置高亮显示
        nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("title"));
        nativeSearchQueryBuilder.withHighlightBuilder(new HighlightBuilder()
                .preTags("<span style=\"color:red\">")
                .postTags("</span>"));

        //3.设置查询的条件 QueryBuilders是ES中的查询条件构造器，设置查询条件，matchQuery关键字支持分词,multiMatchQuery为多条件查询
        nativeSearchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(keywords, "title", "brand", "category"));

        //4.构建查询对象
        NativeSearchQuery nativeSearchQuery = nativeSearchQueryBuilder.build();

        //5.执行查询
        SearchHits<SkuInfo> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, SkuInfo.class);

        //获取高亮结果进行高亮部分进行替换
        for (SearchHit<SkuInfo> searchHit : searchHits) {
            //获取高亮内容
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            //获取标题的高亮结果并替换，如果为空则为原标题内容，否则进行替换
            searchHit.getContent().setTitle(highlightFields.get("title") == null
                    ? searchHit.getContent().getTitle()
                    : highlightFields.get("title").get(0));
        }

        //获取分组结果 terms桶聚合，为字段中每种词条创建一个桶
        Terms terms = searchHits.getAggregations().get("categorygroup");
        //将分类分页结果存入集合中
        List<String> categoryList = getStringsCategoryList(terms);

        //获取品牌分组结果 terms桶聚合，为字段中每种词条创建一个桶
        Terms termsBrand = searchHits.getAggregations().get("brandgroup");
        //将品牌分页结果存入集合中
        List<String> brandList = getStringsBrandList(termsBrand);

        //获取规格分组结果 terms桶聚合，为字段中每种词条创建一个桶
        Terms termsSpec = searchHits.getAggregations().get("skuSpecgroup");
        //将规格分页结果存入集合中
        Map<String, Set<String>> specMap = getStringSetMap(termsSpec);

        //对搜索searchHits集合进行分页封装
        SearchPage<SkuInfo> searchPage = SearchHitSupport.searchPageFor(searchHits, nativeSearchQuery.getPageable());
        List<SkuInfo> skuInfoList = new ArrayList<>();

        //遍历取出查询的商品信息
        for (SearchHit<SkuInfo> searchHit : searchPage.getContent()) {
            SkuInfo content = searchHit.getContent();
            //获取商品信息存入集合
            skuInfoList.add(content);
        }
        // 获取搜索到的数据
        Map<String, Object> resultMap = new HashMap<>();
        //商品信息集合存入map
        resultMap.put("rows", skuInfoList);
        //总分页数
        resultMap.put("totalPages", searchPage.getTotalPages());
        //总记录数
        resultMap.put("total", searchHits.getTotalHits());
        //分组分类结果
        resultMap.put("categoryList", categoryList);
        //品牌分组结果
        resultMap.put("brandList", brandList);
        //规格分组结果
        resultMap.put("specMap", specMap);
        resultMap.put("pageNum", pageNo);
        resultMap.put("pageSize", pageSize);
        //6.返回结果
        return resultMap;
    }

    /**
     * 获取分类分组结果方法
     *
     * @param terms
     * @return
     */
    private List<String> getStringsCategoryList(Terms terms) {
        List<String> categoryList = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            String category = bucket.getKeyAsString();
            categoryList.add(category);
        }
        return categoryList;
    }

    /**
     * 获取分类品牌分组结果方法
     *
     * @param termsBrand
     * @return
     */
    private List<String> getStringsBrandList(Terms termsBrand) {
        List<String> brandList = new ArrayList<>();
        for (Terms.Bucket bucket : termsBrand.getBuckets()) {
            String brand = bucket.getKeyAsString();
            brandList.add(brand);
        }
        return brandList;
    }

    /**
     * 获取分类分组结果方法
     *
     * @param termsSpec
     * @return
     */
    private Map<String, Set<String>> getStringSetMap(Terms termsSpec) {
        Map<String, Set<String>> specMap = new HashMap<>();
        Set<String> specList = new HashSet<>();
        for (Terms.Bucket bucket : termsSpec.getBuckets()) {
            String spec = bucket.getKeyAsString();
            specList.add(spec);
        }
        for (String specs : specList) {
            Map<String, String> map = JSON.parseObject(specs, Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                //获取规格名字
                String key = entry.getKey();
                //获取选项值
                String value = entry.getValue();
                //获取当前规格对应的选项值
                Set<String> specValues = specMap.get(key);
                if (specValues == null) {
                    specValues = new HashSet<>();
                }
                //将遍历出来的选项值存入set集合中去除重复项
                specValues.add(value);
                //将对应的key，value存入map中
                specMap.put(key, specValues);
            }
        }
        return specMap;
    }
}