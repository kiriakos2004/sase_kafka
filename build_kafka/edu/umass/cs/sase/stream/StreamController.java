package edu.umass.cs.sase.stream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class StreamController {
    private Stream myStream;
    private KafkaConsumer<String, String> consumer;
    private volatile boolean running = true;
    private int size;
    private Random randomGenerator;

    public StreamController(int streamSize, String eventType) {
        this.size = streamSize;
        this.myStream = new Stream(streamSize);
        this.randomGenerator = new Random(11);
        this.consumer = createConsumer();
        System.out.println("StreamController initialized with stream size: " + streamSize + " and event type: " + eventType);
        
        if (eventType.equalsIgnoreCase("abcevent")) {
            this.generateABCEvents();
        } else if (eventType.equalsIgnoreCase("stockevent")) {
            this.generateStockEvents();
        } else if (eventType.equalsIgnoreCase("kafkastockevent")) {
            this.generateStockEventsFromKafka();
        }
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
        List<StockEvent> events = new ArrayList<>();

        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    if (skipHeader) {
                        skipHeader = false;
                        continue;
                    }
                    // Process the record and convert it to a StockEvent
                    System.out.printf("Consumed record with key %s and value %s%n", record.key(), record.value());
                    // Assuming the record.value() is a CSV formatted string: id,timestamp,symbol,price,volume
                    String[] fields = record.value().split(",");
                    try {
                        int id = Integer.parseInt(fields[0]);
                        int timestamp = Integer.parseInt(fields[1]);
                        int symbol = Integer.parseInt(fields[2]);
                        int price = Integer.parseInt(fields[3]);
                        int volume = Integer.parseInt(fields[4]);
                        StockEvent event = new StockEvent(id, timestamp, symbol, price, volume);
                        events.add(event);
                        counter++;
                        if (counter >= size) {
                            running = false;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid record: " + record.value());
                    }
                }
            }
        } catch (WakeupException e) {
            if (running) {
                throw e;
            }
        } finally {
            consumer.close();
            System.out.println("Kafka consumer closed");
        }
        myStream.setEvents(events.toArray(new StockEvent[0]));
    }

    public void generateABCEvents() {
        Random r = new Random(11);
        ABCEvent events[] = new ABCEvent[this.size];
        int id;
        int timestamp = 0;
        String symbol;
        int value;

        for (int i = 0; i < size; i++) {
            id = i;
            timestamp = id;
            symbol = String.valueOf((char) (r.nextInt(26) + 'A'));
            value = r.nextInt(100) + 1;

            events[i] = new ABCEvent(id, timestamp, symbol, value);
        }
        myStream.setEvents(events);
    }

    public void generateStockEvents() {
        Random r = new Random(11);
        StockEvent events[] = new StockEvent[this.size];
        int id;
        int timestamp = 0;
        int symbol;
        int volume;
        int price = r.nextInt(100);
        
        for (int i = 0; i < size; i++) {
            id = i;
            timestamp = id;
            symbol = r.nextInt(StockStreamConfig.numOfSymbol) + 1;
            price = r.nextInt(StockStreamConfig.maxPrice) + 1;
            volume = r.nextInt(StockStreamConfig.maxVolume) + 1;

            events[i] = new StockEvent(id, timestamp, symbol, price, volume);
        }
        myStream.setEvents(events);
    }
    
    public Stream getMyStream() {
        return myStream;
    }

    // Adding methods from the old StreamController
    public void generateStockEventsAsConfigType() {
        Random r = new Random(StockStreamConfig.randomSeed);
        StockEvent[] events = new StockEvent[this.size];
        int id;
        int timestamp = 0;
        int symbol;
        int volume;
        int price = r.nextInt(100);
        String eventType = "stock";
        
        for (int i = 0; i < size; i++) {
            id = i;
            timestamp = id;
            symbol = r.nextInt(StockStreamConfig.numOfSymbol) + 1;
            price = r.nextInt(StockStreamConfig.maxPrice) + 1;
            volume = r.nextInt(StockStreamConfig.maxVolume) + 1;

            events[i] = new StockEvent(id, timestamp, symbol, price, volume);
        }
        myStream.setEvents(events);
    }
}

