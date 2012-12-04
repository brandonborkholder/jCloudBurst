package com.github.jcloudburst.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import net.miginfocom.swing.MigLayout;

import com.github.jcloudburst.ImportListener;
import com.github.jcloudburst.JDBCImport;

@SuppressWarnings("serial")
public class ProgressPanel extends ConfigStepPanel {
  private JButton runButton;

  private JLabel totalRowsProcessedLabel;
  private JLabel currentSourceLabel;
  private JProgressBar percentProcessedBar;

  private JLabel timeRunningLabel;

  private JTextArea historyArea;

  private double percentThruSource;
  private long numberOfRowsProcessed;
  private String currentSourceString;
  private long startTime;

  public ProgressPanel() {
    super("Import");

    runButton = new JButton("Import");
    runButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        runImportTask();
      }
    });

    totalRowsProcessedLabel = new JLabel();
    percentProcessedBar = new JProgressBar();
    percentProcessedBar.setMinimum(0);
    percentProcessedBar.setMaximum(1000);
    currentSourceLabel = new JLabel();
    timeRunningLabel = new JLabel();
    historyArea = new JTextArea();
    historyArea.setEditable(false);

    setLayout(new MigLayout("", "[|grow]", "[||||grow||]"));
    add(new JLabel("Running Time: "), "right");
    add(timeRunningLabel, "wrap");
    add(new JLabel("Total Rows Imported: "), "right");
    add(totalRowsProcessedLabel, "wrap");
    add(new JLabel("Current Source: "), "right");
    add(currentSourceLabel, "wrap");
    add(new JLabel("Source Completion: "), "right");
    add(percentProcessedBar, "grow,wrap");
    add(new JLabel("Log: "), "right,top");
    add(new JScrollPane(historyArea), "grow,wrap");
    add(new JSeparator(SwingConstants.HORIZONTAL), "span,grow,wrap");
    add(runButton, "span,right");
  }

  @Override
  protected void flushConfigurationToUI() throws IllegalStateException {
    // nop
  }

  @Override
  protected void flushUIToConfiguration() throws IllegalStateException {
    // nop
  }

  private void runImportTask() {
    setBackgroundTaskStatus("Importing ...");
    clearStatusLabels();

    new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() throws Exception {
        doImport();
        return null;
      }

      protected void done() {
        setBackgroundTaskStatus(null);
        updateStatusLabels();

        try {
          get();

          appendToHistoryLog("import complete");
        } catch (Exception e) {
          e.printStackTrace();
          appendToHistoryLog("import failed, see logs");
        }
      }
    }.execute();
  }

  private void clearStatusLabels() {
    historyArea.setText(null);
    totalRowsProcessedLabel.setText(null);
    percentProcessedBar.setValue(0);
    currentSourceLabel.setText(null);
    timeRunningLabel.setText(null);
  }

  private void appendToHistoryLog(String text) {
    historyArea.append(new SimpleDateFormat("EEE HH:mm:ss").format(new Date()));
    historyArea.append(" ");
    historyArea.append(text);
    historyArea.append("\n");
  }

  private void updateStatusLabels() {
    totalRowsProcessedLabel.setText(String.format("%,d", numberOfRowsProcessed));
    percentProcessedBar.setValue((int) (percentThruSource * 1000));

    if (currentSourceLabel.getText() == null || !currentSourceLabel.getText().equals(currentSourceString)) {
      appendToHistoryLog("started " + currentSourceString);
    }

    currentSourceLabel.setText(currentSourceString);

    long now = System.currentTimeMillis();
    long inSeconds = (now - startTime) / 1000;
    long seconds = inSeconds % 60;
    long minutes = (inSeconds / 60) % 60;
    long hours = (inSeconds / 60 / 60) % 24;
    long days = inSeconds / 60 / 60 / 24;

    int first = 0;
    first = minutes > 0 ? 1 : first;
    first = hours > 0 ? 2 : first;
    first = days > 0 ? 3 : first;

    StringBuilder text = new StringBuilder();
    switch (first) {
    case 3:
      text.append(days);
      text.append("d ");

    case 2:
      text.append(hours);
      text.append("h ");

    case 1:
      text.append(minutes);
      text.append("m ");

    case 0:
      text.append(seconds);
      text.append("s");
    }

    timeRunningLabel.setText(text.toString());
  }

  private void doImport() {
    startTime = System.currentTimeMillis();

    Timer updateTimer = new Timer(100, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateStatusLabels();
      }
    });
    updateTimer.setRepeats(true);
    updateTimer.start();

    try {
      JDBCImport importer = new JDBCImport(config.clone());
      importer.execute(new ImportListener() {
        @Override
        public void setCurrentSource(String source) {
          currentSourceString = source;
        }

        @Override
        public void setPercentThroughSource(double percent) {
          percentThruSource = percent;
        }

        @Override
        public void totalRowsProcessed(long count) {
          numberOfRowsProcessed = count;
        }
      });
    } catch (IOException | SQLException e) {
      e.printStackTrace();
    } finally {
      updateTimer.stop();
    }
  }
}
