package com.IntuitCraft.demo.consumers;

import com.IntuitCraft.demo.Entities.Comment;
import com.IntuitCraft.demo.Entities.Post;
import com.IntuitCraft.demo.beans.CommonKafkaBaseBean;
import com.IntuitCraft.demo.repositories.ICommentRepository;
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

@Service
public class CommentsConsumer {

    @Autowired
    ICommentRepository commentRepository;

    @KafkaListener(topics = "${comments.topic}",
            groupId = "${comments.group.id}")
    void subscribeCommmentsEvents(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.GROUP_ID) String groupId, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                  @Header(KafkaHeaders.RECEIVED_TIMESTAMP) Date receivedTimeStamp,
                                  CommonKafkaBaseBean addCommentEventMessage)
            throws Exception {

        consumeCommentsEvents(addCommentEventMessage);

    }

    private void consumeCommentsEvents(CommonKafkaBaseBean addCommentEventMessage) {

        ObjectMapper mapper=new ObjectMapper();
        Comment comment= null;
        try {
            comment = mapper.readValue(addCommentEventMessage.getEventData(),
                    new TypeReference<Comment>() {
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //LinkedHashMap<?, ?> eventData = (LinkedHashMap<?, ?>) addPostEventMessage.getEventData();
        //Post post=new Post();
        if ((EventName.ADD_REPLY.name().equals(addCommentEventMessage.getEventName()) ||
                EventName.ADD_COMMENT.name().equals(addCommentEventMessage.getEventName()))
                && ObjectUtils.isNotEmpty(comment)) {

            commentRepository.save(comment);
        }
    }
}

