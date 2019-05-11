package cn.itcast.core.service;

import cn.itcast.core.dao.brandApply.BrandApplyDao;
import cn.itcast.core.pojo.brandApply.BrandApply;
import cn.itcast.core.pojo.brandApply.BrandApplyQuery;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@Service
@Transactional
public class BrandStatusServiceImpl implements BrandStatusService {

    @Autowired
    BrandApplyDao brandApplyDao;

    @Override
    public PageResult search(Integer page, Integer rows, BrandApply brandApply) {

        PageHelper.startPage(page,rows);
        BrandApplyQuery query = new BrandApplyQuery();
        BrandApplyQuery.Criteria criteria = query.createCriteria();

        if (null!=brandApply.getStatus()&&!"".equals(brandApply.getStatus().trim())){
            criteria.andStatusEqualTo(brandApply.getStatus().trim());
        }
        if (null!=brandApply.getBrandName()&&!"".equals(brandApply.getBrandName().trim())){
            criteria.andBrandNameEqualTo("%"+brandApply.getBrandName().trim()+"%");
        }
        if (null != brandApply.getSellerId()) {
            //商家后台管理查询分页结果集的条件
            criteria.andSellerIdEqualTo(brandApply.getSellerId());
        }

        Page<BrandApply> p = (Page<BrandApply>) brandApplyDao.selectByExample(query);

        return new PageResult(p.getTotal(),p.getResult());
    }

    @Override
    public void delete(Long[] ids) {
        if (ids != null&&ids.length > 0){
            for (Long id : ids) {
                brandApplyDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        if (ids != null&&ids.length > 0){
            for (Long id : ids) {
                BrandApply brandApply = brandApplyDao.selectByPrimaryKey(id);
                brandApply.setStatus(status);
                brandApplyDao.updateByPrimaryKeySelective(brandApply);
            }
        }
    }
}
