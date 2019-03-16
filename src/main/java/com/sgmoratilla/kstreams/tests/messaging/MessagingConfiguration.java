package com.sgmoratilla.kstreams.tests.messaging;

import com.sgmoratilla.kstreams.tests.messaging.MessagingConfiguration.InputChannel;
import com.sgmoratilla.kstreams.tests.messaging.MessagingConfiguration.OutputChannel;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;

@EnableBinding({InputChannel.class, OutputChannel.class})
public class MessagingConfiguration {

    public interface InputChannel {
        String INPUT = "input-channel";

        @Input(INPUT)
        KStream<?, InputMessage> input();
    }

    public interface OutputChannel {

        String OUTPUT = "ouput-channel";
        @Output(OUTPUT)
        KStream<String, OutputMessage> output();
    }
}
