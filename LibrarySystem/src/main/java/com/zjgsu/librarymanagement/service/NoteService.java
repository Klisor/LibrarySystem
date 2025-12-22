package com.zjgsu.librarymanagement.service;

import com.zjgsu.librarymanagement.model.dto.NoteDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NoteService {

    // 笔记管理
    NoteDTO getNoteById(Long id);

    List<NoteDTO> getNotesByUser(Long userId);

    List<NoteDTO> getNotesByUserAndBook(Long userId, Long bookId);

//    List<NoteDTO> getCurrentUserNotes(Long bookId);

//    NoteDTO createNote(NoteDTO noteDTO);

    @Transactional
    NoteDTO createNote(NoteDTO noteDTO, Long userId);

    NoteDTO updateNote(Long id, NoteDTO noteDTO);

    void deleteNote(Long id);

    // 检查权限
    boolean isNoteOwner(Long noteId, Long userId);
}