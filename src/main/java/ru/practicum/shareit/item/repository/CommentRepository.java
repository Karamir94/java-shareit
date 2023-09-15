package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c " +
            "from Comment c " +
            "join c.item as i " +
            "join i.user as u " +
            "where u.id = ?1 " +
            "order by c.created desc")
    List<Comment> findCommentsForItemsByOwnerId(Long userId);

    @Query("select c " +
            "from Comment c " +
            "join c.item as i " +
            "where i.id = ?1 " +
            "order by c.created desc ")
    List<Comment> findCommentsByItemId(Long itemId);
}
