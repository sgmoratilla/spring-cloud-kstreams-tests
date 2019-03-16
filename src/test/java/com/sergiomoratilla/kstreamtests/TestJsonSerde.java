package com.sergiomoratilla.kstreamtests;

import com.sergiomoratilla.kstreamtests.messaging.InputMessage;
import org.springframework.kafka.support.serializer.JsonSerde;

/**
 * Setting InputMessage as template class because otherwise I'm obtaining:
 * The class 'com.sergiomoratilla.kstreamtests.messaging.InputMessage' is not in the trusted packages
 */
public class TestJsonSerde extends JsonSerde<InputMessage> {

}
