package com.IntuitCraft.demo.configuration;


import com.IntuitCraft.demo.beans.CommonKafkaBaseBean;
import com.IntuitCraft.demo.utils.CommentsKafkaConfigurationUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.DelegatingByTopicDeserializer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${comments.topic}")
    public String COMMENTS_TOPIC;

    public KafkaConsumerConfig() {
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, CommentsKafkaConfigurationUtil.DEFAULT_COMMENTS_GROUP);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        Map<Pattern, Deserializer<?>> topicVsDeserializers = new HashMap<Pattern, Deserializer<?>>();

        topicVsDeserializers.put(Pattern.compile(COMMENTS_TOPIC),
                new JsonDeserializer<>(CommonKafkaBaseBean.class));

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
                new DelegatingByTopicDeserializer(topicVsDeserializers, new JsonDeserializer<>(CommonKafkaBaseBean.class).ignoreTypeHeaders()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CommonKafkaBaseBean> kafkaListenerContainerFactory(

            @Value("${default.consumer.concurrency}") Integer concurrency) {
        ConcurrentKafkaListenerContainerFactory<String, CommonKafkaBaseBean> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(concurrency);
        return factory;
    }

}
