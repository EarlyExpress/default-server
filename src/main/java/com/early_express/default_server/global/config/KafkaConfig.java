package com.early_express.default_server.global.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public RecordMessageConverter converter() {
        return new StringJsonMessageConverter();
    }

    // 기본 토픽 생성 예제
    // 이벤트 토픽 - 도메인 이벤트 발행용
    @Bean
    public NewTopic applicationEventsTopic() {
        return TopicBuilder.name(applicationName + "-events")
                .partitions(3)
                .replicas(3)
                .config("min.insync.replicas", "2")
                .build();
    }

    // 커맨드 토픽 - 명령 처리용
    @Bean
    public NewTopic applicationCommandsTopic() {
        return TopicBuilder.name(applicationName + "-commands")
                .partitions(3)
                .replicas(3)
                .config("min.insync.replicas", "2")
                .build();
    }

    // DLT(Dead Letter Topic) - 처리 실패한 메시지 보관
    @Bean
    public NewTopic applicationDeadLetterTopic() {
        return TopicBuilder.name(applicationName + "-dlt")
                .partitions(1)
                .replicas(3)
                .build();
    }
}