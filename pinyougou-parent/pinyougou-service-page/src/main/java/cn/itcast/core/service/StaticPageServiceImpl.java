package cn.itcast.core.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StaticPageServiceImpl implements StaticPageService,ServletContextAware {

    @Autowired
    private FreeMarkerConfigurer freemarkerConfig;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private ItemDao itemDao;

    public void index(Long id){
        Writer writer=null;
        //输出路径(绝对路径)
        String path=getPath("/"+id+".html");
        Configuration conf = freemarkerConfig.getConfiguration();
        try {
            //获取模板对象
            Template template = conf.getTemplate("item.ftl");

            //获取数据
            Map<String,Object> root=new HashMap<>();
            //获取商品详情对象
            GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
            root.put("goodsDesc",goodsDesc);
            //获取商品对象
            Goods goods = goodsDao.selectByPrimaryKey(id);
            root.put("goods",goods);
            //获取商品一级分类名称
            root.put("itemCat1",goods.getCategory1Id());
            //获取商品二级分类名称
            root.put("itemCat2",goods.getCategory2Id());
            //获取商品三级分类名称
            root.put("itemCat3",goods.getCategory3Id());

            //获取库存对象
            ItemQuery itemQuery=new ItemQuery();
             itemQuery.createCriteria().andGoodsIdEqualTo(id).andStatusEqualTo("1");
            List<Item> itemList = itemDao.selectByExample(itemQuery);
            root.put("itemList",itemList);


            //获取输出流
             writer=new OutputStreamWriter(new FileOutputStream(path),"UTF-8");
            template.process(root,writer);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (writer!=null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPath(String path){
        return servletContext.getRealPath(path);
    }

    @Autowired
    private ServletContext servletContext;
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext=servletContext;
    }
}
