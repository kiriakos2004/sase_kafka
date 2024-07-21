from confluent_kafka import Consumer, KafkaError

def consume_messages():
    consumer = Consumer({
        'bootstrap.servers': 'localhost:29092',
        'group.id': 'my_group',
        'auto.offset.reset': 'earliest'
    })

    # Adjust the topic name to match your Debezium topic
    consumer.subscribe(['dbprefix.klimakosimes.test'])

    while True:
        msg = consumer.poll(1.0)

        if msg is None:
            continue
        if msg.error():
            if msg.error().code() == KafkaError._PARTITION_EOF:
                continue
            else:
                print(msg.error())
                break

        print(f'Received message: {msg.value().decode("utf-8")}')

    consumer.close()

if __name__ == "__main__":
    consume_messages()
