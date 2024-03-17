package com.IntuitCraft.demo.consumers;

import com.IntuitCraft.demo.Entities.Post;
import com.IntuitCraft.demo.beans.CommonKafkaBaseBean;
import com.IntuitCraft.demo.repositories.IPostRepository;
import com.IntuitCraft.demo.utils.enums.EventName;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

@Service
public class PostsConsumer {

    @Autowired
    private IPostRepository postRepository;
    @KafkaListener(topics = "${posts.topic}",
            groupId = "${posts.group.id}")
    void subscribeChannelMediaCompletedEvents(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                              @Header(KafkaHeaders.GROUP_ID) String groupId, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                              @Header(KafkaHeaders.RECEIVED_TIMESTAMP) Date receivedTimeStamp,
                                              CommonKafkaBaseBean addPOstEventMessage)
            throws Exception {

        consumePostsEvents(addPOstEventMessage);

    }

    private void consumePostsEvents(CommonKafkaBaseBean addPostEventMessage) throws IOException {

        ObjectMapper mapper=new ObjectMapper();
        Post post=mapper.readValue(addPostEventMessage.getEventData(),
                new TypeReference<Post>() {
                });
        //LinkedHashMap<?, ?> eventData = (LinkedHashMap<?, ?>) addPostEventMessage.getEventData();
        //Post post=new Post();
       if(EventName.ADD_POST.name().equals(addPostEventMessage.getEventName())
       && ObjectUtils.isNotEmpty(post)){

           postRepository.save(post);
       }
    }



}
