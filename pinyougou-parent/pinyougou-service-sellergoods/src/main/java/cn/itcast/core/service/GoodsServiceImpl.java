package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import pojogroup.GoodsVo;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class GoodsServiceImpl implements GoodsService{

    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private SellerDao sellerDao;
    @Autowired
    private BrandDao brandDao;
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private Destination topicPageAndSolrDestination;
    @Autowired
    private Destination queueSolrDeleteDestination;
    @Autowired
    private JmsTemplate jmsTemplate;


    @Override
    public void add(GoodsVo goodsVo) {

        //商品表
        //刚添加商品处于未审核状态
        goodsVo.getGoods().setAuditStatus("0");
        goodsDao.insertSelective(goodsVo.getGoods());

        //商品详情表
        goodsVo.getGoodsDesc().setGoodsId(goodsVo.getGoods().getId());
        goodsDescDao.insertSelective(goodsVo.getGoodsDesc());

        if ("1".equals(goodsVo.getGoods().getIsEnableSpec())){
            //库存表
            List<Item> itemList = goodsVo.getItemList();
            for (Item item : itemList) {
                String title = goodsVo.getGoods().getGoodsName();
                String spec = item.getSpec();
                Map<String,String> map = JSON.parseObject(spec, Map.class);
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title+=" "+entry.getValue();
                }
                //设置标题
                item.setTitle(title);

                setAttribute(item,goodsVo);
                itemDao.insertSelective(item);
        }

        }else {
            Item item = new Item();

            item.setTitle(goodsVo.getGoods().getGoodsName());
            setAttribute(item,goodsVo);
            //价格默认值
            item.setPrice(new BigDecimal(0));
            //库存默认值
            item.setNum(0);
            //默认启用
            item.setStatus("1");
            itemDao.insertSelective(item);
        }
    }

    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {

        PageHelper.startPage(page,rows);
        GoodsQuery query = new GoodsQuery();
        query.setOrderByClause("id desc");
        GoodsQuery.Criteria criteria = query.createCriteria();

        if (null!=goods.getAuditStatus()&&!"".equals(goods.getAuditStatus().trim())){
            criteria.andAuditStatusEqualTo(goods.getAuditStatus().trim());
        }
        if (null!=goods.getGoodsName()&&!"".equals(goods.getGoodsName().trim())){
            criteria.andGoodsNameLike("%"+goods.getGoodsName().trim()+"%");
        }
        if (null != goods.getSellerId()) {
            //商家后台管理查询分页结果集的条件
            criteria.andSellerIdEqualTo(goods.getSellerId());
        }
        //只查询未删除的
        criteria.andIsDeleteIsNull();

        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(query);

        return new PageResult(p.getTotal(),p.getResult());
    }

    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo goodsVo = new GoodsVo();

        Goods goods = goodsDao.selectByPrimaryKey(id);
        goodsVo.setGoods(goods);

        goodsVo.setGoodsDesc(goodsDescDao.selectByPrimaryKey(id));

        ItemQuery query = new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(id);
        goodsVo.setItemList(itemDao.selectByExample(query));
        return goodsVo;
    }

    @Override
    public void update(GoodsVo goodsVo) {

        goodsDao.updateByPrimaryKeySelective(goodsVo.getGoods());
        goodsDescDao.updateByPrimaryKeySelective(goodsVo.getGoodsDesc());

        ItemQuery query=new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(goodsVo.getGoods().getId());
        itemDao.deleteByExample(query);
        if ("1".equals(goodsVo.getGoods().getIsEnableSpec())){
            //库存表
            List<Item> itemList = goodsVo.getItemList();
            for (Item item : itemList) {
                String title = goodsVo.getGoods().getGoodsName();
                String spec = item.getSpec();
                Map<String,String> map = JSON.parseObject(spec, Map.class);
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title+=" "+entry.getValue();
                }
                //设置标题
                item.setTitle(title);

                setAttribute(item,goodsVo);
                itemDao.insertSelective(item);
            }

        }else {
            Item item = new Item();

            item.setTitle(goodsVo.getGoods().getGoodsName());
            setAttribute(item,goodsVo);
            //价格默认值
            item.setPrice(new BigDecimal(0));
            //库存默认值
            item.setNum(0);
            //默认启用
            item.setStatus("1");
            itemDao.insertSelective(item);
        }

    }

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void updateStatus(Long[] ids, String status) {

        Goods goods = new Goods();

        goods.setAuditStatus(status);

        for (Long id : ids) {
            goods.setId(id);
            goodsDao.updateByPrimaryKeySelective(goods);
            if ("1".equals(status)){

                jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(String.valueOf(id));
                    }
                });



            }

        }
    }

    @Override
    public void delete(Long[] ids) {
        Goods goods = new Goods();
        goods.setIsDelete("1");
        for (Long id : ids) {
            goods.setId(id);
            goodsDao.updateByPrimaryKeySelective(goods);

            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(String.valueOf(id));
                }
            });


        }
    }


    public void setAttribute(Item item,GoodsVo goodsVo){
        //选取一张图片作为搜索页面的图片
        String itemImages = goodsVo.getGoodsDesc().getItemImages();
        List<Map> maps = JSON.parseArray(itemImages, Map.class);
        if (maps!=null&&maps.size()>0){
            item.setImage(String.valueOf(maps.get(0).get("url")));
        }
        //时间
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());

        //商品ID
        item.setGoodsId(goodsVo.getGoods().getId());
        //第三级商品分类ID
        item.setCategoryid(goodsVo.getGoods().getCategory3Id());
        //商家ID
        item.setSellerId(goodsVo.getGoods().getSellerId());
        //商家名称
        Seller seller = sellerDao.selectByPrimaryKey(goodsVo.getGoods().getSellerId());
        item.setSeller(seller.getName());

        //第三级分类名称
        ItemCat itemCat = itemCatDao.selectByPrimaryKey(goodsVo.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());

        //品牌名称
        Brand brand = brandDao.selectByPrimaryKey(goodsVo.getGoods().getBrandId());
        item.setBrand(brand.getName());
    }
}
