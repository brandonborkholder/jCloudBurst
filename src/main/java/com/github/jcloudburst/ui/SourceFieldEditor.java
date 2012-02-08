package com.github.jcloudburst.ui;

import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

import com.github.jcloudburst.RowHandler;
import com.jidesoft.swing.AutoCompletionComboBox;

@SuppressWarnings("serial")
public class SourceFieldEditor extends AutoCompletionComboBox {
  @SuppressWarnings("unchecked")
  public SourceFieldEditor(List<String> sourceFields) {
    Vector<String> selections = new Vector<String>();
    selections.addAll(sourceFields);
    selections.addAll(RowHandler.VARIABLES);

    setModel(new DefaultComboBoxModel<String>(selections));

    setStrict(false);
    setStrictCompletion(false);
  }
}
