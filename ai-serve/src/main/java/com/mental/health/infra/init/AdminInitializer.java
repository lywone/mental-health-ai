package com.mental.health.infra.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mental.health.module.auth.SysAdmin;
import com.mental.health.module.auth.SysAdminMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动初始化：sys_admin 为空时创建默认管理员 admin / 123456（BCrypt）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final SysAdminMapper sysAdminMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Long count = sysAdminMapper.selectCount(new LambdaQueryWrapper<>());
        if (count != null && count > 0) {
            return;
        }
        SysAdmin admin = new SysAdmin();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setNickname("管理员");
        sysAdminMapper.insert(admin);
        log.info("已初始化默认管理员：admin / 123456");
    }
}
