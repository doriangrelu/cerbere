package fr.cerbere.component.cerbere_core.adapter.config;

import fr.cerbere.component.cerbere_core.infrastructure.messaging.kafka.producer.AlarmStateChangedKafkaProducer;
import fr.cerbere.component.cerbere_core.infrastructure.messaging.kafka.producer.AlertKafkaProducer;
import fr.cerbere.component.cerbere_core.infrastructure.messaging.kafka.producer.DeviceKafkaProducer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.time.Duration;

/**
 * Provisionnement explicite des topics produits par {@code cerbere-core}
 * (voir ADR 0014 pour la convention de dimensionnement). Ne redéclare pas
 * {@code cerbere.device.events.raw} : ce topic est possédé et provisionné par
 * {@code cerbere-devices-mock}, {@code cerbere-core} n'en est que consommateur.
 */
@Configuration(proxyBeanMethods = false)
public final class KafkaTopicConfig {

    private static final int PARTITIONS = 3;
    private static final int REPLICAS = 1;
    private static final long RETENTION = Duration.ofDays(3).toMillis();

    @Bean
    public NewTopic alarmStateChangedTopic() {
        return this.topic(AlarmStateChangedKafkaProducer.TOPIC);
    }

    @Bean
    public NewTopic alarmAlertsTopic() {
        return this.topic(AlertKafkaProducer.TOPIC);
    }

    @Bean
    public NewTopic deviceStateTopic() {
        return this.topic(DeviceKafkaProducer.TOPIC);
    }

    private NewTopic topic(final String name) {
        return TopicBuilder.name(name)
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .config(TopicConfig.RETENTION_MS_CONFIG, Long.toString(RETENTION))
                .build();
    }
}
