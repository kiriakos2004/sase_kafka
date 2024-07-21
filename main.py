import mysql.connector
from mysql.connector import Error
import time
import csv

def create_connection(host_name, user_name, user_password, db_name):
    connection = None
    try:
        connection = mysql.connector.connect(
            host=host_name,
            user=user_name,
            password=user_password,
            database=db_name,
            auth_plugin='mysql_native_password'  # Ensure using correct plugin
        )
        print("Connection to MySQL DB successful")
    except Error as e:
        print(f"The error '{e}' occurred")
    return connection

# Connection details
host_name = "localhost"
user_name = "root"
user_password = "password"
db_name = "testdb"

# Connect to the database
connection = create_connection(host_name, user_name, user_password, db_name)

def insert_data(connection, query, data):
    cursor = connection.cursor()
    try:
        cursor.execute(query, data)
        connection.commit()
        #print("Data inserted successfully")
    except Error as e:
        print(f"The error '{e}' occurred")

# Insert data query
insert_user_query = """
INSERT INTO test (var1, var2, var3, var4, var5, var6, var7, var8)
VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
"""

# Read data from CSV file and insert into database
csv_file_path = '227592820NR.csv'  # Update with the path to your CSV file

with open(csv_file_path, mode='r') as file:
    csv_reader = csv.reader(file)
    for i, row in enumerate(csv_reader):
        if i == 0:
            # Skip header row if CSV file has a header
            continue
        if i > 50:
            # Stop after inserting 10 rows
            break
        
        user_data = tuple(row)
        
        # Insert data into the table
        insert_data(connection, insert_user_query, user_data)
        
        # Wait for 1 second
        time.sleep(1/100)

# Close the connection
if connection.is_connected():
    connection.close()
    print("The connection is closed")
