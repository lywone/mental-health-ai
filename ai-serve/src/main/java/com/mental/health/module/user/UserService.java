package com.mental.health.module.user;

import com.mental.health.common.BusinessException;
import com.mental.health.common.ErrorCode;
import com.mental.health.infra.storage.StorageService;
import com.mental.health.module.user.dto.ProfileUpdateRequest;
import com.mental.health.module.user.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 用户基本信息：查询 / 更新（仅本人）/ 头像上传
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserMapper userMapper;
    private final StorageService storageService;

    public UserProfileVO getProfile(Long uid) {
        return toVO(mustGet(uid));
    }

    /** 更新本人信息：仅覆盖传入字段，昵称不允许更新为空白 */
    public UserProfileVO updateProfile(Long uid, ProfileUpdateRequest req) {
        User user = mustGet(uid);
        if (req.getNickname() != null) {
            String name = req.getNickname().trim();
            if (!name.isEmpty()) {
                user.setNickname(name);
            }
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }
        if (req.getGender() != null) {
            user.setGender(req.getGender());
        }
        if (req.getBirthday() != null) {
            if (req.getBirthday().isAfter(LocalDate.now())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "生日不能晚于今天");
            }
            user.setBirthday(req.getBirthday());
        }
        if (req.getBio() != null) {
            user.setBio(req.getBio().trim());
        }
        userMapper.updateById(user);
        return toVO(user);
    }

    /** 头像上传：复用本地图片存储（jpg/png/webp ≤5MB） */
    public String uploadAvatar(MultipartFile file) {
        return storageService.uploadImage(file);
    }

    private User mustGet(Long uid) {
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private UserProfileVO toVO(User user) {
        String phone = user.getPhone();
        if (phone != null && phone.length() == 11) {
            phone = phone.substring(0, 3) + "****" + phone.substring(7);
        }
        return new UserProfileVO(user.getId(), phone, user.getNickname(), user.getAvatar(), user.getGender(),
                user.getBirthday() == null ? null : user.getBirthday().toString(),
                user.getBio(),
                user.getCreatedAt() == null ? null : user.getCreatedAt().format(DATETIME_FMT));
    }
}
