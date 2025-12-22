package com.zjgsu.librarymanagement.service.impl;

import com.zjgsu.librarymanagement.exception.BusinessException;
import com.zjgsu.librarymanagement.model.dto.RegisterRequest;
import com.zjgsu.librarymanagement.model.dto.UserDTO;
import com.zjgsu.librarymanagement.model.dto.UserUpdateRequest;
import com.zjgsu.librarymanagement.model.entity.User;
import com.zjgsu.librarymanagement.repository.UserRepository;
import com.zjgsu.librarymanagement.response.LoginResponse;
import com.zjgsu.librarymanagement.service.UserService;
import com.zjgsu.librarymanagement.util.JwtUtil;
import com.zjgsu.librarymanagement.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new PasswordEncoder ();
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserDTO register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (existsByUsername(registerRequest.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (existsByEmail(registerRequest.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }

        // 创建用户
        User user = registerRequest.toEntity();
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(User.UserRole.USER); // 注册用户默认为普通用户

        User savedUser = userRepository.save(user);
        log.info("用户注册成功: {}", savedUser.getUsername());

        return UserDTO.fromEntity(savedUser);
    }

    @Override
    public LoginResponse login(String username, String password) {
        try {
            // 1. 查询用户
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException("用户名不存在"));

            // 2. 手动验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new BusinessException("密码错误");
            }

            // 4. 生成JWT令牌
            String token = jwtUtil.generateToken(user);

            // 6. 返回登录响应
            return new LoginResponse(token, jwtUtil.getExpiration(), UserDTO.fromEntity(user));

        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage(), e);
            throw new BusinessException("登录失败，请稍后重试");
        }
    }

    @Override
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> searchUsers(String username, String email, User.UserRole role) {
        List<User> users;

        if (username != null && role != null) {
            users = userRepository.findByUsernameContainingAndRole(username, role);
        } else if (username != null) {
            users = userRepository.findByUsernameContaining(username);
        } else if (email != null) {
            users = userRepository.findByEmailContaining(email);
        } else if (role != null) {
            users = userRepository.findAll().stream()
                    .filter(user -> user.getRole() == role)
                    .collect(Collectors.toList());
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO addUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (existsByUsername(userDTO.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (existsByEmail(userDTO.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }

        // 创建用户
        User user = userDTO.toEntity();
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("添加用户成功: {}", savedUser.getUsername());

        return UserDTO.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateRequest updateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        // 更新邮箱
        if (updateRequest.getEmail() != null) {
            // 检查邮箱是否被其他用户使用
            userRepository.findByEmail(updateRequest.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            throw new BusinessException("邮箱已被其他用户使用");
                        }
                    });
            user.setEmail(updateRequest.getEmail());
        }

        // 更新最大借阅数量
        if (updateRequest.getMaxBorrowCount() != null) {
            if (updateRequest.getMaxBorrowCount() < user.getBorrowedCount()) {
                throw new BusinessException("最大借阅数量不能小于当前借阅数量");
            }
            user.setMaxBorrowCount(updateRequest.getMaxBorrowCount());
        }

        User updatedUser = userRepository.save(user);
        log.info("更新用户成功: {}", updatedUser.getUsername());

        return UserDTO.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        // 检查用户是否有未归还的图书
        // TODO: 添加借阅检查逻辑

        userRepository.delete(user);
        log.info("删除用户成功: {}", user.getUsername());
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}