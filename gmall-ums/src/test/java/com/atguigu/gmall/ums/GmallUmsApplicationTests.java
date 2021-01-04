package com.atguigu.gmall.ums;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class GmallUmsApplicationTests {

    @Test
    void contextLoads() {

        String code = StringUtils.substring(UUID.randomUUID().toString(), 0, 4);
        System.out.println("code = " + code);
    }

}
