package org.blueseacode.service.user.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private String bio;
    private String github;
    private String website;
    private Integer score;
    private Integer status;
    private List<String> roles;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
    private int resourceCount;
    private int articleCount;
    private int demandCount;
}
