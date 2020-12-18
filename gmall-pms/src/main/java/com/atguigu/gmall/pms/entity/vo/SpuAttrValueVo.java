package com.atguigu.gmall.pms.entity.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;

import java.util.List;

public class SpuAttrValueVo extends SpuAttrValueEntity {

    public void setValueSelected(List<String> valueSelected){
        this.setAttrValue(valueSelected.get(0));
    }
}
