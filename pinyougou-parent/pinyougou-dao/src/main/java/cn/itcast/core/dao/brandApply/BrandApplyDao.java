package cn.itcast.core.dao.brandApply;

import cn.itcast.core.pojo.brandApply.BrandApply;
import cn.itcast.core.pojo.brandApply.BrandApplyQuery;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BrandApplyDao {
    int countByExample(BrandApplyQuery example);

    int deleteByExample(BrandApplyQuery example);

    int deleteByPrimaryKey(Long id);

    int insert(BrandApply record);

    int insertSelective(BrandApply record);

    List<BrandApply> selectByExample(BrandApplyQuery example);

    BrandApply selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BrandApply record, @Param("example") BrandApplyQuery example);

    int updateByExample(@Param("record") BrandApply record, @Param("example") BrandApplyQuery example);

    int updateByPrimaryKeySelective(BrandApply record);

    int updateByPrimaryKey(BrandApply record);
}