package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.vo.GroupVo;
import com.atguigu.gmall.pms.entity.vo.ItemGroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author ll
 * @email ll@atguigu.com
 * @date 2020-12-14 19:34:31
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<GroupVo> queryAttrsByCategoryId(Long categoryId);

    List<ItemGroupVo> queryItemGroupVosByCategoryIdAndSpuIdAndSkuId(Long cid, Long spuId, Long skuId);
}

