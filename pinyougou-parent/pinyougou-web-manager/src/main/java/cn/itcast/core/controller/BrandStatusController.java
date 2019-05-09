package cn.itcast.core.controller;

import cn.itcast.core.pojo.brandApply.BrandApply;
import cn.itcast.core.service.BrandStatusService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/brandStatus")
public class BrandStatusController {

    @Reference
    BrandStatusService brandStatusService;

    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody BrandApply brandApply){
        return brandStatusService.search(page,rows,brandApply);
    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandStatusService.delete(ids);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids,String status){
        try {
            brandStatusService.updateStatus(ids,status);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }
}
