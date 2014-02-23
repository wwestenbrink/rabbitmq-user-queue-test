## RabbitMQ user queue test project
The goal of this project is to test using RabbitMQ queues as user mailboxes.

* When the user is connected it will receives new messages immediately.
* When the user is not connected their messages are preserved in a their queue by rabbitMQ .
* Messages are preserved until they are either acknowledged or expire.

## Implementation
A small command line producer connects to rabbitMq on localhost and adds one persistent message to all user queues, a message expires after 60s.

The swing consumer application adds an arbitrary number of users, each opening a channel and start consuming messages from their queue.

Messages have to be acknowledged or they will show up again after user reconnects (unless the message expired).

Connect, acknowledge & disconnect can be controlled for all users or selected user(s).

## High availability
The consumer application uses [lyra] (https://github.com/jhalterman/lyra), a high availability RabbitMQ client written by jhalterman.

It automatically recovers AMQP resources when unexpected failures occur and tolerates the RabbitMQ server going away for a while.

## Modify / Build / Run
For now you can open the project using IntelliJ to modify, build & run the consumer/producer applications.

## License
[Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html)