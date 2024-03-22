package com.IntuitCraft.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.IntuitCraft.demo.Entities.Comment;
import com.IntuitCraft.demo.Entities.Post;
import com.IntuitCraft.demo.beans.CommentsRequest;
import com.IntuitCraft.demo.exceptions.CommentsException;
import com.IntuitCraft.demo.exceptions.PostsException;
import com.IntuitCraft.demo.repositories.ICommentRepository;
import com.IntuitCraft.demo.service.CommentService;
import com.IntuitCraft.demo.service.KafkaProducer;
import com.IntuitCraft.demo.service.PostService;
import com.IntuitCraft.demo.utils.CommentsServiceConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class CommentServiceTest {

    @Mock
    private ICommentRepository commentRepository;

    @Mock
    private PostService postService;

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getFirstLevelComments_PostNotFound() {
        Long postId = 1L;
        int pageNumber = 0;

        when(postService.findPostById(postId)).thenReturn(null);

        assertThrows(PostsException.class, () -> commentService.getFirstLevelComments(postId, pageNumber));
    }

    @Test
    void getCommentById_CommentNotFound() {
        Long commentId = 1L;

        when(commentRepository.findByCommentId(commentId)).thenReturn(null);

        assertThrows(CommentsException.class, () -> commentService.getCommentById(commentId));
    }

    @Test
    void getReplies_CommentNotFound() {
        Long commentId = 771L;
        int pageNumber = 0;

        when(commentRepository.findCommentsByParentIdPageable(commentId, PageRequest.of(pageNumber, CommentsServiceConstants.FETCH_SIZE)))
                .thenReturn(null);

        assertThrows(CommentsException.class, () -> commentService.getReplies(commentId, pageNumber));
    }

    @Test
    void addComment_PostNotFound() {
        Long postId = 1L;
        CommentsRequest commentRequest = new CommentsRequest();
        commentRequest.setContent("Test comment");
        commentRequest.setUserId("user123");

        when(postService.findPostById(postId)).thenReturn(null);

        assertThrows(PostsException.class, () -> commentService.addComment(postId, commentRequest));
    }

    @Test
    void addComment_NullRequest() {
        Long postId = 3L;

        assertThrows(CommentsException.class, () -> commentService.addComment(postId, null));
    }

    @Test
    void findCommentsByParentId_CommentNotFound() {
        Long parentId = 1L;

        when(commentRepository.findCommentsByParentId(parentId)).thenReturn(null);

        assertThrows(CommentsException.class, () -> commentService.findCommentsByParentId(parentId));
    }

    @Test
    void addReply_CommentNotFound() {
        Long parentId = 1L;
        CommentsRequest commentRequest = new CommentsRequest();
        commentRequest.setContent("Test reply");
        commentRequest.setUserId("user123");

        when(commentRepository.findCommentsByParentId(parentId)).thenReturn(null);

        assertThrows(CommentsException.class, () -> commentService.addReply(parentId, commentRequest));
    }
    @Test
    void getFirstLevelComments_Positive() {
        Long postId = 1L;
        int pageNumber = 0;

        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment(1L, postId, "user123", "Test comment"));

        Page<Comment> page = new PageImpl<>(comments);

        when(postService.findPostById(postId)).thenReturn(new Post(postId, "Test post", "Content", "user123"));
        when(commentRepository.findFirstLevelCommentsByPostId(postId, PageRequest.of(pageNumber, CommentsServiceConstants.FETCH_SIZE))).thenReturn(page);

        Page<Comment> result = commentService.getFirstLevelComments(postId, pageNumber);

        assertEquals(comments.size(), result.getContent().size());
    }

    @Test
    void getCommentById_Positive() {
        Long commentId = 1L;

        Comment comment = new Comment(commentId, 1L, "user123", "Test comment");

        when(commentRepository.findByCommentId(commentId)).thenReturn(comment);

        Comment result = commentService.getCommentById(commentId);

        assertNotNull(result);
        assertEquals(commentId, result.getId());
    }
}

