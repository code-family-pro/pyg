package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BrandServiceImpl implements  BrandService {

    @Autowired
    private BrandDao brandDao;

    //查询所有
    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }

   //分页查询不带条件
    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum,pageSize);
        Page<Brand>page=(Page<Brand>) brandDao.selectByExample(null);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void add(Brand brand) {
        brandDao.insertSelective(brand);
    }

    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    @Override
    public void delete(Long[] ids) {
        if (ids!=null&&ids.length>0){
            for (Long id : ids) {
                brandDao.deleteByPrimaryKey(id);
            }
        }
    }

    //分页查询带条件
    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Brand brand) {
        PageHelper.startPage(pageNum,pageSize);

        BrandQuery brandQuery = new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();

        if (null!=brand.getName() && !"".equals(brand.getName().trim())){
            criteria.andNameLike("%"+brand.getName().trim()+"%");
        }
        if (null!=brand.getFirstChar()&& !"".equals(brand.getFirstChar().trim())){
            criteria.andFirstCharEqualTo(brand.getFirstChar().trim());
        }
        Page<Brand>page=(Page<Brand>) brandDao.selectByExample(brandQuery);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public List<Map> selectOptionList() {
        return brandDao.selectOptionList();
    }
}
