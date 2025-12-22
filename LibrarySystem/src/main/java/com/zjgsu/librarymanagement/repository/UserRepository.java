package com.zjgsu.librarymanagement.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjgsu.librarymanagement.model.entity.Book;
import com.zjgsu.librarymanagement.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Mapper
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long userId);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByUsernameContainingAndRole(String username, User.UserRole role);
    List<User> findByUsernameContaining(String username);
    List<User> findByEmailContaining(String email);

    // MyBatis Plus 查询示例
    @Select("SELECT * FROM users WHERE username LIKE CONCAT('%', #{username}, '%') AND role = #{role}")
    List<User> findUsersByUsernameAndRole(@Param("username") String username, @Param("role") String role);
}