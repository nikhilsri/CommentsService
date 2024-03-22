package com.IntuitCraft.demo;

import com.IntuitCraft.demo.Entities.Comment;
import com.IntuitCraft.demo.Entities.Dislikes;
import com.IntuitCraft.demo.Entities.Likes;
import com.IntuitCraft.demo.beans.CommonKafkaBaseBean;
import com.IntuitCraft.demo.exceptions.PostsException;
import com.IntuitCraft.demo.repositories.ICommentRepository;
import com.IntuitCraft.demo.repositories.IVoteRepository;
import com.IntuitCraft.demo.service.CommentService;
import com.IntuitCraft.demo.service.KafkaProducer;
import com.IntuitCraft.demo.service.VoteService;
import com.IntuitCraft.demo.utils.CommentsServiceConstants;
import com.IntuitCraft.demo.utils.VotesConstants;
import com.IntuitCraft.demo.utils.enums.EventName;
import com.IntuitCraft.demo.utils.enums.EventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@SpringBootTest
class VoteServiceTest {

    @Mock
    private IVoteRepository voteRepository;

    @Mock
    private ICommentRepository commentRepository;

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private VoteService voteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void likeComment_Success() {
        Long commentId = 1L;
        String userId = "user123";

        Comment comment = new Comment();
        Likes likes = null;

        when(commentService.getCommentById(commentId)).thenReturn(comment);
        when(voteRepository.findLikesByCommentIdAndUserId(commentId, userId)).thenReturn(likes);

        assertTrue(voteService.likeComment(commentId, userId));
    }

    @Test
    void likeComment_Failure() {
        Long commentId = 1L;
        String userId = "user123";

        when(commentService.getCommentById(commentId)).thenThrow(PostsException.class);

        assertFalse(voteService.likeComment(commentId, userId));
    }

    @Test
    void likeComment_AlreadyLiked() {
        Long commentId = 1L;
        String userId = "user123";

        Comment comment = new Comment();
        Likes likes = new Likes(); // Simulate existing like

        when(commentService.getCommentById(commentId)).thenReturn(comment);
        when(voteRepository.findLikesByCommentIdAndUserId(commentId, userId)).thenReturn(likes);

        assertTrue(voteService.likeComment(commentId, userId));

        // Verify that Kafka producer is not called
        verify(kafkaProducer, never()).sendToKafka(anyString(), any(CommonKafkaBaseBean.class));
    }

    @Test
    void likeComment_CommentNotFound() {
        Long commentId = 1L;
        String userId = "user123";

        when(commentService.getCommentById(commentId)).thenThrow(PostsException.class);

        assertFalse(voteService.likeComment(commentId, userId));

        // Verify that Kafka producer is not called
        verify(kafkaProducer, never()).sendToKafka(anyString(), any(CommonKafkaBaseBean.class));
    }

    @Test
    void dislikeComment_AlreadyDisliked() {
        Long commentId = 1L;
        String userId = "user123";

        Comment comment = new Comment();
        Dislikes dislikes = new Dislikes(); // Simulate existing dislike

        when(commentService.getCommentById(commentId)).thenReturn(comment);
        when(voteRepository.findDislikesByCommentIDAndUserId(commentId, userId)).thenReturn(dislikes);

        assertTrue(voteService.dislikeComment(commentId, userId));

        // Verify that Kafka producer is not called
        verify(kafkaProducer, never()).sendToKafka(anyString(), any(CommonKafkaBaseBean.class));
    }

    @Test
    void dislikeComment_CommentNotFound() {
        Long commentId = 1L;
        String userId = "user123";

        when(commentService.getCommentById(commentId)).thenThrow(PostsException.class);

        assertFalse(voteService.dislikeComment(commentId, userId));

        // Verify that Kafka producer is not called
        verify(kafkaProducer, never()).sendToKafka(anyString(), any(CommonKafkaBaseBean.class));
    }
}
