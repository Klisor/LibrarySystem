package com.zjgsu.librarymanagement.util;
import com.zjgsu.librarymanagement.util.JwtUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
public class Tools {
    @Autowired
    private JwtUtil jwtUtil;
    public boolean isAdmin(String token) {
        return "ADMIN".equals(jwtUtil.getRoleFromToken(token));
    }

    public boolean isSelf(Long id, String tk) {
        return id.equals(jwtUtil.getUserIdFromToken(tk));
    }
}
