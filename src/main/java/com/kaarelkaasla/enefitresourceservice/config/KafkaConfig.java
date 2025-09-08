package com.kaarelkaasla.enefitresourceservice.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaarelkaasla.enefitresourceservice.dtos.ResourceEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Configures Kafka topics, producer/consumer factories, and error handling.
 * Uses an idempotent producer with JSON serializing/deserializing, RECORD acks on the listener,
 * and a DefaultErrorHandler that retries with FixedBackOff before dead-lettering.
 */
@Configuration
@Slf4j
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${app.kafka.topic.resource-updates}")
  private String topicName;

  @Value("${app.kafka.topic.resource-updates-dlt}")
  private String dltTopicName;

  @Value("${app.kafka.retry.attempts}")
  private int retryAttempts;

  @Value("${app.kafka.retry.delay}")
  private long retryDelay;

  @Bean
  public NewTopic resourceUpdatesTopic() {
    return TopicBuilder.name(topicName).partitions(3).replicas(1).build();
  }

  @Bean
  public NewTopic resourceUpdatesDltTopic() {
    return TopicBuilder.name(dltTopicName).partitions(3).replicas(1).build();
  }

  @Bean
  public ProducerFactory<String, ResourceEvent> producerFactory(ObjectMapper objectMapper) {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
    configProps.put(ProducerConfig.ACKS_CONFIG, "all");
    configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

    DefaultKafkaProducerFactory<String, ResourceEvent> factory =
        new DefaultKafkaProducerFactory<>(configProps);
    factory.setKeySerializer(new StringSerializer());
    factory.setValueSerializer(new JsonSerializer<>(objectMapper));
    return factory;
  }

  @Bean
  public KafkaTemplate<String, ResourceEvent> kafkaTemplate(
      ProducerFactory<String, ResourceEvent> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }

  @Bean
  public ConsumerFactory<String, ResourceEvent> consumerFactory(ObjectMapper objectMapper) {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "enefitresourceservice-group");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.kaarelkaasla.enefitresourceservice");

    DefaultKafkaConsumerFactory<String, ResourceEvent> factory =
        new DefaultKafkaConsumerFactory<>(props);
    factory.setKeyDeserializer(new StringDeserializer());
    factory.setValueDeserializer(new JsonDeserializer<>(ResourceEvent.class, objectMapper));
    return factory;
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ResourceEvent>
      kafkaListenerContainerFactory(ConsumerFactory<String, ResourceEvent> consumerFactory) {
    ConcurrentKafkaListenerContainerFactory<String, ResourceEvent> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    // Process and commit offsets one record at a time to reduce redelivery scope
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

    // Retry delivery with fixed backoff before dead-lettering to the DLT
    factory.setCommonErrorHandler(
        new DefaultErrorHandler(
            (record, exception) -> {
              log.error(
                  "Message sent to DLT. Topic: {}, Key: {}, Value: {}, Exception: {}",
                  record.topic(),
                  record.key(),
                  record.value(),
                  exception.getMessage());
            },
            new FixedBackOff(retryDelay, retryAttempts)));

    return factory;
  }
}
