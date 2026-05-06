package org.blueseacode.service.user.model;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    private String nickname;
    private String bio;
    private String github;
    private String website;
    private String email;
    private String phone;
}
