package com.wwestenbrink.common.log;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogArea extends JTextArea implements Logger {
    public LogArea() {
        super();

        // make sure the caret is always updated to scroll automatically
        DefaultCaret caret = (DefaultCaret) getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    public void log(String msg, Object time) {

        String timeStr = new SimpleDateFormat("HH:mm:ss.SSS").format(time);
        append(timeStr + " " + msg + "\n");
    }

    public void log(String msg) {
        log(msg, Calendar.getInstance().getTime());
    }

}
