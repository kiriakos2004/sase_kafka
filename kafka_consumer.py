from confluent_kafka import Consumer, KafkaException, KafkaError

# Define server with port
bootstrap_servers = 'localhost:29092'

# Define topic name from where the message will be received
topic_name = 'test'

# Consumer configuration
conf = {
    'bootstrap.servers': bootstrap_servers,
    'group.id': 'mygroup',
    'auto.offset.reset': 'earliest'
}

# Initialize consumer variable
consumer = Consumer(conf)

# Subscribe to the topic
consumer.subscribe([topic_name])

try:
    while True:
        msg = consumer.poll(timeout=0.01)  # Polling interval set to 10 milliseconds
        if msg is None:
            continue
        if msg.error():
            if msg.error().code() == KafkaError._PARTITION_EOF:
                # End of partition event
                print(f"{msg.topic()} [{msg.partition()}] reached end at offset {msg.offset()}")
            else:
                raise KafkaException(msg.error())
        else:
            print(f"Topic Name={msg.topic()}, Message={msg.value().decode('utf-8')}")
except KeyboardInterrupt:
    pass
finally:
    # Close down consumer to commit final offsets.
    consumer.close()
