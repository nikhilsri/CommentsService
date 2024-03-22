package com.IntuitCraft.demo.service;

import com.IntuitCraft.demo.Entities.Comment;
import com.IntuitCraft.demo.Entities.Dislikes;
import com.IntuitCraft.demo.Entities.Likes;
import com.IntuitCraft.demo.beans.CommonKafkaBaseBean;
import com.IntuitCraft.demo.exceptions.PostsException;
import com.IntuitCraft.demo.repositories.ICommentRepository;
import com.IntuitCraft.demo.repositories.IVoteRepository;
import com.IntuitCraft.demo.utils.CommentsServiceConstants;
import com.IntuitCraft.demo.utils.VotesConstants;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VoteService {

    private final IVoteRepository voteRepository;
    private final ICommentRepository commentRepository;
    private final KafkaProducer kafkaProducer;
    @Autowired
    CommentService commentService;
    @Value("${votes.topic}")
    private String votesTopic;

    @Autowired
    public VoteService(IVoteRepository voteRepository, ICommentRepository commentRepository, KafkaProducer kafkaProducer) {
        this.commentRepository = commentRepository;
        this.voteRepository = voteRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Cacheable(value = "votesCache", key = "#commentId+ '_' + #voteType+ '_'+ #pageNumber")
    public Page<String> getCommentLikesUserList(Long commentId, String voteType, int pageNumber) {
        if (VotesConstants.LIKES.equals(voteType))
            return voteRepository.findUserIdsForVoteByCommentId(commentId, VotesConstants.LIKES, PageRequest.of(pageNumber, CommentsServiceConstants.FETCH_SIZE));
        else
            return voteRepository.findUserIdsForVoteByCommentId(commentId, VotesConstants.DISLIKES, PageRequest.of(pageNumber, CommentsServiceConstants.FETCH_SIZE));
    }

    public boolean likeComment(Long commentId, String userId) {
        try {
            //Comment comment = commentService.getCommentById(commentId);
            Likes likes = voteRepository.findLikesByCommentIdAndUserId(commentId, userId);
            if (ObjectUtils.isNotEmpty(likes)) {
                //user already liked the same comment
                return true;
            }
            //Dislikes disLikes=voteRepository.findDislikesByCommentIDAndUserId(commentId,userId);
            Map<String, Object> eventData = new HashMap<String, Object>();
            eventData.put(VotesConstants.DISLIKES, "dislikes");
            eventData.put(CommentsServiceConstants.COMMENTS, commentId);
            eventData.put(CommentsServiceConstants.USERID, userId);

            ObjectMapper mapper = new ObjectMapper();
            String data = "";
            try {
                data = mapper.writeValueAsString(eventData);
            } catch (JsonProcessingException e) {
                throw PostsException.POST_NOT_ADDED;
            }
            // Set any other fields as needed
            CommonKafkaBaseBean commonKafkaBaseBean = new CommonKafkaBaseBean(EventName.LIKE.name(), data, LocalDate.now(), EventType.VOTE.name());
            kafkaProducer.sendToKafka(votesTopic, commonKafkaBaseBean);
            return true;
        } catch (Exception e) {
            //logger add krna h
            return false;
        }
        //return false;
    }

    public boolean dislikeComment(Long commentId, String userId) {
        try {

            //Comment comment = commentService.getCommentById(commentId);
            Dislikes disLikes = voteRepository.findDislikesByCommentIDAndUserId(commentId, userId);
            if (ObjectUtils.isNotEmpty(disLikes)) {
                //user already disliked the same comment
                return true;
            }
            //Likes likes=voteRepository.findLikesByCommentIdAndUserId(commentId,userId);

            Map<String, Object> eventData = new HashMap<String, Object>();
            eventData.put(VotesConstants.LIKES, "likes");
            eventData.put(CommentsServiceConstants.COMMENTS, commentId);
            eventData.put(CommentsServiceConstants.USERID, userId);

            ObjectMapper mapper = new ObjectMapper();
            String data = "";
            try {
                data = mapper.writeValueAsString(eventData);
            } catch (JsonProcessingException e) {
                throw PostsException.POST_NOT_ADDED;
            }

            CommonKafkaBaseBean commonKafkaBaseBean = new CommonKafkaBaseBean(EventName.DISLIKE.name(), data, LocalDate.now(), EventType.VOTE.name());
            kafkaProducer.sendToKafka(votesTopic, commonKafkaBaseBean);
            return true;
            /*if(ObjectUtils.isNotEmpty(likes)){
                    //user already liked the same comment and now he is trying to dislike it
                    //so we are making him do that while decrementing the like count
                    //remove the entry from likes table also
                    comment.setLikesCount(comment.getLikesCount()-1);
                    removeLikeIfAlreadyDislikedBySameUserAndComment=true;
            }
            if (ObjectUtils.isNotEmpty(comment)) {
                Dislikes dislike = new Dislikes();
                dislike.setCommentId(commentId);
                dislike.setUserId(userId);
                if (ObjectUtils.isNotEmpty(comment.getPostId())) {
                    dislike.setPostId(comment.getPostId());
                }
                voteRepository.saveDisLikes(dislike);
                comment.setDislikesCount(comment.getDislikesCount() + 1);
                commentRepository.updateComment(comment);
                if(removeLikeIfAlreadyDislikedBySameUserAndComment){
                    //remove entry from like if same user try to dislike the already liked comment
                    voteRepository.removeLikeByUserAndComment(likes.getUserId(), commentId);
                }
                return true;
            }*/
        } catch (Exception e) {
            //logger add krna h
            return false;
        }
        //return false;
    }

}
