package com.sergiomoratilla.kstreamtests;

import static org.assertj.core.api.Assertions.assertThat;

import com.sergiomoratilla.kstreamtests.messaging.InputMessage;
import com.sergiomoratilla.kstreamtests.messaging.InputMessageSerde;
import com.sergiomoratilla.kstreamtests.messaging.MessagingConfiguration.InputChannel;
import com.sergiomoratilla.kstreamtests.messaging.MessagingConfiguration.OutputChannel;
import com.sergiomoratilla.kstreamtests.messaging.OurTopology;
import com.sergiomoratilla.kstreamtests.messaging.OutputMessage;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes.StringSerde;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KstreamTestsApplication.class)
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(partitions = 1,
    topics = {
        InputChannel.INPUT,
        OutputChannel.OUTPUT
    })
public class KafkaApiStreamTest {

    private static ConsumerRecordFactory<String, InputMessage> senderFactory;
    private static TopologyTestDriver testDriver;

    private static final String INPUT_TOPIC = "input-topic";
    private static final String OUTPUT_TOPIC = "output-topic";

    @BeforeAll
    public static void setup(@Autowired OurTopology ourTopology, @Autowired EmbeddedKafkaBroker embeddedKafka) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-metrics");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, org.apache.kafka.common.serialization.Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, TestJsonSerde.class);
        Topology topology = instanceTopology(ourTopology);
        testDriver = new TopologyTestDriver(topology, props);

        senderFactory = new ConsumerRecordFactory<>("input-topic", new StringSerializer(), new JsonSerializer<>());
    }

    @Nonnull
    private static Topology instanceTopology(@Nonnull OurTopology ourTopology) {
        StreamsBuilder builder = new StreamsBuilder();

        Consumed with = Consumed.with(new StringSerde(), new InputMessageSerde());
        ourTopology.buildTopology(builder.stream(INPUT_TOPIC, with)).to(OUTPUT_TOPIC);
        return builder.build();
    }

    private static void send(@Nonnull String key, @Nonnull InputMessage message) {
        testDriver.pipeInput(senderFactory.create(INPUT_TOPIC, key, message));
    }

    private OutputMessage readOutput() {
        ProducerRecord<String, OutputMessage> record =
            testDriver.readOutput(OUTPUT_TOPIC, new StringDeserializer(), new JsonDeserializer<>(OutputMessage.class));

        return record.value();
    }

    @AfterAll
    public static void teardown() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Test
    public void test() {
        InputMessage message = new InputMessage("owner-id");

        send("a-key", message);
        OutputMessage output = readOutput();

        assertThat(output.getCount()).isEqualTo(1);
    }

}