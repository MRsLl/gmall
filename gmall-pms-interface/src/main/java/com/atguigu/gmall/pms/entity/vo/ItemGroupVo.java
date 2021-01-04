package com.atguigu.gmall.pms.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class ItemGroupVo {

    private String groupName;

    private List<AttrValueVo> attrValues;
}
