package com.wwestenbrink.rabbitmqConsumer.model;

import com.rabbitmq.client.*;
import com.wwestenbrink.common.log.Logger;

import javax.swing.*;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class User implements Consumer{
    private int id;
    private UserModel model;
    private int unaccked=0;
    private String lastMsg="";
    private Logger logger;
    private long lastDeliveryTag;

    private Connection connection;
    private Channel channel;
    private boolean connected;

    @Override
    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body)
            throws IOException {
        lastDeliveryTag = envelope.getDeliveryTag();

        String msg = new String(body);
        if (envelope.isRedeliver()) {
            msg += "(isRedeliver)";
        }
        lastMsg = msg;
        unaccked++;

        log("Recieved message " + lastDeliveryTag + ": " + msg);
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        unaccked = 0;
        connected = true;
        this.log("Connected");
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        log("handleCancelOk "+consumerTag);
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        log("handleCancelCancel "+consumerTag);
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        connected = false;
        this.log("Disconnected");
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        this.log("Recovered");
    }

    public void setModel(UserModel model) {
        this.model = model;
    }

    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return "user"+(id+1);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void connect() {
        if (isConnected()) {
            return; // already connected
        }
        try {
            channel = connection.createChannel();
            channel.queueDeclare(getName(), true, false, false, null);
            channel.basicConsume(getName(), false, "consumer." + getName(), this);
        } catch (Exception ex) {
            System.out.println(this+ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void disconnect(){
        if (!isConnected()) {
            return; // not connected
        }

        try {
            channel.close();
        } catch (Exception ex) {
            System.out.println(this+ex.getMessage());
            ex.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public int getUnaccked() throws Exception {
        return unaccked;
    }

    public void ackAll() {
        if (!isConnected()) {
            return; // not connected
        }

        if (unaccked == 0) {
            return; // no message to acknowledge
        }

        try {
            channel.basicAck(lastDeliveryTag, true);
            this.unaccked = 0;
            this.log("Acknowledged all");
        } catch (Exception ex) {
            System.out.println(this+ex.getMessage());
            ex.printStackTrace();
        }
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    private void log(final String msg) {
        final Date time = Calendar.getInstance().getTime();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (model != null)
                    model.fireTableRowsUpdated(id, id);

                logger.log(getName() + " " + msg, time);
            }
        });
    }
}
