package cn.itcast.core.controller;


import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojogroup.SpecificationVo;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference
    private SpecificationService specificationService;
@RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Specification specification){

    return specificationService.search(page,rows,specification,specification);
 }

 @RequestMapping("/add")
    public Result add(@RequestBody SpecificationVo specificationVo){
    try{
    specificationService.add(specificationVo);
    return new Result(true,"添加成功");
    }catch (Exception e){
        return new Result(false,"添加失败");
    }
 }

 //数据回显
    @RequestMapping("/findOne")
    public SpecificationVo findOne(Long id){
    return specificationService.findOne(id);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody SpecificationVo specificationVo){
        try{
            specificationService.update(specificationVo);
            return new Result(true,"添加成功");
        }catch (Exception e){
            return new Result(false,"添加失败");
        }
    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            specificationService.delete(ids);
            return new Result(true,"删除成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
    return specificationService.selectOptionList();
    }
}
