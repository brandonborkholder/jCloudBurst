package com.github.jcloudburst.ui;

import static com.github.jcloudburst.ui.ExceptionUtils.logAndShow;
import static java.lang.String.format;
import static javax.swing.BorderFactory.createCompoundBorder;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createEtchedBorder;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import com.github.jcloudburst.config.ImportConfig;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
  private static final Logger LOGGER = Logger.getLogger(MainWindow.class);

  private CardLayout switcherLayout;
  private JPanel switcherPanel;
  private LiteStatusBar statusBar;

  private ImportConfig config;

  private int currentStep;
  private JLabel instructionsLabel;
  private List<ConfigStepPanel> steps;

  private JButton nextButton;
  private JButton backButton;

  private JLabel leftLabel;

  public MainWindow(ImportConfig config) {
    setTitle("CloudBurst");

    try {
      setIconImage(ImageIO.read(MainWindow.class.getClassLoader().getResource("icons/cbicon.png")));
    } catch (IOException ex) {
      LOGGER.error("Error loading icon", ex);
    }

    this.config = config;

    switcherLayout = new CardLayout();
    switcherPanel = new JPanel(switcherLayout);

    statusBar = new LiteStatusBar();

    steps = new ArrayList<ConfigStepPanel>();
    steps.add(new DatabaseConnectionPanel());
    steps.add(new TableChooserPanel());
    steps.add(new FileSourceChooser());
    steps.add(new ColumnMapperPanel());
    steps.add(new ProgressPanel());

    instructionsLabel = new JLabel();
    instructionsLabel.setBorder(createCompoundBorder(createEtchedBorder(), createEmptyBorder(5, 5, 5, 5)));
    instructionsLabel.setVerticalAlignment(SwingConstants.TOP);

    int logoWidth = 150;
    try {
      Image logo = ImageIO.read(MainWindow.class.getClassLoader().getResource("icons/cblogo.png"));
      logoWidth = logo.getWidth(null);
      leftLabel = new JLabel(new ImageIcon(logo));
    } catch (IOException ex) {
      LOGGER.error("Error loading logo", ex);
    }

    attachStatusBarListener();

    int index = 0;
    for (ConfigStepPanel panel : steps) {
      panel.setName(String.valueOf(index));
      switcherPanel.add(panel, panel.getName());
      index++;
    }

    nextButton = new JButton();

    backButton = new JButton("<< Back");
    backButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        back();
      }
    });

    currentStep = 0;
    updateUIForCurrentStep();

    setLayout(new MigLayout("", format("[%dpx|grow]", logoWidth), "[|grow||20px:20px]"));
    add(leftLabel, "");
    add(switcherPanel, "grow,wrap,spany 2");
    add(instructionsLabel, "grow,wrap");
    add(backButton, "left");
    add(nextButton, "right,wrap");
    add(statusBar, "span,grow");

    try {
      steps.get(currentStep).loadConfiguration(config);
    } catch (Exception e) {
      logAndShow(this, e);
    }
  }

  protected void attachStatusBarListener() {
    TaskStatusTextListener listener = new TaskStatusTextListener() {
      @Override
      public void statusChanged(Object src, String text) {
        if (text != null) {
          LOGGER.info(text);
        }

        statusBar.setStatusText(text);
      }
    };

    for (ConfigStepPanel step : steps) {
      step.addTaskStatusListener(listener);
    }
  }

  protected void next() {
    ConfigStepPanel current = steps.get(currentStep);
    ConfigStepPanel next = steps.get(currentStep + 1);

    try {
      current.saveToConfiguration(config);
      next.loadConfiguration(config);
      currentStep++;
    } catch (Exception e) {
      logAndShow(this, e);
    }

    updateUIForCurrentStep();
  }

  protected void back() {
    currentStep--;
    updateUIForCurrentStep();
  }

  protected void updateUIForCurrentStep() {
    switcherLayout.show(switcherPanel, String.valueOf(currentStep));

    ConfigStepPanel active = steps.get(currentStep);
    instructionsLabel.setText("<html>" + active.getExplanationText() + "</html>");

    Action nextAction = active.getCustomNextAction();
    if (nextAction == null) {
      nextAction = new NextAction();
    }

    nextButton.setAction(nextAction);
    backButton.setVisible(currentStep > 0);
  }

  private class NextAction extends AbstractAction {
    NextAction() {
      super("Next >>");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      next();
    }
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("Uncaught exception on thread " + t, e);
      }
    });

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    MainWindow window = new MainWindow(new ImportConfig());
    window.setPreferredSize(new Dimension(1024, 768));
    window.pack();
    window.setLocationRelativeTo(null);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setVisible(true);
  }
}
