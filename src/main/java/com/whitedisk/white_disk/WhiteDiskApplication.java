package com.whitedisk.white_disk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan("com.whitedisk.white_disk.mapper")
@EnableScheduling
@EnableTransactionManagement
@SpringBootApplication
public class WhiteDiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhiteDiskApplication.class, args);
    }

}
