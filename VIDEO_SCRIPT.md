# Video Explanation Script

1. Introduce yourself and the assignment.
2. Show docker-compose.yml and start the Kafka stack with `docker-compose up -d`.
3. Show `order.avsc` and explain the fields.
4. Run the main consumer, retry consumer and DLQ consumer.
5. Run the producer and show messages being processed and the running average.
6. Intentionally cause failures (e.g. by changing the code) and show messages flowing to retry and DLQ topics.
7. Conclude by summarising how the system meets the requirements.
