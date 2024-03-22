package com.IntuitCraft.demo.service;

import com.IntuitCraft.demo.Entities.Comment;
import com.IntuitCraft.demo.Entities.Post;
import com.IntuitCraft.demo.beans.CommentsOutputBean;
import com.IntuitCraft.demo.beans.CommentsRequest;
import com.IntuitCraft.demo.beans.CommonKafkaBaseBean;
import com.IntuitCraft.demo.configuration.CommonConfigurations;
import com.IntuitCraft.demo.exceptions.CommentsException;
import com.IntuitCraft.demo.exceptions.PostsException;
import com.IntuitCraft.demo.repositories.ICommentRepository;
import com.IntuitCraft.demo.repositories.IPostRepository;
import com.IntuitCraft.demo.utils.CommentsServiceConstants;
import com.IntuitCraft.demo.utils.enums.EventName;
import com.IntuitCraft.demo.utils.enums.EventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CommentService {
    @Autowired
    private ICommentRepository commentRepository;

    @Autowired
    private IPostRepository postRepository;

    @Autowired
    PostService postService;

    @Value(value = "${comments.topic}")
    public String COMMENTS_TOPIC;

    @Autowired
    public CommonConfigurations commonConfigurations;

    private final KafkaProducer kafkaProducer;

    public CommentService(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }
    ObjectMapper mapper=new ObjectMapper();

    @Cacheable(value = "firstLevelCommentCache",key = "#postId +'_'+#pageNumber")
    public Page<Comment> getFirstLevelComments(Long postId, int pageNumber) {
        //cache se check krlo pehle ki hai ki nahi usme for DB hit
        //1st cahce is postIdVsComments--for N comments on posts
        //2nd cache key is parentCommentIdVsComments -- for first n replies of comment
        if(ObjectUtils.isEmpty(postService.findPostById(postId))){
            throw PostsException.POST_NOT_FOUND;
        }
        return commentRepository.findFirstLevelCommentsByPostId(postId, PageRequest.of( pageNumber, CommentsServiceConstants.FETCH_SIZE));
    }

    @Cacheable(value = "commentCache",key = "#commentId")
    public Comment getCommentById(Long commentId){
        Comment comment=commentRepository.findByCommentId(commentId);
        if(ObjectUtils.isNotEmpty(comment)){
            return comment;
        }
        throw CommentsException.COMMENT_NOT_FOUND;
    }

    @Cacheable(value = "repliesCache",key = "#commentId")
    public Page<Comment> getReplies(Long commentId, int pageNumber) {
        Page<Comment> comments= commentRepository.findCommentsByParentIdPageable(commentId, PageRequest.of(pageNumber, CommentsServiceConstants.FETCH_SIZE));
        if(ObjectUtils.isEmpty(comments)){
            throw CommentsException.COMMENT_NOT_FOUND;
        }
        return comments;
    }

    @CacheEvict(value = "firstLevelCommentCache",key = "#postId +'_'+0")
    public Comment addComment(Long postId, CommentsRequest commentRequest) {
        if(ObjectUtils.isEmpty(commentRequest)){
            throw CommentsException.COMMENT_BAD_REQUEST;
        }else if(ObjectUtils.isEmpty(postService.findPostById(postId))){
            throw PostsException.POST_NOT_FOUND;
        }
        Comment comment = new Comment();
        comment.setPostId(postId); // Assuming Post entity exists
        comment.setContent(commentRequest.getContent());
        comment.setUserId(commentRequest.getUserId());
        //set userID afterwards
        String commentData="";
        try {
            commentData=mapper.writeValueAsString(comment);
        } catch (JsonProcessingException e) {
            throw PostsException.POST_NOT_ADDED;
        }
        // Set any other fields as needed
        CommonKafkaBaseBean commonKafkaBaseBean =new CommonKafkaBaseBean(EventName.ADD_COMMENT.name(),commentData, LocalDate.now(), EventType.COMMENTS.name());
        kafkaProducer.sendToKafka(COMMENTS_TOPIC, commonKafkaBaseBean);
        return comment;
    }

    @Cacheable(value = "commentByParentId",key = "#parentId")
    public Comment findCommentsByParentId(Long parentId){
        Comment comment= commentRepository.findCommentsByParentId(parentId);
        if(ObjectUtils.isEmpty(comment)){
            throw CommentsException.COMMENT_NOT_FOUND;
        }
        return comment;
    }

    public String deleteCommentById(Long commentId,String userId){
        boolean canDelete=false;
        if(ObjectUtils.isEmpty(userId))
            throw CommentsException.COMMENT_BAD_REQUEST;
        Comment comment= getCommentById(commentId);
        if(ObjectUtils.isEmpty(comment)){
            throw CommentsException.COMMENT_NOT_FOUND;
        }
        if(ObjectUtils.isNotEmpty(getCommentByUserIdAndCommentId(commentId,userId))){
            canDelete=true;
        }else{
            Post post=postService.findPostById(comment.getPostId());
            if(ObjectUtils.isNotEmpty(post)){
                if(post.getUserId().equals(userId))
                    canDelete=true;
            }
        }
        if(canDelete) {
            commentRepository.deleteCommentById(commentId);
            return "Comment Deleted Successfully";
        }
        throw CommentsException.UNABLE_TO_DELETE_COMMENT;
    }

    public Comment getCommentByUserIdAndCommentId(Long commentId,String userId){
        return commentRepository.findCommentsByIdAndUserId(commentId,userId);
    }
    public Comment getCommentByIdAndCommentId(Long commentId,String userId){
        return commentRepository.findCommentsByIdAndUserId(commentId,userId);
    }

    public CommentsOutputBean addReply(Long parentId, CommentsRequest content) {
        Comment comment = findCommentsByParentId(parentId);
       // Post post=postRepository.getPostByPostId(comment.getParent());
        if (ObjectUtils.isNotEmpty(comment)) {
            Comment reply = new Comment();
            reply.setContent(content.getContent());
            reply.setParentId(comment.getId());
            reply.setPostId(comment.getPostId());
            reply.setUserId(content.getUserId());
            String replyData="";
            try {
                replyData=mapper.writeValueAsString(reply);
            } catch (JsonProcessingException e) {
                throw PostsException.POST_NOT_ADDED;
            }
            // Set any other fields as needed
            CommonKafkaBaseBean commonKafkaBaseBean =new CommonKafkaBaseBean(EventName.ADD_REPLY.name(),replyData, LocalDate.now(), EventType.REPLY.name());
            kafkaProducer.sendToKafka(COMMENTS_TOPIC, commonKafkaBaseBean);
            //commentRepository.save(reply);
        }else{
            throw CommentsException.COMMENT_NOT_FOUND;
        }
        CommentsOutputBean commentsOutputBean=new CommentsOutputBean(
                comment.getId(), comment.getPostId(), content.getContent()
        );
        return commentsOutputBean;
    }

}

