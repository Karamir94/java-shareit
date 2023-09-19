package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static Comment toComment(CommentDtoIn comment, Item item, User user) {
        return new Comment(
                comment.getId(),
                comment.getText(),
                item,
                user,
                LocalDateTime.now()
        );
    }

    public static CommentDtoOut toCommentDto(Comment comment) {

        return new CommentDtoOut(
                comment.getId(),
                comment.getText(),
                ItemMapper.toItemDto(comment.getItem()),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }
}
