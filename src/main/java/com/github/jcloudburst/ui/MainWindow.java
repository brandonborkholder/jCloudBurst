package com.github.jcloudburst.ui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

import com.github.jcloudburst.config.ImportConfig;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
  protected CardLayout switcherLayout;
  protected JPanel switcherPanel;

  protected ImportConfig config;

  protected int currentStep;
  protected List<ConfigStepPanel> steps;

  protected JButton nextButton;
  protected JButton backButton;

  public MainWindow(ImportConfig config) {
    this.config = config;

    switcherLayout = new CardLayout();
    switcherPanel = new JPanel(switcherLayout);

    steps = new ArrayList<ConfigStepPanel>();
    steps.add(new DatabaseConnectionPanel());
    steps.add(new TableChooserPanel());
    steps.add(new FileSourceChooser());
    steps.add(new ColumnMapperPanel());
    steps.add(new ProgressPanel());

    int index = 0;
    for (ConfigStepPanel panel : steps) {
      panel.setName(String.valueOf(index));
      switcherPanel.add(panel, panel.getName());
      index++;
    }

    nextButton = new JButton("Next >>");
    nextButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        next();
      }
    });

    backButton = new JButton("<< Back");
    backButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        back();
      }
    });

    currentStep = 0;
    updateUIForCurrentStep();

    setLayout(new MigLayout("", "[grow|grow]", "[grow|]"));
    add(switcherPanel, "span,grow,wrap");
    add(backButton, "left");
    add(nextButton, "right");

    try {
      steps.get(currentStep).loadConfiguration(config);
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
    }
  }

  protected void next() {
    ConfigStepPanel current = steps.get(currentStep);
    ConfigStepPanel next = steps.get(currentStep + 1);

    try {
      current.saveToConfiguration(config);
      next.loadConfiguration(config);
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
    }

    currentStep++;
    updateUIForCurrentStep();
  }

  protected void back() {
    currentStep--;
    updateUIForCurrentStep();
  }

  protected void updateUIForCurrentStep() {
    switcherLayout.show(switcherPanel, String.valueOf(currentStep));

    backButton.setVisible(currentStep > 0);
    nextButton.setVisible(currentStep < steps.size() - 1);
  }

  public static void main(String[] args) throws Exception {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    MainWindow window = new MainWindow(new ImportConfig());
    window.setPreferredSize(new Dimension(1024, 768));
    window.pack();
    window.setLocationRelativeTo(null);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setVisible(true);
  }
}
