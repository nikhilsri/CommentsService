package com.IntuitCraft.demo.repositories;

import com.IntuitCraft.demo.Entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ICommentRepository {
    Comment save(Comment comment);
    List<Comment> findByPostId(Long postId);
    List<Comment> findByUserId(String userId); // Change type to String
    Page<Comment> findFirstLevelCommentsByPostId(Long postId, Pageable pageable);

    Page<Comment> findCommentsByParentIdPageable(Long parentId, Pageable pageable);
    Comment findByCommentId(Long commentId);

    Comment findCommentsByParentId(Long parentId);

    public void updateCommentLikes(Long commentId, int newLikes);

    public void updateCommentDislikes(Long commentId, int newDislikes);
    void updateComment(Comment comment);

    public void deleteCommentById(Long commentId);

    Comment findCommentsByIdAndUserId(Long commentId,String userId);
}


