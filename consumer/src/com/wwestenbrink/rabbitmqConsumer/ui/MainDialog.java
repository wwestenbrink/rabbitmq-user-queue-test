package com.wwestenbrink.rabbitmqConsumer.ui;

import com.rabbitmq.client.*;
import com.wwestenbrink.common.log.LogArea;
import com.wwestenbrink.rabbitmqConsumer.model.User;
import com.wwestenbrink.rabbitmqConsumer.model.UserModel;
import net.jodah.lyra.ConnectionOptions;
import net.jodah.lyra.Connections;
import net.jodah.lyra.config.Config;
import net.jodah.lyra.config.RecoveryPolicies;
import net.jodah.lyra.event.ChannelListener;
import net.jodah.lyra.event.ConnectionListener;
import net.jodah.lyra.event.ConsumerListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class MainDialog extends JDialog implements TableModelListener {
    private static final String appTitle = "RabbitMQ user";
    private static final int startClients = 100;

    private int clientsConnected = 0;

    private JPanel contentPane;
    private LogArea logTextArea;
    private JButton quitButton;
    private JButton connectAllButton;
    private JTable clientTable;
    private JLabel statusLabel;
    private JButton connectButton;
    private JButton ackButton;
    private JButton disconnectAllButton;
    private JButton ackAllButton;
    private JButton disconnectButton;
    private JLabel selectLabel;

    UserModel userModel;

    private ArrayList<Integer> selectedTableRows = new ArrayList();

    public MainDialog() {
        super(new MainFrame(appTitle));

        setTitle(appTitle);
        setContentPane(contentPane);

        updateStatusLabel();

        // initialize client model
        userModel = new UserModel(startClients);
        userModel.addTableModelListener(this);

        // initialize client table
        clientTable.setModel(userModel);
        clientTable.getColumnModel().getColumn(0).setMaxWidth(100);
        clientTable.getColumnModel().getColumn(1).setMaxWidth(100);
        clientTable.getColumnModel().getColumn(2).setMaxWidth(100);
        clientTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                selectedTableRows.clear();
                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();

                if (lsm.isSelectionEmpty()) {
                    selectLabel.setText("No selection");
                } else {
                    // Find out which indexes are selected.
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();
                    for (int i = minIndex; i <= maxIndex; i++) {
                        if (lsm.isSelectedIndex(i)) {
                            selectedTableRows.add(i);
                        }
                    }
                    selectLabel.setText(selectedTableRows.size() + " rows selected");
                }

                connectButton.setEnabled(!lsm.isSelectionEmpty());
                ackButton.setEnabled(!lsm.isSelectionEmpty());
                disconnectButton.setEnabled(!lsm.isSelectionEmpty());
            }
        });

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int index: selectedTableRows){
                    userModel.getUser(index).connect();
                }
            }
        });
        ackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int index: selectedTableRows){
                    userModel.getUser(index).ackAll();
                }
            }
        });
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int index: selectedTableRows){
                    userModel.getUser(index).disconnect();
                }
            }
        });

        // initialize dialog
        setModal(true);
        getRootPane().setDefaultButton(connectAllButton);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onQuit();
            }
        });

        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onQuit();
            }
        });
        connectAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userModel.connectAll();
            }
        });
        disconnectAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userModel.disconnectAll();
            }
        });
        ackAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userModel.ackAll();
            }
        });

        // postphone further initialisation to dialog open
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                init();
            }
        });
    }

    public void init() {
        try {
            Config config = new Config();

            /*config.withRecoveryPolicy(new RecoveryPolicy()
                    .withMaxAttempts(20)
                    .withInterval(Duration.seconds(1))
                    .withMaxDuration(Duration.minutes(5)));

            config.withRetryPolicy(new RetryPolicy()
                    .withBackoff(Duration.seconds(1), Duration.seconds(30))
                    .withMaxDuration(Duration.minutes(10)));*/

            config.withRecoveryPolicy(RecoveryPolicies.recoverAlways());

            config.withConnectionListeners(new ConnectionListener() {
                @Override
                public void onChannelRecovery(Connection connection) {
                    logTextArea.log("Connection Recovering channels");
                }

                @Override
                public void onCreate(Connection connection) {
                    logTextArea.log("Connection onCreate");
                }

                @Override
                public void onCreateFailure(Throwable failure) {
                    logTextArea.log("Connection onCreateFailure");
                }

                @Override
                public void onRecovery(Connection connection) {
                    logTextArea.log("Connection Connection recovered");
                }

                @Override
                public void onRecoveryFailure(Connection connection, Throwable failure) {
                    logTextArea.log("Connection onRecoveryFailure");
                }
            }).withChannelListeners(new ChannelListener() {
                @Override
                public void onConsumerRecovery(Channel channel) {
                    logTextArea.log("Channel " + channel.getChannelNumber() + " onConsumerRecovery");
                }

                @Override
                public void onCreate(Channel channel) {
                    logTextArea.log("Channel " + channel.getChannelNumber() + " onCreate");
                }

                @Override
                public void onCreateFailure(Throwable failure) {
                    logTextArea.log("Channel onCreateFailure");
                }

                @Override
                public void onRecovery(Channel channel) {
                    logTextArea.log("Channel " + channel.getChannelNumber() + " onRecovery");
                }

                @Override
                public void onRecoveryFailure(Channel channel, Throwable failure) {
                    logTextArea.log("Channel " + channel.getChannelNumber() + " onRecoveryFailure");
                }
            }).withConsumerListeners(new ConsumerListener() {
                @Override
                public void onBeforeRecovery(Consumer consumer, Channel channel) {
                    logTextArea.log("Consumer from channel " + channel.getChannelNumber() + " onBeforeRecovery");
                }

                @Override
                public void onAfterRecovery(Consumer consumer, Channel channel) {
                    logTextArea.log("Consumer from channel " + channel.getChannelNumber() + " onAfterRecovery");
                }

                @Override
                public void onRecoveryFailure(Consumer consumer, Channel channel, Throwable failure) {
                    logTextArea.log("Consumer from channel " + channel.getChannelNumber() + " onRecoveryFailure: " + failure);
                }
            });

            ConnectionOptions options = new ConnectionOptions().withHost("localhost");
            Connection connection = Connections.create(options, config);

            connection.addShutdownListener(new ShutdownListener() {
                @Override
                public void shutdownCompleted(ShutdownSignalException cause) {
                    logTextArea.log("Connection shutdown, cause:" + cause);
                }
            });

            logTextArea.log("Connected to RabbitMQ");

            for (int i = 0; i < startClients; i++) {
                User user = new User();
                user.setLogger(logTextArea);
                user.setConnection(connection);
                userModel.addUser(user);
                user.connect();
            }

        } catch (Exception ex) {
            System.out.println("consumer init: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {
        MainDialog dialog = new MainDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    /**
     * recalculate some properties when userModel changes
     */
    public void tableChanged(TableModelEvent e) {
        int nConnected = 0;

        for (int i = 0, rows = userModel.getRowCount(); i < rows; i++) {
            User user = userModel.getUser(i);
            if (user.isConnected()) {
                nConnected++;
            }
        }

        clientsConnected = nConnected;
        updateStatusLabel();
    }

    private void updateStatusLabel() {
        String status;

        status = clientsConnected + " connected";
        statusLabel.setText(status);
    }

    private void onQuit() {
        System.out.println("quiting");
        dispose();
    }
}