package com.sgmoratilla.kstreams.tests.messaging;

import com.sgmoratilla.kstreams.tests.messaging.MessagingConfiguration.InputChannel;
import com.sgmoratilla.kstreams.tests.messaging.MessagingConfiguration.OutputChannel;
import javax.annotation.Nonnull;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Service
public class OurTopology {

    /**
     * This method "installs" the topology into spring cloud stream processing.
     */
    @StreamListener(InputChannel.INPUT)
    @SendTo(OutputChannel.OUTPUT)
    public KStream<String, OutputMessage> processReservationStream(@Nonnull KStream<?, InputMessage> stream) {
        return buildTopology(stream);
    }

    /**
     * This method build the topology from an initial stream. This method was split for testing.
     */
    @Nonnull
    public KStream<String, OutputMessage> buildTopology(@Nonnull KStream<?, InputMessage> stream) {

        return stream
            .map((k, v) -> new KeyValue<>(v.getOwner(), v))
            .groupByKey()
            .count()
            .mapValues(l -> new OutputMessage(l))
            .toStream();
    }
}
