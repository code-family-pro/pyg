package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class ContentServiceImpl implements ContentService {
	
	@Autowired
	private ContentDao contentDao;

	@Override
	public List<Content> findAll() {
		List<Content> list = contentDao.selectByExample(null);
		return list;
	}

	@Override
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(Content content) {
		contentDao.insertSelective(content);
	}

	@Autowired
	private RedisTemplate redisTemplate;
	@Override
	public void edit(Content content) {

		Content c = contentDao.selectByPrimaryKey(content.getId());
		if (!c.getCategoryId().equals(content.getCategoryId())){
			redisTemplate.boundHashOps("content").delete(c.getCategoryId());
		}
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		contentDao.updateByPrimaryKeySelective(content);
	}

	@Override
	public Content findOne(Long id) {
		Content content = contentDao.selectByPrimaryKey(id);
		return content;
	}

	@Override
	public void delAll(Long[] ids) {
		if(ids != null){
			for(Long id : ids){
				contentDao.deleteByPrimaryKey(id);
			}
		}
	}

    @Override
    public List<Content> findByCategoryId(Long categoryId) {

		List<Content> contentList = null;
				//先查询缓存
				contentList= (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);

		//判断redis缓存中是否有,没有就查询数据库
		if (contentList==null || contentList.size()==0){
			ContentQuery query = new ContentQuery();
			query.createCriteria().andCategoryIdEqualTo(categoryId).andStatusEqualTo("1");
			query.setOrderByClause("sort_order desc");
			contentList= contentDao.selectByExample(query);

			//数据库查询出来以后保存一份到redis缓存中
			redisTemplate.boundHashOps("content").put(categoryId,contentList);

			//设置缓存保留时间
			redisTemplate.boundHashOps("content").expire(4, TimeUnit.HOURS);

		}
		return contentList;


    }

}
