package com.ustb.config;

import com.ustb.entity.UserEntity;
import com.ustb.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    // run方法的作用：在启动时自动执行run方法，查询数据库是否存在admin和user，如果不存在则创建，确保系统开箱即有可登录的用户。
    @Override
    public void run(String... args) {
        if (userMapper.findByUsername("admin") == null) {
            userMapper.insert(UserEntity.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))  // 加密器加密
                    .role("ADMIN")
                    .build());
        }
        if (userMapper.findByUsername("user") == null) {
            userMapper.insert(UserEntity.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .role("USER")
                    .build());
        }
    }
}
