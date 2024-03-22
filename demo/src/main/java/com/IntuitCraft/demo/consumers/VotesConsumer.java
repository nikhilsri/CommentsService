package com.IntuitCraft.demo.consumers;

import com.IntuitCraft.demo.Entities.Comment;
import com.IntuitCraft.demo.Entities.Dislikes;
import com.IntuitCraft.demo.Entities.Likes;
import com.IntuitCraft.demo.beans.CommonKafkaBaseBean;
import com.IntuitCraft.demo.repositories.ICommentRepository;
import com.IntuitCraft.demo.repositories.IVoteRepository;
import com.IntuitCraft.demo.service.CommentService;
import com.IntuitCraft.demo.utils.CommentsServiceConstants;
import com.IntuitCraft.demo.utils.VotesConstants;
import com.IntuitCraft.demo.utils.enums.EventName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class VotesConsumer {


    private final IVoteRepository voteRepository;


    private final ICommentRepository commentRepository;

    @Autowired
    CommentService commentService;

    public VotesConsumer(IVoteRepository voteRepository, ICommentRepository commentRepository) {
        this.voteRepository = voteRepository;
        this.commentRepository = commentRepository;
    }

    @KafkaListener(topics = "${votes.topic}",
            groupId = "${votes.group.id}")
    void subscribeChannelMediaCompletedEvents(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                              @Header(KafkaHeaders.GROUP_ID) String groupId, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                              @Header(KafkaHeaders.RECEIVED_TIMESTAMP) Date receivedTimeStamp,
                                              CommonKafkaBaseBean votesEventMessage)
            throws Exception {

        consumeVotesEvents(votesEventMessage);

    }

    private void consumeVotesEvents(CommonKafkaBaseBean votesEventMessage) {

        ObjectMapper mapper=new ObjectMapper();
        boolean removeDislikeIfAlreadyLikedBySameUserAndComment=false;
        try {
            Map<String,Object> votes=mapper.readValue(votesEventMessage.getEventData(),
                    new TypeReference<Map<String,Object>>() {
                    });
            String userId="";
           // Comment comment;
            if(ObjectUtils.isNotEmpty(votes)) {

                userId=votes.containsKey(CommentsServiceConstants.USERID)?getUserId(mapper,votes):"";
                Comment comment = null;
                if (EventName.LIKE.name().equals(votesEventMessage.getEventName())) {

                    Dislikes dislikes = null;
                    if (votes.containsKey(CommentsServiceConstants.COMMENTS)) {
                        comment = getComment(mapper, votes);
                    }
                    if (votes.containsKey(VotesConstants.DISLIKES)) {
                        dislikes = getVotes(mapper, votes, VotesConstants.DISLIKE, Dislikes.class,userId);
                    }
                    if (ObjectUtils.isNotEmpty(dislikes)) {

                        //user already disliked the same comment now he is trying to like it
                        comment.setDislikesCount(comment.getDislikesCount() - 1);
                        removeDislikeIfAlreadyLikedBySameUserAndComment = true;

                    }
                    if (ObjectUtils.isNotEmpty(comment)) {
                        Likes like = new Likes();
                        like.setCommentId(comment.getId());
                        like.setUserId(userId);
                        if (ObjectUtils.isNotEmpty(comment.getPostId())) {
                            like.setPostId(comment.getPostId());
                        }
                        //transaction for consistency,but if possible we can retry to a threshold limit
                        voteRepository.saveLikes(like);
                        comment.setLikesCount(comment.getLikesCount() + 1);
                        commentRepository.updateComment(comment);
                        if (removeDislikeIfAlreadyLikedBySameUserAndComment) {
                            //remove entry from dislike if same user try to like the already disliked comment
                            voteRepository.removeDislikeByUserAndComment(dislikes.getUserId(), comment.getId());
                        }

                    }

                } else {
                    Likes likes = null;
                    boolean removeLikeIfAlreadyDislikedBySameUserAndComment = false;
                    if (votes.containsKey(CommentsServiceConstants.COMMENTS)) {
                        comment = getComment(mapper, votes);
                    }
                    if (votes.containsKey(VotesConstants.LIKES)) {
                        likes = getVotes(mapper, votes, VotesConstants.LIKE, Likes.class,userId);
                    }
                    if (ObjectUtils.isNotEmpty(likes)) {
                        //user already liked the same comment and now he is trying to dislike it
                        //so we are making him do that while decrementing the like count
                        //remove the entry from likes table also
                        comment.setLikesCount(comment.getLikesCount() - 1);
                        removeLikeIfAlreadyDislikedBySameUserAndComment = true;
                    }
                    if (ObjectUtils.isNotEmpty(comment)) {
                        Dislikes dislike = new Dislikes();
                        dislike.setCommentId(comment.getId());
                        dislike.setUserId(userId);
                        if (ObjectUtils.isNotEmpty(comment.getPostId())) {
                            dislike.setPostId(comment.getPostId());
                        }
                        voteRepository.saveDisLikes(dislike);
                        comment.setDislikesCount(comment.getDislikesCount() + 1);
                        commentRepository.updateComment(comment);
                        if (removeLikeIfAlreadyDislikedBySameUserAndComment) {
                            //remove entry from like if same user try to dislike the already liked comment
                            voteRepository.removeLikeByUserAndComment(likes.getUserId(), comment.getId());
                        }

                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private  <T> T getVotes(ObjectMapper mapper, Map<String, Object> votes,String voteType,Class<T> responseType,String userId) throws JsonProcessingException {
        Comment comment=getComment(mapper,votes);
        if (VotesConstants.LIKE.equals(voteType)) {
            return (T) voteRepository.findLikesByCommentIdAndUserId(comment.getId(),userId);
        } else {
            /*LinkedHashMap<String, Object> dislikesMap = (LinkedHashMap<String, Object>) votes.get(VotesConstants.DISLIKES);
            String dislikesJson = mapper.writeValueAsString(dislikesMap);*/
            //return mapper.readValue(dislikesJson, responseType);
            return (T) voteRepository.findDislikesByCommentIDAndUserId(comment.getId(),userId);
        }
    }

    private Comment getComment(ObjectMapper mapper, Map<String, Object> votes) throws JsonProcessingException {

        Integer commentId = (Integer) votes.get(CommentsServiceConstants.COMMENTS);
       /* String commentJson = mapper.writeValueAsString(commentMap);
        commentId= mapper.readValue(commentJson,
                new TypeReference<Long>() {
                });*/
        Comment comment = commentRepository.findByCommentId(Long.valueOf(commentId));
        return comment;
    }

    private static String getUserId(ObjectMapper mapper, Map<String, Object> votes) throws JsonProcessingException {
        //votes.get(CommentsServiceConstants.USERID)
        String userId = (String) votes.get(CommentsServiceConstants.USERID);
        return userId;
    }
}
