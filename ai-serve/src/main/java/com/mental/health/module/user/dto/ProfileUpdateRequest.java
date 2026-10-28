package com.mental.health.module.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户基本信息更新请求（仅更新传入字段；手机号不支持修改）
 */
@Data
public class ProfileUpdateRequest {

    @Size(max = 20, message = "昵称不超过 20 字")
    private String nickname;

    private String avatar;

    /** 0 保密 / 1 男 / 2 女 */
    @Min(value = 0, message = "性别取值非法")
    @Max(value = 2, message = "性别取值非法")
    private Integer gender;

    private LocalDate birthday;

    @Size(max = 200, message = "简介不超过 200 字")
    private String bio;
}
