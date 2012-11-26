package com.github.jcloudburst.ui;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;

@SuppressWarnings("serial")
public class SeparatorCellEditor extends DefaultCellEditor {
  @SuppressWarnings("unchecked")
  public SeparatorCellEditor() {
    super(new JComboBox<String>(new String[] { ",", ";", "\t" }));
    ((JComboBox<String>) getComponent()).setEditable(true);
    ((JComboBox<String>) getComponent()).setRenderer(new UTFStringRenderer());
    setClickCountToStart(2);
  }
}
