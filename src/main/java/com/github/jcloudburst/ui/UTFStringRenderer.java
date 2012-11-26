package com.github.jcloudburst.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class UTFStringRenderer extends JLabel implements TableCellRenderer, ListCellRenderer<String> {
  private DefaultListCellRenderer listDelegate;
  private DefaultTableCellRenderer tableDelegate;

  public UTFStringRenderer() {
    listDelegate = new DefaultListCellRenderer();
    listDelegate.setHorizontalAlignment(SwingConstants.CENTER);
    tableDelegate = new DefaultTableCellRenderer();
    tableDelegate.setHorizontalAlignment(SwingConstants.CENTER);
  }

  @Override
  public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean hasFocus) {
    value = getRendererString(value);
    return listDelegate.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    value = getRendererString((String) value);
    return tableDelegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
  }

  private String getRendererString(String value) {
    if (value == null || value.isEmpty()) {
      value = "<[ EMPTY ]>";
    } else {
      value = String.format("%s [ %s ]", value, getDescription(value));
    }

    return value;
  }

  private String getDescription(String str) {
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < str.length(); i++) {
      if (0 < i) {
        builder.append(" ");
      }

      /*
       * XXX does not handle high/low surrogate features
       */
      int codepoint = Character.codePointAt(str, i);
      String name = Character.getName(codepoint);
      if (name == null) {
        builder.append(String.format("\\u%X", codepoint));
      } else {
        builder.append(name);
      }
    }

    return builder.toString();
  }
}
