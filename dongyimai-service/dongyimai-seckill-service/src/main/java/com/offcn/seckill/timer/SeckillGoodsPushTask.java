package com.offcn.seckill.timer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.offcn.seckill.dao.SeckillGoodsMapper;
import com.offcn.seckill.pojo.SeckillGoods;
import com.offcn.utils.DateUtil;
import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillGoodsPushTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 每30秒执行一次
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void loadGoodsPushRedis() {
        List<Date> dateMenus = DateUtil.getDateMenus();
        for (Date dateMenu : dateMenus) {
            QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<>();
            //1.商品必须通过审核
            queryWrapper.eq("status", "1");
            //2.库存数量大于0
            queryWrapper.gt("stock_count", 0);
            //3.活动时间区间 结束时间需加2个小时
            queryWrapper.ge("start_time", DateUtil.date2StrFull(dateMenu));
            queryWrapper.lt("end_time", DateUtil.date2StrFull(DateUtil.addDateHour(dateMenu, 2)));
            //构建redis的键
            String keyTime = DateUtil.date2Str(dateMenu);
            //5.redis中不存在该秒杀商品
            Set keys = redisTemplate.boundHashOps("SeckillGoods_" + keyTime).keys();
            if (!Collections.isEmpty(keys)) {
                queryWrapper.notIn("id", keys);
            }
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(queryWrapper);
            for (SeckillGoods goods : seckillGoodsList) {
                //将数据库中秒杀商品信息存入redis中
                System.out.println("SeckillGoods_" + keyTime);
                redisTemplate.boundHashOps("SeckillGoods_" + keyTime).put(goods.getId(), goods);
                SeckillGoods SeckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + keyTime).get(goods.getId());
                System.out.println(SeckillGoods);
                //设置商品数量
                Long[] ids = pushIds(goods.getStockCount(), goods.getId());
                redisTemplate.boundListOps("SeckillGoodsCountQueue_" + goods.getId()).leftPushAll(ids);
                redisTemplate.boundHashOps("SeckillGoodsCount").increment(goods.getId(), goods.getStockCount());
            }
        }
    }

    /**
     * 将商品ID存入到数组中
     *
     * @param len:长度
     * @param id:值
     * @return
     */
    public Long[] pushIds(int len, Long id) {
        Long[] ids = new Long[len];
        Arrays.fill(ids, id);
        return ids;
    }
}