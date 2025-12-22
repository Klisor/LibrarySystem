package com.zjgsu.librarymanagement.controller;

import com.zjgsu.librarymanagement.model.dto.NoteDTO;
import com.zjgsu.librarymanagement.response.ApiResponse;
import com.zjgsu.librarymanagement.service.NoteService;
import com.zjgsu.librarymanagement.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final JwtUtil jwtUtil;
    /**
     * 获取笔记列表（前端分页）
     */
    @GetMapping
    public ApiResponse<List<NoteDTO>> getNotes( @RequestParam(required = false) Long bookId,
                                                @RequestHeader("Authorization") String token) {
        if(JwtUtil.validateToken(token)) {
            Long userId=jwtUtil.getUserIdFromToken(token);
            List<NoteDTO> notes = noteService.getNotesByUser(userId);
            return ApiResponse.success(notes);
        }
        else {
            return ApiResponse.error("无效token");
        }
    }

    /**
     * 获取笔记详情
     */
    @GetMapping("/{id}")
    public ApiResponse<NoteDTO> getNote(@PathVariable Long id ,
                                        @RequestHeader("Authorization") String token) {
        if(JwtUtil.validateToken(token)) {
            NoteDTO note = noteService.getNoteById(id);
            if(note.getUserId()==jwtUtil.getUserIdFromToken(token))
                return ApiResponse.success(note);
            else {
                return ApiResponse.error("无权限");
            }
        }
        else {
            return ApiResponse.error("无效token");
        }
    }

    /**
     * 创建笔记
     */
    @PostMapping
    public ApiResponse<NoteDTO> createNote(@Valid @RequestBody NoteDTO noteDTO ,
                                           @RequestHeader("Authorization") String token) {
        if(JwtUtil.validateToken(token)) {
            Long userId=jwtUtil.getUserIdFromToken(token);
            NoteDTO createdNote = noteService.createNote(noteDTO,userId);
            return ApiResponse.success("笔记创建成功", createdNote);
        }
        else {
            return ApiResponse.error("无效token");
        }

    }

    /**
     * 更新笔记
     */
    @PutMapping("/{id}")
    public ApiResponse<NoteDTO> updateNote(
            @PathVariable Long id,
            @Validated @RequestBody NoteDTO noteDTO,
            @RequestHeader("Authorization") String token) {
        if(JwtUtil.validateToken(token)) {
            NoteDTO note = noteService.getNoteById(id);
            if(note.getUserId()==jwtUtil.getUserIdFromToken(token)) {
                NoteDTO updatedNote = noteService.updateNote(id, noteDTO);
                return ApiResponse.success("笔记更新成功", updatedNote);
            }
            else {
                return ApiResponse.error("无权限");
            }
        }
        else {
            return ApiResponse.error("无效token");
        }
    }

    /**
     * 删除笔记
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNote(@PathVariable Long id,
                                        @RequestHeader("Authorization") String token) {
        if(JwtUtil.validateToken(token)) {
            NoteDTO note = noteService.getNoteById(id);
            if(note.getUserId()==jwtUtil.getUserIdFromToken(token)) {
                noteService.deleteNote(id);
                return ApiResponse.success("笔记删除成功", null);
            }
            else {
                return ApiResponse.error("无权限");
            }

        }
        else {
            return ApiResponse.error("无效token");
        }

    }
}