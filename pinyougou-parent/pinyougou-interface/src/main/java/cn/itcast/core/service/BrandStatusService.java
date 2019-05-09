package cn.itcast.core.service;

import cn.itcast.core.pojo.brandApply.BrandApply;
import entity.PageResult;

public interface BrandStatusService {
    PageResult search(Integer page, Integer rows, BrandApply brandApply);

    void delete(Long[] ids);

    void updateStatus(Long[] ids, String status);
}
