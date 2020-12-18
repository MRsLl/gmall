package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author ll
 * @email ll@atguigu.com
 * @date 2020-12-14 20:29:09
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
	
}
