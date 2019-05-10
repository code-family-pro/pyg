package cn.itcast.core.service;

import cn.itcast.core.dao.seckill.SeckillGoodsDao;
import cn.itcast.core.pojo.seckill.SeckillGoods;
import cn.itcast.core.pojo.seckill.SeckillGoodsQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private SeckillGoodsDao seckillGoodsDao;

    @Override
    public PageResult search(Integer page, Integer rows, SeckillGoods seckillGoods) {
        PageHelper.startPage(page, rows);
        if ("".equals(seckillGoods.getStatus())){
            Page<SeckillGoods> page1 = (Page<SeckillGoods>) seckillGoodsDao.selectByExample(null);
            return new PageResult(page1.getTotal(), page1.getResult());
        }
        SeckillGoodsQuery seckillGoodsQuery = new SeckillGoodsQuery();
        seckillGoodsQuery.createCriteria().andStatusEqualTo(seckillGoods.getStatus());
        Page<SeckillGoods> page1 = (Page<SeckillGoods>) seckillGoodsDao.selectByExample(seckillGoodsQuery);
        return new PageResult(page1.getTotal(), page1.getResult());

    }

    @Override
    public SeckillGoods findOne(Long id) {
        return seckillGoodsDao.selectByPrimaryKey(id);
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        SeckillGoods seckillGoods = new SeckillGoods();
        seckillGoods.setStatus(status);
        seckillGoods.setCheckTime(new Date());
        if (null != ids && ids.length > 0){
            for (Long id : ids) {
                seckillGoods.setId(id);
                seckillGoodsDao.updateByPrimaryKeySelective(seckillGoods);
            }
        }
    }
}
