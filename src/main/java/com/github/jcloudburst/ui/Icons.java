package com.github.jcloudburst.ui;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Icons {
  public static final Icon deleteIcon;

  static {
    ImageIcon ico = null;
    try (InputStream in = Icons.class.getClassLoader().getResourceAsStream("icons/delete.png")) {
      ico = new ImageIcon(ImageIO.read(in));
    } catch (IOException e) {
      e.printStackTrace();
    }

    deleteIcon = ico;
  }
}
