package com.zjgsu.librarymanagement.model.dto;
import com.zjgsu.librarymanagement.model.entity.Note;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


import java.time.LocalDateTime;

@Data
public class NoteDTO {

    private Long id;

    private Long userId;

    private Long bookId;

    private String bookTitle;

    @NotBlank(message = "笔记标题不能为空")
    @Size(max = 200, message = "笔记标题不能超过200个字符")
    private String title;

    @NotBlank(message = "笔记内容不能为空")
    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 转换为Note实体
    public Note toEntity() {
        Note note = new Note();
        note.setTitle(this.title);
        note.setContent(this.content);
        note.setBookId(this.bookId);
        return note;
    }

    // 从Note实体转换
    public static NoteDTO fromEntity(Note note) {
        if (note == null) {
            return null;
        }
        NoteDTO dto = new NoteDTO();
        dto.setId(note.getId());
        dto.setUserId(note.getUserId());
        dto.setBookId(note.getBookId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        return dto;
    }
}