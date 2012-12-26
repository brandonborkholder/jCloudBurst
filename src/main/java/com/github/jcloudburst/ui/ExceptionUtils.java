package com.github.jcloudburst.ui;

import java.awt.Container;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

public class ExceptionUtils {
  private static final Logger LOGGER = Logger.getLogger(ExceptionUtils.class);
  
  public static void logAndShow(Container parent, Exception e) {
    LOGGER.error("Unhandled exception", e);
    JOptionPane.showMessageDialog(parent, "Error!", e.getMessage(), JOptionPane.ERROR_MESSAGE);
  }
}
