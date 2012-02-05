package com.github.jcloudburst.ui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
  public static void main(String[] args) throws Exception {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    
    MainWindow window = new MainWindow();
    window.setPreferredSize(new Dimension(1024, 768));
    window.pack();
    window.setLocationRelativeTo(null);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setVisible(true);
  }
}
