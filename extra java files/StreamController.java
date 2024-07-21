
package edu.umass.cs.sase.stream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import java.util.Collections;
import java.util.Properties;

public class StreamController {
    private Stream myStream;
    private KafkaConsumer<String, String> consumer;

    public StreamController(int streamSize, String eventType) {
        this.myStream = new Stream(streamSize);
        this.consumer = createConsumer();
    }

    private KafkaConsumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "stream-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("your-topic-name"));
        return consumer;
    }

    public void generateStockEventsFromKafka() {
        ConsumerRecords<String, String> records = consumer.poll(1000);
        int counter = 0;
        for (ConsumerRecord<String, String> record : records) {
            if (counter >= myStream.getSize()) {
                break;
            }
            // Assuming the message format is "id,timestamp,eventType,price"
            String[] parts = record.value().split(",");
            int id = Integer.parseInt(parts[0]);
            int timestamp = Integer.parseInt(parts[1]);
            String eventType = parts[2];
            int price = Integer.parseInt(parts[3]);

            ABCEvent event = new ABCEvent(id, timestamp, eventType, price);
            myStream.getEvents()[counter++] = event;
        }
    }

    public Stream getMyStream() {
        return myStream;
    }
}
