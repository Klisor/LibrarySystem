package com.zjgsu.librarymanagement.service.impl;

import com.zjgsu.librarymanagement.exception.BusinessException;
import com.zjgsu.librarymanagement.model.dto.NoteDTO;
import com.zjgsu.librarymanagement.model.entity.Note;
import com.zjgsu.librarymanagement.model.entity.User;
import com.zjgsu.librarymanagement.repository.NoteRepository;
import com.zjgsu.librarymanagement.service.NoteService;
import com.zjgsu.librarymanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserService userService;

    @Override
    public NoteDTO getNoteById(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "笔记不存在"));

        return NoteDTO.fromEntity(note);
    }

    @Override
    public List<NoteDTO> getNotesByUser(Long userId) {
        List<Note> notes = noteRepository.findByUserId(userId);
        return notes.stream()
                .map(NoteDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteDTO> getNotesByUserAndBook(Long userId, Long bookId) {
        List<Note> notes;
        if (bookId != null) {
            notes = noteRepository.findByUserIdAndBookId(userId, bookId);
        } else {
            notes = noteRepository.findByUserId(userId);
        }
        return notes.stream()
                .map(NoteDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoteDTO createNote(NoteDTO noteDTO, Long userId) {
        // 验证关联书籍是否存在（如果提供了bookId）
        if (noteDTO.getBookId() != null) {
            // TODO: 检查书籍是否存在
        }

        // 创建笔记
        Note note = noteDTO.toEntity();
        note.setUserId(userId);

        Note savedNote = noteRepository.save(note);
        log.info("创建笔记成功 - 用户Id: {}, 标题: {}", userId, savedNote.getTitle());

        return NoteDTO.fromEntity(savedNote);
    }

    @Override
    @Transactional
    public NoteDTO updateNote(Long id, NoteDTO noteDTO) {
        // 获取笔记
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "笔记不存在"));

        // 更新笔记
        if (noteDTO.getTitle() != null) {
            note.setTitle(noteDTO.getTitle());
        }
        if (noteDTO.getContent() != null) {
            note.setContent(noteDTO.getContent());
        }
        if (noteDTO.getBookId() != null) {
            note.setBookId(noteDTO.getBookId());
        }

        Note updatedNote = noteRepository.save(note);
        log.info("更新笔记成功 - ID: {}, 标题: {}", id, updatedNote.getTitle());

        return NoteDTO.fromEntity(updatedNote);
    }

    @Override
    @Transactional
    public void deleteNote(Long id) {
        // 获取笔记
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "笔记不存在"));

        noteRepository.delete(note);
        log.info("删除笔记成功 - ID: {}, 标题: {}", id, note.getTitle());
    }

    @Override
    public boolean isNoteOwner(Long noteId, Long userId) {
        return noteRepository.findById(noteId)
                .map(note -> note.getUserId().equals(userId))
                .orElse(false);
    }
}