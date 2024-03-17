package com.IntuitCraft.demo.service;

import com.IntuitCraft.demo.Entities.Comment;
import com.IntuitCraft.demo.beans.CommentsOutputBean;
import com.IntuitCraft.demo.exceptions.CommentsException;
import com.IntuitCraft.demo.repositories.ICommentRepository;
import com.IntuitCraft.demo.repositories.IPostRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    @Autowired
    private ICommentRepository commentRepository;

    @Autowired
    private IPostRepository IPostRepository;

    public Page<Comment> getFirstLevelComments(Long postId, int pageSize) {
        return commentRepository.findFirstLevelCommentsByPostId(postId, PageRequest.of(0, pageSize));
    }

    public Page<Comment> getReplies(Long commentId, int pageSize) {
        return commentRepository.findCommentsByParentIdPageable(commentId, PageRequest.of(0, pageSize));
    }

    public Comment addComment(Long postId, String content) {
        Comment comment = new Comment();
        comment.setPostId(postId); // Assuming Post entity exists
        comment.setContent(content);
        //set userID afterwards
        return commentRepository.save(comment);
    }

    public CommentsOutputBean addReply(Long parentId, String content) {
        Comment comment = commentRepository.findCommentsByParentId(parentId);
       // Post post=postRepository.getPostByPostId(comment.getParent());
        if (ObjectUtils.isNotEmpty(comment)) {
            Comment reply = new Comment();
            reply.setContent(content);
            reply.setParent(comment.getId());
            reply.setPostId(comment.getPostId());
            commentRepository.save(reply);
        }else{
            throw CommentsException.COMMENT_NOT_FOUND;
        }
        CommentsOutputBean commentsOutputBean=new CommentsOutputBean(
                comment.getId(), comment.getPostId(), content
        );
        return commentsOutputBean;
    }

}

