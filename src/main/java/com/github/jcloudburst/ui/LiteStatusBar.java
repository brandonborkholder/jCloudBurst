package com.github.jcloudburst.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class LiteStatusBar extends JPanel {
  private JLabel timeLabel;
  private JLabel statusLabel;

  private long started;

  private Timer timer;

  public LiteStatusBar() {
    timeLabel = new JLabel();
    statusLabel = new JLabel();

    timeLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    statusLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

    setLayout(new MigLayout("insets 0", "[100:150|grow]", "[grow]"));

    add(timeLabel, "grow");
    add(statusLabel, "grow");

    timer = new Timer(500, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateTime();
      }
    });

    timer.setRepeats(true);

    setStatusText(null);
  }

  public void setStatusText(String text) {
    if (text == null) {
      started = -1;
      timer.stop();
      timeLabel.setText(null);
    } else if (!timer.isRunning()) {
      timer.start();
      started = System.currentTimeMillis();
    }

    statusLabel.setText(text);
  }

  private void updateTime() {
    DateFormat format = new SimpleDateFormat("HH:mm:ss");

    long now = System.currentTimeMillis();
    timeLabel.setText(format.format(new Date(now - started)));
  }
}
