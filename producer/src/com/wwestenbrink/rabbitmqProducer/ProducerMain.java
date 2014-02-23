package com.wwestenbrink.rabbitmqProducer;

import com.rabbitmq.client.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ProducerMain {
    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        for (int i =1; i <=500;i++) {
            String queue = "user"+i;
            channel.queueDeclare(queue, true, false, false, null);
            String time = new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
            String message = "message produced on "+ time;

            AMQP.BasicProperties properties = new AMQP.BasicProperties()
                    .builder()
                    .expiration("60000") // expire message after 60s
                    .deliveryMode(2) // persist message
                    .build();

            channel.basicPublish("", queue, properties, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }

        channel.close();
        connection.close();
    }
}
