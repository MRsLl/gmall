package com.atguigu.gmall.wms;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GmallWmsApplicationTests {

    @Autowired
    private WareSkuMapper wareSkuMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void mapperTest() {
        List<WareSkuEntity> wareSkuEntities = wareSkuMapper.checkSkuStock(30l, 50);
        System.out.println("wareSkuEntities.toString() = " + wareSkuEntities.toString());
    }

    @Test
    void mapperTest1() {
        wareSkuMapper.tryLock(1L,2);
    }

}
