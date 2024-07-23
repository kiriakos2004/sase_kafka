package edu.umass.cs.sase.stream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class StreamController {
    private Stream myStream;
    private KafkaConsumer<String, String> consumer;
    private volatile boolean running = true;

    public StreamController(int streamSize, String eventType) {
        this.myStream = new Stream(streamSize);
        this.consumer = createConsumer();
        System.out.println("StreamController initialized with stream size: " + streamSize + " and event type: " + eventType);
    }

    private KafkaConsumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "mygroup");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); 

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("test"));
        System.out.println("Kafka consumer created and subscribed to topic: test");
        return consumer;
    }

    public void generateStockEventsFromKafka() {
        System.out.println("Polling for records...");
        boolean skipHeader = true;
        int counter = 0;

        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10));
                for (ConsumerRecord<String, String> record : records) {
                    if (skipHeader) {
                        skipHeader = false;
                        System.out.println("Skipping header row: " + record.value());
                        continue;
                    }
                    if (counter >= myStream.getSize()) {
                        running = false; 
                        break;
                    }
                    System.out.println("Processing record: " + record.value());
                    String[] parts = record.value().split(",");
                    int id = Integer.parseInt(parts[0]);
                    int timestamp = Integer.parseInt(parts[1]);
                    String eventType = parts[2];
                    int price = Integer.parseInt(parts[3]);

                    ABCEvent event = new ABCEvent(id, timestamp, eventType, price);
                    myStream.getEvents()[counter++] = event;
                    System.out.println("Event created and added to stream: " + event);
                }
            }
        } catch (WakeupException e) {
            // Ignore exception if closing
            if (running) throw e;
        } catch (RuntimeException e) {
            System.err.println("Runtime exception: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected exception: " + e.getMessage());
        } finally {
            consumer.close();
            System.out.println("Finished processing records.");
        }
    }

    public void shutdown() {
        running = false;
        consumer.wakeup();
    }

    public Stream getMyStream() {
        return myStream;
    }

    public static void main(String[] args) {
        StreamController controller = new StreamController(10000, "StockEvent");
        Runtime.getRuntime().addShutdownHook(new Thread(controller::shutdown));
        controller.generateStockEventsFromKafka();
    }
}

