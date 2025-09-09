package com.kaarelkaasla.enefitresourceservice.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaarelkaasla.enefitresourceservice.dtos.ResourceEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Configures Kafka topic and producer for event publishing.
 * Uses an idempotent producer with JSON serialization.
 */
@Configuration
@Slf4j
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${app.kafka.topic.resource-updates}")
  private String topicName;

  @Bean
  public NewTopic resourceUpdatesTopic() {
    return TopicBuilder.name(topicName).partitions(3).replicas(1).build();
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
    factory.setValueSerializer(
        new org.springframework.kafka.support.serializer.JsonSerializer<>(objectMapper));
    return factory;
  }

  @Bean
  public KafkaTemplate<String, ResourceEvent> kafkaTemplate(
      ProducerFactory<String, ResourceEvent> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }
}
