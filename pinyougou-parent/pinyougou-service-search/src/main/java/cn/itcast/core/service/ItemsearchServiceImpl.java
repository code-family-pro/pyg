package cn.itcast.core.service;



import cn.itcast.core.pojo.item.Item;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service
public class ItemsearchServiceImpl implements ItemsearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {

        Map<String, Object> resultMap=new HashMap<>();

        List<String> list = searchCategory(searchMap);
        resultMap.put("categoryList", list);

        if (list!=null&& list.size()>0) {
            resultMap.putAll(searchBrandAndSpec(list.get(0)));
        }

        Map<String, Object> map = search2(searchMap);
        resultMap.putAll(map);

        return resultMap;
    }



    //查询品牌和规格
    public Map<String,Object> searchBrandAndSpec(String category){
        Map<String, Object> resultMap=new HashMap<>();

        Object typeid = redisTemplate.boundHashOps("itemCat").get(category);
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeid);
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeid);
        resultMap.put("brandList",brandList);
        resultMap.put("specList",specList);
        return resultMap;


    }

    //查询商品分类
    public List<String> searchCategory(Map<String,String> searchMap){


        Criteria criteria =new Criteria("item_keywords").is(searchMap.get("keywords"));
        Query query=new SimpleQuery(criteria);

        GroupOptions options = new GroupOptions();
        options.addGroupByField("item_category");
        query.setGroupOptions(options);

        ArrayList<String> list = new ArrayList<>();
        GroupPage<Item> page = solrTemplate.queryForGroupPage(query, Item.class);
        GroupResult<Item> groupResult = page.getGroupResult("item_category");
        Page<GroupEntry<Item>> entries = groupResult.getGroupEntries();
        List<GroupEntry<Item>> content = entries.getContent();
        for (GroupEntry<Item> entry : content) {
           list.add(entry.getGroupValue());
        }

        return list;

    }

    //查询高亮分页
    public Map<String,Object> search2(Map<String,String> searchMap) {

        Map<String, Object> resultMap=new HashMap<>();

        searchMap.put("keywords",searchMap.get("keywords").replaceAll(" ",""));

        Criteria criteria =new Criteria("item_keywords").is(searchMap.get("keywords"));
        HighlightQuery query = new SimpleHighlightQuery(criteria);

        String pageNo = searchMap.get("pageNo");
        String pageSize = searchMap.get("pageSize");
        query.setOffset((Integer.parseInt(pageNo)-1)*Integer.parseInt(pageSize));
        query.setRows(Integer.parseInt(pageSize));

        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);

        //商品分类
        if (searchMap.get("category")!=null && !"".equals(searchMap.get("category").trim())){

            FilterQuery filterQuery=new SimpleFilterQuery();
            filterQuery.addCriteria(new Criteria("item_category").is(searchMap.get("category")));
            query.addFilterQuery(filterQuery);
        }

        //品牌
        if (searchMap.get("brand")!=null && !"".equals(searchMap.get("brand").trim())){

            FilterQuery filterQuery=new SimpleFilterQuery();
            filterQuery.addCriteria(new Criteria("item_brand").is(searchMap.get("brand")));
            query.addFilterQuery(filterQuery);
        }

        //规格
        if (searchMap.get("spec")!=null &&!"" .equals(searchMap.get("spec"))){
            FilterQuery filterQuery=new SimpleFilterQuery();
            Map<String,String> specMap = JSON.parseObject(searchMap.get("spec"), Map.class);
            Set<Map.Entry<String, String>> entrySet = specMap.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                filterQuery.addCriteria(new Criteria("item_spec_"+entry.getKey()).is(entry.getValue()));
            }

            query.addFilterQuery(filterQuery);
        }

        //价格
        if (searchMap.get("price")!=null && !"".equals(searchMap.get("price").trim())){

            FilterQuery filterQuery=new SimpleFilterQuery();
            String[] split = searchMap.get("price").split("-");
            if (!"*".equals(split[1])){
                filterQuery.addCriteria(new Criteria("item_price").between(split[0],split[1],true,true));
            }else {
                filterQuery.addCriteria(new Criteria("item_price").greaterThanEqual(split[0]));

            }
            query.addFilterQuery(filterQuery);
        }

        if (searchMap.get("sortField")!=null && !"".equals(searchMap.get("sortField"))){

            if ("ASC".equals(searchMap.get("sort"))) {
                query.addSort(new Sort(Sort.Direction.ASC, "item_" + searchMap.get("sortField")));
            }else {
                query.addSort(new Sort(Sort.Direction.DESC, "item_" + searchMap.get("sortField")));

            }
        }

        HighlightPage<Item> page = solrTemplate.queryForHighlightPage(query, Item.class);

        List<HighlightEntry<Item>> highlighted = page.getHighlighted();
        if (highlighted!=null && highlighted.size()>0){
            for (HighlightEntry<Item> item : highlighted) {
                Item entity = item.getEntity();
                List<HighlightEntry.Highlight> highlights = item.getHighlights();
                if (highlights!=null&&highlights.size()>0) {
                    entity.setTitle(highlights.get(0).getSnipplets().get(0));
                }
            }

        }
        //结果集
        resultMap.put("rows",page.getContent());
        //总条数
        resultMap.put("total",page.getTotalElements());
        //总页数
        resultMap.put("totalPages",page.getTotalPages());

        return resultMap;
    }

    //查询普通分页
    public Map<String,Object> search1(Map<String,String> searchMap){
        Map<String, Object> resultMap=new HashMap<>();

        Criteria criteria =new Criteria("item_keywords").is(searchMap.get("keywords"));
        SimpleQuery query = new SimpleQuery(criteria);

        String pageNo = searchMap.get("pageNo");
        String pageSize = searchMap.get("pageSize");

        query.setOffset((Integer.parseInt(pageNo)-1)*Integer.parseInt(pageSize));
        query.setRows(Integer.parseInt(pageSize));

        ScoredPage<Item> page = solrTemplate.queryForPage(query, Item.class);

        //结果集
        resultMap.put("rows",page.getContent());
        //总条数
        resultMap.put("total",page.getTotalElements());
        //总页数
        resultMap.put("totalPages",page.getTotalPages());
        return resultMap;
    }
}
