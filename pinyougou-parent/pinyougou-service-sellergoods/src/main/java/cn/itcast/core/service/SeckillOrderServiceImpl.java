package cn.itcast.core.service;

import cn.itcast.core.dao.seckill.SeckillOrderDao;
import cn.itcast.core.pojo.seckill.SeckillOrder;
import cn.itcast.core.pojo.seckill.SeckillOrderQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderDao seckillOrderDao;

    @Override
    public PageResult search(Integer page, Integer rows, SeckillOrder seckillOrder) {
        PageHelper.startPage(page, rows);
        if ("".equals(seckillOrder.getStatus()) && null == seckillOrder.getId()){
            Page<SeckillOrder> page1 = (Page<SeckillOrder>) seckillOrderDao.selectByExample(null);
            return new PageResult(page1.getTotal(), page1.getResult());
        }
        SeckillOrderQuery seckillOrderQuery = new SeckillOrderQuery();
        SeckillOrderQuery.Criteria criteria = seckillOrderQuery.createCriteria();
        if (null != seckillOrder.getStatus() && !"".equals(seckillOrder.getStatus())) {
            criteria.andStatusEqualTo(seckillOrder.getStatus());
        }

        if (null != seckillOrder.getId() && !"".equals(seckillOrder.getId().toString().trim())) {
            criteria.andIdEqualTo(seckillOrder.getId());
        }
        Page<SeckillOrder> page1 = (Page<SeckillOrder>) seckillOrderDao.selectByExample(seckillOrderQuery);
        return new PageResult(page1.getTotal(), page1.getResult());
    }

    @Override
    public SeckillOrder findOne(Long id) {
        SeckillOrder seckillOrder = seckillOrderDao.selectByPrimaryKey(id);
        return seckillOrder;
    }
}
