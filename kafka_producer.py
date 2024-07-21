import csv
import time
from confluent_kafka import Producer

# Define server with port
bootstrap_servers = 'localhost:29092'

# Define topic name where the message will be published
topic_name = 'test'

# Producer configuration
conf = {
    'bootstrap.servers': bootstrap_servers
}

# Initialize producer variable
producer = Producer(conf)

# Publish text in defined topic
def delivery_report(err, msg):
    """ Called once for each message produced to indicate delivery result.
        Triggered by poll() or flush(). """
    if err is not None:
        print('Message delivery failed: {}'.format(err))
    else:
        print('Message delivered to {} [{}]'.format(msg.topic(), msg.partition()))

# Path to the CSV file
csv_file_path = 'stream_data_50000.csv'

# Produce messages from CSV file
with open(csv_file_path, 'r') as csvfile:
    csvreader = csv.reader(csvfile)
    for i, row in enumerate(csvreader):
        if i >= 49999:
            break
        message = ','.join(row)
        producer.produce(topic_name, key=None, value=message, callback=delivery_report)
        producer.poll(1)
        print(f"Sent message: {message}")
        time.sleep(1/10)

# Wait for any outstanding messages to be delivered and delivery reports
# to be received.
producer.flush()

# Print message
print("Messages Sent")
