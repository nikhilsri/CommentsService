package com.IntuitCraft.demo.service;

import com.IntuitCraft.demo.Entities.Post;
import com.IntuitCraft.demo.beans.CommonKafkaBaseBean;
import com.IntuitCraft.demo.beans.PostRequest;
import com.IntuitCraft.demo.exceptions.PostsException;
import com.IntuitCraft.demo.repositories.IPostRepository;
import com.IntuitCraft.demo.utils.CommentsServiceConstants;
import com.IntuitCraft.demo.utils.enums.EventName;
import com.IntuitCraft.demo.utils.enums.EventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PostService {
    @Autowired
    private IPostRepository postRepository;

    @Value("${posts.topic}")
    private String postsTopic;

    private final KafkaProducer kafkaProducer;

    @Autowired
    public PostService(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public Post addPost(PostRequest postRequest) {
        if(ObjectUtils.isEmpty(postRequest)||ObjectUtils.isEmpty(postRequest.getUserId())){
            throw PostsException.POST_BAD_REQUEST;
        }
        Post post = new Post(postRequest.getTitle(), postRequest.getContent(), postRequest.getUserId());
        String postData = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            postData = mapper.writeValueAsString(post);
        } catch (JsonProcessingException e) {
            throw PostsException.POST_NOT_ADDED;
        }

        CommonKafkaBaseBean commonKafkaBaseBean = new CommonKafkaBaseBean(EventName.ADD_POST.name(), postData, LocalDate.now(), EventType.POST.name());
        kafkaProducer.sendToKafka(postsTopic, commonKafkaBaseBean);
        return post;
    }

    @Cacheable(value = "postCache",key = "#postId",unless = "#result == null" )
    public Post findPostById(Long postId){
        if(ObjectUtils.isEmpty(postId))
            throw PostsException.POST_BAD_REQUEST;
        Post post= postRepository.findPostById(postId);
        if(ObjectUtils.isEmpty(post)){
            throw PostsException.POST_NOT_FOUND;
        }
        return post;
    }
    public Page<Post> getPostsByDate(LocalDate date, int pageNumber) {
        //caching krni hai ??
        return postRepository.getPostByDateAdded(date, PageRequest.of( pageNumber, CommentsServiceConstants.FETCH_SIZE));
    }
}

