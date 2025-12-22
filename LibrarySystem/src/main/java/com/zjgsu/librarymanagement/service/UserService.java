package com.zjgsu.librarymanagement.service;

import com.zjgsu.librarymanagement.model.dto.RegisterRequest;
import com.zjgsu.librarymanagement.model.dto.UserDTO;
import com.zjgsu.librarymanagement.model.dto.UserUpdateRequest;
import com.zjgsu.librarymanagement.model.entity.User;
import com.zjgsu.librarymanagement.response.LoginResponse;

import java.util.List;

public interface UserService {

    // 注册登录
    UserDTO register(RegisterRequest registerRequest);

    LoginResponse login(String username, String password);

    // 用户管理
    UserDTO getUserById(Long id);

    UserDTO getUserByUsername(String username);

    List<UserDTO> getAllUsers();

    List<UserDTO> searchUsers(String username, String email, User.UserRole role);

    UserDTO addUser(UserDTO userDTO);

    UserDTO updateUser(Long id, UserUpdateRequest updateRequest);

    void deleteUser(Long id);

    // 检查
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}