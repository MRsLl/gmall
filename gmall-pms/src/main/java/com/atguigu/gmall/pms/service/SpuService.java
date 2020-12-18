package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SpuEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author ll
 * @email ll@atguigu.com
 * @date 2020-12-14 19:34:30
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo querySpuByPageAndCategoryId(Long categoryId,PageParamVo pageParamVo);

    void saveAllInformation(SpuVo spuVo);
}

