
# KStream Integration Tests:

I have some services working on production using Spring Cloud Stream + Kafka with no problems. So that, it felt like starting with Kafka Streams was the next natural step.

After a few hours trying to run a successful tests, I was a bit disappointed because it didn't seem to hard.
At the time of writing this (March 2019), I think spring-kafka-test does not correctly support KStreams.

I was getting:

> java.lang.ClassCastException: class com.sun.proxy.$Proxy94 cannot be cast to class org.springframework.messaging.MessageChannel (com.sun.proxy.$Proxy94 and org.springframework.messaging.MessageChannel are in unnamed module of loader 'app')
   at org.springframework.cloud.stream.test.binder.TestSupportBinder.bindProducer(TestSupportBinder.java:67) ~[spring-cloud-stream-test-support-2.1.0.RELEASE.jar:2.1.0.RELEASE]
   at org.springframework.cloud.stream.binding.BindingService.lambda$rescheduleProducerBinding$2(BindingService.java:267) ~[spring-cloud-stream-2.1.0.RELEASE.jar:2.1.0.RELEASE]
   at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-5.1.5.RELEASE.jar:5.1.5.RELEASE]

which entails:
>  2019-03-16 17:39:05.189  WARN 88354 --- [-StreamThread-1] org.apache.kafka.clients.NetworkClient   : [Consumer clientId=kstreams-tests-develop-service-494e6e02-26ac-4b44-a86c-e68fd4fbf70d-StreamThread-1-consumer, groupId=kstreams-tests-develop-service] Connection to node -1 (localhost/127.0.0.1:9092) could not be established. Broker may not be available.

At first I thought it was a problem with my configuration, but checking the code of  `org.springframework.cloud.stream.test.binder.TestSupportBinder`,  the problem is that it is expecting a MessageChannel (as in standard Spring-Kafka bindings).

I am pretty new to this part of Spring, so that I don't feel confident enough to browse and write what piece is missing.
So that, I try to write the test using the Kafka API for testing. This is the motivation of this code.

In **`com.sgmoratilla.kstreams.tests.SpringBindingsKStreamTest`** you will find the test using Spring bindings, as specified in documentation.
In **`com.sgmoratilla.kstreams.tests.KafkaApiStreamTest`** you will find the same version written with Kafka utils.

Both are testing the Kafka topology. KafkaApiStreamTest is not testing the bindings to the topics as SpringBindingsKStreamTest would do.