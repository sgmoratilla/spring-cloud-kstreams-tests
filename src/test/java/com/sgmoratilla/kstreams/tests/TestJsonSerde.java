package com.sgmoratilla.kstreams.tests;

import com.sgmoratilla.kstreams.tests.messaging.InputMessage;
import org.springframework.kafka.support.serializer.JsonSerde;

/**
 * Setting InputMessage as template class because otherwise I'm obtaining:
 * The class 'InputMessage' is not in the trusted packages
 */
public class TestJsonSerde extends JsonSerde<InputMessage> {

}
