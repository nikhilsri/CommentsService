package com.IntuitCraft.demo.service;

import com.IntuitCraft.demo.beans.CommonKafkaBaseBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToKafka(String topic, CommonKafkaBaseBean commonKafkaBaseBean){
        kafkaTemplate.send(topic, commonKafkaBaseBean);
    }
}
