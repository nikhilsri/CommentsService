package com.IntuitCraft.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.IntuitCraft.demo.Entities.Post;
import com.IntuitCraft.demo.beans.CommonKafkaBaseBean;
import com.IntuitCraft.demo.beans.PostRequest;
import com.IntuitCraft.demo.exceptions.PostsException;
import com.IntuitCraft.demo.repositories.IPostRepository;
import com.IntuitCraft.demo.service.KafkaProducer;
import com.IntuitCraft.demo.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class PostServiceTest {

    @Mock
    private IPostRepository postRepository;

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void addPost_Positive() {
        PostRequest postRequest = new PostRequest("Title", "Content", "user123");
        Post post = new Post(null, "Title", "Content", "user123");

        //doNothing().when(postRepository.save(any(Post.class)));

        Post result = postService.addPost(postRequest);

        assertNotNull(result);
        assertEquals(post.getTitle(), result.getTitle());
        assertEquals(post.getContent(), result.getContent());
        assertEquals(post.getUserId(), result.getUserId());
        //verify(kafkaProducer,times(1)).sendToKafka(anyString(), any(CommonKafkaBaseBean.class));
    }

    @Test
    void findPostById_Positive() {
        Long postId = 1L;
        Post post = new Post(postId, "Title", "Content", "user123");

        when(postRepository.findPostById(postId)).thenReturn(post);

        Post result = postService.findPostById(postId);

        assertNotNull(result);
        assertEquals(postId, result.getId());
    }

    @Test
    void findPostById_Negative() {
        Long postId = 1L;

        when(postRepository.findPostById(postId)).thenReturn(null);

        assertThrows(PostsException.class, () -> postService.findPostById(postId));
    }
    @Test
    void findPostById_Negative_NullId() {
        Long postId = null;

        assertThrows(PostsException.class, () -> postService.findPostById(postId));
    }


}
