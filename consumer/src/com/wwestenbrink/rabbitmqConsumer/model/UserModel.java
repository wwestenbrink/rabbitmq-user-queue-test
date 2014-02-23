package com.wwestenbrink.rabbitmqConsumer.model;

import javax.swing.table.AbstractTableModel;
import java.util.concurrent.ConcurrentHashMap;

public class UserModel extends AbstractTableModel {
    private static final int COL_NAME = 0;
    private static final int COL_CONNECTED = 1;
    private static final int COL_MSG_UNACCKED = 2;
    private static final int COL_MSG_LAST = 3;

    private static final String[] colLabels = {"Client", "Status", "Unacknowledged", "Last message"};

    private ConcurrentHashMap<Integer, User> users;
    private int lastId = 0;

    public UserModel(int initialSize) {
        this.users = new ConcurrentHashMap<Integer, User>(initialSize);
    }

    public void addUser(User user) {
        user.setModel(this);
        user.setId(lastId);
        users.put(lastId, user);

        fireTableRowsInserted(lastId, lastId);
        lastId++;
    }

    public User getUser(int id) {
        return users.get(id);
    }

    @Override
    public int getRowCount() {
        return this.users.size();
    }

    @Override
    public int getColumnCount() {
        return colLabels.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return colLabels[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        User user = (User) users.get(rowIndex);

        try {
            switch (columnIndex) {
                case COL_NAME:
                    return user.getName();
                case COL_CONNECTED:
                    return user.isConnected() ? "Connected" : "disconnected";
                case COL_MSG_UNACCKED:
                    return user.getUnaccked();
                case COL_MSG_LAST:
                    return user.getLastMsg();
            }
        } catch (Exception e) {
            return e.getMessage();
        }

        return "";
    }

    public void connectAll() {
        for (User user : users.values()) {
            user.connect();
        }
    }

    public void disconnectAll() {
        for (User user : users.values()) {
            user.disconnect();
        }
    }

    public void ackAll() {
        for (User user : users.values()) {
            user.ackAll();
        }
    }
}
