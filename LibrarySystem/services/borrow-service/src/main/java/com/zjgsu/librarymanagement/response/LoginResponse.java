package com.zjgsu.librarymanagement.response;

import com.zjgsu.librarymanagement.model.dto.UserDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginResponse extends ApiResponse<LoginResponse.Data> {

    public static class Data {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public Long getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
        }

        public UserDTO getUser() {
            return user;
        }

        public void setUser(UserDTO user) {
            this.user = user;
        }

        private Long expiresIn;
        private UserDTO user;
    }

    public LoginResponse(String token, Long expiresIn, UserDTO user) {
        super(200, "登录成功", new Data());
        this.data.setToken(token);
        this.data.setExpiresIn(expiresIn);
        this.data.setUser(user);
    }
}