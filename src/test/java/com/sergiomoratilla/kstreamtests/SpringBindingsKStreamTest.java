package com.sergiomoratilla.kstreamtests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sergiomoratilla.kstreamtests.messaging.InputMessage;
import com.sergiomoratilla.kstreamtests.messaging.MessagingConfiguration.InputChannel;
import com.sergiomoratilla.kstreamtests.messaging.MessagingConfiguration.OutputChannel;
import com.sergiomoratilla.kstreamtests.messaging.OutputMessage;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes.StringSerde;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Failed test:
 *
 * You will constantly get:
 * java.lang.ClassCastException: class com.sun.proxy.$Proxy94 cannot be cast to class org.springframework.messaging.MessageChannel (com.sun.proxy.$Proxy94 and org.springframework.messaging.MessageChannel are in unnamed module of loader 'app')
 * 	at org.springframework.cloud.stream.test.binder.TestSupportBinder.bindProducer(TestSupportBinder.java:67) ~[spring-cloud-stream-test-support-2.1.0.RELEASE.jar:2.1.0.RELEASE]
 * 	at org.springframework.cloud.stream.binding.BindingService.lambda$rescheduleProducerBinding$2(BindingService.java:267) ~[spring-cloud-stream-2.1.0.RELEASE.jar:2.1.0.RELEASE]
 * 	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-5.1.5.RELEASE.jar:5.1.5.RELEASE]
 *
 * 	which entails:
 * 	2019-03-16 17:39:05.189  WARN 88354 --- [-StreamThread-1] org.apache.kafka.clients.NetworkClient   : [Consumer clientId=kstreams-tests-develop-service-494e6e02-26ac-4b44-a86c-e68fd4fbf70d-StreamThread-1-consumer, groupId=kstreams-tests-develop-service] Connection to node -1 (localhost/127.0.0.1:9092) could not be established. Broker may not be available.
 *
 * 	The source of the problem is that TestSupportBinder expects MessageChannel but KStream is passed.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KstreamTestsApplication.class)
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(partitions = 1,
	topics = {
		InputChannel.INPUT,
		OutputChannel.OUTPUT
	})
public class SpringBindingsKStreamTest {

	private static KafkaTemplate<String, InputMessage> template;

	private static Consumer<String, OutputMessage> outputConsumer;

	@BeforeAll
	public static void setup(@Autowired @Nonnull EmbeddedKafkaBroker embeddedKafka) {
		template = template(embeddedKafka);
		outputConsumer = consumer(embeddedKafka, OutputChannel.OUTPUT, new StringSerde(), new JsonSerde<>());
	}

	private static <K, V> Consumer<K, V> consumer(@Nonnull EmbeddedKafkaBroker embeddedKafka,
		String topic, Serde<K> keySerde, Serde<V> valueSerde) {
		Map<String, Object> consumerProps =
			KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "false", embeddedKafka);

		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_DOC, StringDeserializer.class);
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

		DefaultKafkaConsumerFactory<K, V> kafkaConsumerFactory =
			new DefaultKafkaConsumerFactory<>(consumerProps, keySerde.deserializer(),
				valueSerde.deserializer());
		Consumer<K, V> consumer = kafkaConsumerFactory.createConsumer();
		embeddedKafka.consumeFromAnEmbeddedTopic(consumer, topic);
		return consumer;
	}

	private static <K, V> KafkaTemplate<K, V> template(@Nonnull EmbeddedKafkaBroker embeddedKafka) {
		Map<String, Object> senderProperties =
			KafkaTestUtils.senderProps(embeddedKafka.getBrokersAsString());

		senderProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		senderProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		// create a Kafka producer factory
		ProducerFactory<K, V> producerFactory =
			new DefaultKafkaProducerFactory<>(
				senderProperties);

		// create a Kafka template
		KafkaTemplate<K,V> template = new KafkaTemplate<>(producerFactory);
		// set the default topic to send to
		template.setDefaultTopic(InputChannel.INPUT);

		return template;
	}

	@AfterAll
	public static void teardown() {
		if (outputConsumer != null) {
			outputConsumer.close();
		}
	}

	@Test
	public void test() {
		InputMessage in = new InputMessage("owner-id");
		template.send(InputChannel.INPUT, "one-key", in);

		ConsumerRecords<String, OutputMessage> outputTopicRecords = KafkaTestUtils.getRecords(this.outputConsumer);

		assertThat(outputTopicRecords.count()).isEqualTo(1);
	}
}
