package com.offcn.sellergoods.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.entity.PageResult;
import com.offcn.sellergoods.dao.*;
import com.offcn.sellergoods.group.GoodsEntity;
import com.offcn.sellergoods.pojo.*;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/****
 * @Author:ujiuye
 * @Description:Goods业务层接口实现类
 * @Date 2021/2/1 14:19
 *****/
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {


    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private GoodsDescMapper goodsDescMapper;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private ItemCatMapper itemCatMapper;

    @Resource
    private BrandMapper brandMapper;

    /**
     * Goods条件+分页查询
     *
     * @param goods 查询条件
     * @param page  页码
     * @param size  页大小
     * @return 分页结果
     */
    @Override
    public PageResult<Goods> findPage(Goods goods, int page, int size) {
        Page<Goods> mypage = new Page<>(page, size);
        QueryWrapper<Goods> queryWrapper = this.createQueryWrapper(goods);
        IPage<Goods> iPage = this.page(mypage, queryWrapper);
        return new PageResult<Goods>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * Goods分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<Goods> findPage(int page, int size) {
        Page<Goods> mypage = new Page<>(page, size);
        IPage<Goods> iPage = this.page(mypage, new QueryWrapper<Goods>());

        return new PageResult<Goods>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * Goods条件查询
     *
     * @param goods
     * @return
     */
    @Override
    public List<Goods> findList(Goods goods) {
        //构建查询条件
        QueryWrapper<Goods> queryWrapper = this.createQueryWrapper(goods);
        //根据构建的条件查询数据
        return this.list(queryWrapper);
    }


    /**
     * Goods构建查询对象
     *
     * @param goods
     * @return
     */
    public QueryWrapper<Goods> createQueryWrapper(Goods goods) {
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        if (goods != null) {
            // 主键
            if (!StringUtils.isEmpty(goods.getId())) {
                queryWrapper.eq("id", goods.getId());
            }
            // 商家ID
            if (!StringUtils.isEmpty(goods.getSellerId())) {
                queryWrapper.eq("seller_id", goods.getSellerId());
            }
            // SPU名
            if (!StringUtils.isEmpty(goods.getGoodsName())) {
                queryWrapper.eq("goods_name", goods.getGoodsName());
            }
            // 默认SKU
            if (!StringUtils.isEmpty(goods.getDefaultItemId())) {
                queryWrapper.eq("default_item_id", goods.getDefaultItemId());
            }
            // 状态
            if (!StringUtils.isEmpty(goods.getAuditStatus())) {
                queryWrapper.eq("audit_status", goods.getAuditStatus());
            }
            // 是否上架
            if (!StringUtils.isEmpty(goods.getIsMarketable())) {
                queryWrapper.eq("is_marketable", goods.getIsMarketable());
            }
            // 品牌
            if (!StringUtils.isEmpty(goods.getBrandId())) {
                queryWrapper.eq("brand_id", goods.getBrandId());
            }
            // 副标题
            if (!StringUtils.isEmpty(goods.getCaption())) {
                queryWrapper.eq("caption", goods.getCaption());
            }
            // 一级类目
            if (!StringUtils.isEmpty(goods.getCategory1Id())) {
                queryWrapper.eq("category1_id", goods.getCategory1Id());
            }
            // 二级类目
            if (!StringUtils.isEmpty(goods.getCategory2Id())) {
                queryWrapper.eq("category2_id", goods.getCategory2Id());
            }
            // 三级类目
            if (!StringUtils.isEmpty(goods.getCategory3Id())) {
                queryWrapper.eq("category3_id", goods.getCategory3Id());
            }
            // 小图
            if (!StringUtils.isEmpty(goods.getSmallPic())) {
                queryWrapper.eq("small_pic", goods.getSmallPic());
            }
            // 商城价
            if (!StringUtils.isEmpty(goods.getPrice())) {
                queryWrapper.eq("price", goods.getPrice());
            }
            // 分类模板ID
            if (!StringUtils.isEmpty(goods.getTypeTemplateId())) {
                queryWrapper.eq("type_template_id", goods.getTypeTemplateId());
            }
            // 是否启用规格
            if (!StringUtils.isEmpty(goods.getIsEnableSpec())) {
                queryWrapper.eq("is_enable_spec", goods.getIsEnableSpec());
            }
            // 是否删除
            if (!StringUtils.isEmpty(goods.getIsDelete())) {
                queryWrapper.eq("is_delete", goods.getIsDelete());
            }
        }
        return queryWrapper;
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        //逻辑删除
        Goods goods = this.getById(id);
        //检查是否下架的商品
        if (!goods.getIsMarketable().equals("0")) {
            throw new RuntimeException("必须先下架再删除！");
        }
        goods.setIsDelete("1");
        //未审核
        goods.setAuditStatus("0");
        this.updateById(goods);
    }

    /**
     * 修改Goods
     *
     * @param goodsEntity
     */
    @Override
    public void update(GoodsEntity goodsEntity) {
        //将审核状态重新设置为未审核
        goodsEntity.getGoods().setAuditStatus("0");
        //1.修改SPU的信息
        this.updateById(goodsEntity.getGoods());
        goodsEntity.getGoodsDesc().setGoodsId(goodsEntity.getGoods().getId());
        //2.修改商品扩展信息
        goodsDescMapper.updateById(goodsEntity.getGoodsDesc());
        //3.先根据商品ID删除SKU信息
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("goods_id", goodsEntity.getGoods().getId());
        itemMapper.delete(queryWrapper);
        //4.重新添加SKU信息
        this.saveItemList(goodsEntity);
    }

    private void saveItemList(GoodsEntity goodsEntity) {
        //有启动规格
        if (goodsEntity.getGoods().getIsEnableSpec().equals("1")) {
            List<Item> itemList = goodsEntity.getItemList();
            if (!StringUtils.isEmpty(itemList)) {
                //遍历sku列表，每一个sku对应就是tb_item表一个记录
                for (int i = 0; i < itemList.size(); i++) {
                    Item item = itemList.get(i);
                    String title = goodsEntity.getGoods().getGoodsName();
                    String spec = item.getSpec();
                    Map<String, String> map = JSON.parseObject(spec, Map.class);
                    for (String key : map.keySet()) {
                        String value = map.get(key);
                        title += " " + value;
                    }
                    //设置标题
                    item.setTitle(title);
                    setItemValue(goodsEntity, item);
                }
            }
        } else {
            //没有启动规格
            Item item = new Item();
            item.setPrice(goodsEntity.getGoods().getPrice());
            item.setSpec("{}");
            item.setNum(9999);
            item.setStatus("1");//sku状态 为正常
            item.setIsDefault("1");//默认sku
            //设置标题
            item.setTitle(goodsEntity.getGoods().getGoodsName());
            setItemValue(goodsEntity, item);
        }
    }

    /**
     * 增加Goods
     *
     * @param goodsEntity
     */
    @Override
    public void add(GoodsEntity goodsEntity) {
        //1.将数据存储到tb_goods表中
        Goods goods = goodsEntity.getGoods();
        this.save(goods);
        //2.将数据存储到tb_goods_desc表中
        Long goodsId = goods.getId();
        GoodsDesc goodsDesc = goodsEntity.getGoodsDesc();
        goodsDesc.setGoodsId(goodsId);
        goodsDescMapper.insert(goodsDesc);
        //3.将数据存储到tb_item表中
        //有启动规格
        if (goods.getIsEnableSpec().equals("1")) {
            List<Item> itemList = goodsEntity.getItemList();
            if (!StringUtils.isEmpty(itemList)) {
                for (int i = 0; i < itemList.size(); i++) {
                    Item item = itemList.get(i);
                    String goodsName = goods.getGoodsName();
                    String spec = item.getSpec();
                    Map<String, String> map = JSON.parseObject(spec, Map.class);
                    for (String key : map.keySet()) {
                        String value = map.get(key);
                        goodsName += " " + value;
                    }
                    item.setTitle(goodsName);
                    setItemValue(goodsEntity, item);
                }
            }
        } else {
            Item item = new Item();
            item.setPrice(goods.getPrice());
            item.setSpec("{}");
            item.setNum(9999);
            item.setStatus("1");
            item.setIsDefault("1");
            //设置标题
            item.setTitle(goods.getGoodsName());
            setItemValue(goodsEntity, item);
        }

    }

    private void setItemValue(GoodsEntity goodsEntity, Item item) {
        //设置图片
        String itemImages = goodsEntity.getGoodsDesc().getItemImages();
        List<Map> maps = JSON.parseArray(itemImages, Map.class);
        item.setImage((String) maps.get(0).get("url"));
        //设置第三级分类编号
        item.setCategoryId(goodsEntity.getGoods().getCategory3Id());
        //设置创建修改时间
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        //设置商品编号
        item.setGoodsId(goodsEntity.getGoods().getId());
        //设置卖家编号
        item.setSellerId(goodsEntity.getGoods().getSellerId());
        //设置第三级分类名
        ItemCat itemCat = itemCatMapper.selectById(goodsEntity.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());
        //设置品牌名
        Brand brand = brandMapper.selectById(goodsEntity.getGoods().getBrandId());
        item.setBrand(brand.getName());
        //保存到tb_item表中
        itemMapper.insert(item);
    }

    /**
     * 根据ID查询Goods
     *
     * @param id
     * @return
     */
    @Override
    public GoodsEntity findById(Long id) {
        //1.根据ID查询SPU信息
        Goods goods = this.getById(id);
        //2.根据ID查询商品扩展信息
        GoodsDesc goodsDesc = goodsDescMapper.selectById(id);
        //3.根据ID查询SKU列表
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("goods_id", id);
        List<Item> itemList = itemMapper.selectList(queryWrapper);
        //4.设置复合实体对象
        GoodsEntity goodsEntity = new GoodsEntity();
        goodsEntity.setGoods(goods);
        goodsEntity.setGoodsDesc(goodsDesc);
        goodsEntity.setItemList(itemList);
        return goodsEntity;
    }

    /**
     * 查询Goods全部数据
     *
     * @return
     */
    @Override
    public List<Goods> findAll() {
        return this.list(new QueryWrapper<Goods>());
    }

    @Override
    public void audit(Long goodsId) {
        //查询商品
        Goods goods = this.getById(goodsId);
        //判断商品是否已经删除
        if (goods.getIsDelete().equals("1")) {
            throw new RuntimeException("该商品已经删除！");
        }
        //实现上架和审核
        goods.setAuditStatus("1"); //审核通过
        goods.setIsMarketable("1"); //上架
        this.updateById(goods);
    }

    /**
     * 商品下架
     *
     * @param goodsId
     */
    @Override
    public void pull(Long goodsId) {
        Goods goods = this.getById(goodsId);
        if (goods.getIsDelete().equals("1")) {
            throw new RuntimeException("此商品已删除！");
        }
        goods.setIsMarketable("0");//下架状态
        this.updateById(goods);
    }

    /***
     * 商品上架
     * @param goodsId
     */
    @Override
    public void put(Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        //检查是否删除的商品
        if (goods.getIsDelete().equals("1")) {
            throw new RuntimeException("此商品已删除！");
        }
        if (!goods.getAuditStatus().equals("1")) {
            throw new RuntimeException("未通过审核的商品不能！");
        }
        //上架状态
        goods.setIsMarketable("1");
        goodsMapper.updateById(goods);
    }

    /***
     * 批量上架
     * @param ids:需要上架的商品ID集合
     * @return
     */
    @Override
    public int putMany(Long[] ids) {
        //上架商品，需要审核通过的商品
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        //商品未删除
        queryWrapper.eq("is_delete", "0");
        //商品已审核
        queryWrapper.eq("audit_status", "1");
        //对多个商品上架
        queryWrapper.in("id", Arrays.asList(ids));
        //更新数据
        Goods goods = new Goods();
        goods.setIsMarketable("1");//设置上架
        return goodsMapper.update(goods, queryWrapper);
    }

    /***
     * 批量下架
     * @param ids:需要下架的商品ID集合
     * @return
     */
    @Override
    public int pullMany(Long[] ids) {
        //下架商品，需要审核通过的商品
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        //商品未删除
        queryWrapper.eq("is_delete", "0");
        //商品已审核
        queryWrapper.eq("audit_status", "1");
        //对多个商品下架
        queryWrapper.in("id", Arrays.asList(ids));
        //更新数据
        Goods goods = new Goods();
        goods.setIsMarketable("0");//设置下架
        goods.setIsDelete("1");//删除商品下架
        return goodsMapper.update(goods, queryWrapper);
    }


}
