package com.github.jcloudburst.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import com.github.jcloudburst.ColumnMapType;
import com.github.jcloudburst.ColumnsMapGuesser;
import com.github.jcloudburst.DelimitedFileReader;
import com.github.jcloudburst.DelimitedSource;
import com.github.jcloudburst.ui.DatabaseConnectionPanel.ConnectionState;
import com.github.jcloudburst.ui.TableChooserPanel.TableRef;

@SuppressWarnings("serial")
public class ColumnMapperPanel extends ConfigStepPanel {
  private List<ColumnMapType> columnMaps;

  private ConnectionState lastColumnsState;
  private List<ColumnInfo> columns;

  private FileSourceState lastFieldState;
  private List<FieldInfo> fields;

  public ColumnMapperPanel() {
    super("Columns");
  }

  @Override
  protected void flushConfigurationToUI() throws IllegalStateException {
    removeAll();
    
    ensureHasColumns();
  }

  @Override
  protected void flushUIToConfiguration() throws IllegalStateException {
    config.getMapping().getColumn().clear();
    config.getMapping().getColumn().addAll(columnMaps);
  }

  private void ensureHasColumns() {
    if (lastColumnsState == null || !lastColumnsState.equals(new ConnectionState(config))) {
      columns = null;
      new SwingWorker<List<ColumnInfo>, Void>() {
        @Override
        protected List<ColumnInfo> doInBackground() throws Exception {
          return getColumns();
        }

        @Override
        protected void done() {
          try {
            columns = get();
            ensureHasFields();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }.execute();
    } else {
      ensureHasFields();
    }
  }

  private List<ColumnInfo> getColumns() throws SQLException {
    try (Connection c = getConnection()) {
      TableRef tableRef = new TableRef(config.getTable());
      DatabaseMetaData metadata = c.getMetaData();
      ResultSet set = metadata.getColumns(tableRef.catalog, tableRef.schema, tableRef.name, null);
  
      List<ColumnInfo> columns = new ArrayList<>();
      while (set.next()) {
        columns.add(new ColumnInfo(set));
      }
  
      set.close();
      Collections.sort(columns);
      return columns;
    }
  }

  private void ensureHasFields() {
    if (lastFieldState == null || !lastFieldState.equals(new FileSourceState(config))) {
      fields = null;
      new SwingWorker<List<FieldInfo>, Void>() {
        @Override
        protected List<FieldInfo> doInBackground() throws Exception {
          return getFields();
        }

        @Override
        protected void done() {
          try {
            fields = get();
            setupUI();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }.execute();
    } else {
      setupUI();
    }
  }

  private List<FieldInfo> getFields() throws IOException {
    List<String> fieldNames = null;
    for (DelimitedSource source : config.getCsv()) {
      DelimitedFileReader header = new DelimitedFileReader(source);
      if (header.getHeader() != null) {
        fieldNames = header.getHeader();
        header.close();
        break;
      } else {
        header.close();
      }
    }
  
    List<FieldInfo> fields = null;
    if (fieldNames != null) {
      fields = new ArrayList<>(fieldNames.size());
      for (String name : fieldNames) {
        FieldInfo field = new FieldInfo();
        field.name = name;
        field.index = fields.size();
  
        fields.add(field);
      }
    }
  
    return fields;
  }

  private void setupUI() {
    removeAll();
    
    initializeColumnMaps();

    JPanel listPanel = new JPanel(new MigLayout("", "[|grow,sg|grow,sg|grow,sg|grow,sg]"));
    listPanel.add(new JLabel("Skip"));
    listPanel.add(new JLabel("DB Column"));
    listPanel.add(new JLabel("File Field"));
    listPanel.add(new JLabel("Fixed Value"));
    listPanel.add(new JLabel("Date Format"), "wrap");

    for (int index = 0; index < columnMaps.size(); index++) {
      addColumn(index, listPanel);
    }

    setLayout(new BorderLayout());
    add(new JScrollPane(listPanel), BorderLayout.CENTER);
    revalidate();
  }

  private void addColumn(final int index, JPanel container) {
    final ColumnInfo column = columns.get(index);
    final ColumnMapType map = columnMaps.get(index);
  
    final JCheckBox skipBox = new JCheckBox();
    JLabel columnInfoLabel = new JLabel(column.name + " (" + column.typeName + ")");
    final JComboBox<FieldInfo> fieldChooser = new JComboBox<>(fields.toArray(new FieldInfo[fields.size()]));
    final JTextField dateFormatField = new JTextField("yyyy-MM-dd HH:mm:ss");
    final JTextField fixedValueField = new JTextField();
  
    skipBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        fieldChooser.setEnabled(!skipBox.isSelected());
        dateFormatField.setEnabled(!skipBox.isSelected());
        fixedValueField.setEnabled(!skipBox.isSelected());
      }
    });
  
    if (column.isDate()) {
      dateFormatField.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void removeUpdate(DocumentEvent e) {
          changedUpdate(e);
        }
  
        @Override
        public void insertUpdate(DocumentEvent e) {
          changedUpdate(e);
        }
  
        @Override
        public void changedUpdate(DocumentEvent e) {
          map.setFormat(dateFormatField.getText());
        }
      });
    }
  
    fieldChooser.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        FieldInfo field = (FieldInfo) fieldChooser.getSelectedItem();
        if (field == null) {
          map.setFileColIndex(null);
          map.setFileColName(null);
        } else if (field.name == null) {
          map.setFileColIndex(field.index);
          fixedValueField.setText(null);
        } else {
          map.setFileColName(field.name);
          fixedValueField.setText(null);
        }
      }
    });
  
    fixedValueField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
      }
  
      @Override
      public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
      }
  
      @Override
      public void changedUpdate(DocumentEvent e) {
        String text = fixedValueField.getText();
        if (text == null || text.isEmpty()) {
          map.setFixedValue(null);
        } else {
          map.setFixedValue(text);
          fieldChooser.setSelectedItem(null);
        }
      }
    });
  
    skipBox.setEnabled(column.nullable);
    fieldChooser.setSelectedItem(getExistingMap(column));
    dateFormatField.setVisible(column.isDate());
  
    container.add(skipBox, "grow");
    container.add(columnInfoLabel, "grow");
    container.add(fieldChooser, "grow");
    container.add(fixedValueField, "grow");
  
    if (column.isDate()) {
      container.add(dateFormatField, "grow,wrap");
    } else {
      JLabel notDate = new JLabel("not a recognized date column");
      notDate.setFont(notDate.getFont().deriveFont(Font.ITALIC));
      notDate.setForeground(Color.gray);
      notDate.setHorizontalAlignment(SwingConstants.CENTER);
      container.add(notDate, "grow,wrap");
    }
  }

  private void initializeColumnMaps() {
    Map<String, ColumnMapType> alreadyMapped = new TreeMap<>();
    for (ColumnMapType map : config.getMapping().getColumn()) {
      alreadyMapped.put(map.getDbColumn(), map);
    }
    
    List<FieldInfo> guessedFields = getGuessedFields(columns, fields);
    
    columnMaps = new ArrayList<>(columns.size());
    for (int i = 0; i < columns.size(); i++) {
      ColumnInfo column = columns.get(i);
      ColumnMapType map = alreadyMapped.get(column.name);
      if (map == null) {
        map = new ColumnMapType();
        map.setDbColumn(column.name);
        
        FieldInfo guessedField = guessedFields.get(i);
        if (guessedField != null) {
          if (guessedField.name == null) {
            map.setFileColIndex(guessedField.index);
          } else {
            map.setFileColName(guessedField.name);
          }
        }
      }
      
      columnMaps.add(map);
    }
  }

  private List<FieldInfo> getGuessedFields(List<ColumnInfo> columns, List<FieldInfo> fields) {
    if (fields == null) {
      return Arrays.asList(new FieldInfo[columns.size()]);
    }
  
    List<String> colNames = new ArrayList<>(columns.size());
    for (ColumnInfo column : columns) {
      colNames.add(column.name);
    }
  
    List<String> fieldNames = new ArrayList<>(fields.size());
    for (FieldInfo field : fields) {
      fieldNames.add(field.name);
    }
  
    ColumnsMapGuesser guesser = new ColumnsMapGuesser(colNames, fieldNames);
    Map<Integer, Integer> map = new TreeMap<>(guesser.guessMapping());
  
    List<FieldInfo> guessedFields = new ArrayList<>(columns.size());
    for (int i = 0; i < columns.size(); i++) {
      if (map.containsKey(i)) {
        guessedFields.add(fields.get(map.get(i)));
      } else {
        guessedFields.add(null);
      }
    }
  
    return guessedFields;
  }

  private FieldInfo getExistingMap(ColumnInfo column) {
    ColumnMapType map = null;
    for (ColumnMapType m : columnMaps) {
      if (m.getDbColumn().equals(column.name)) {
        map = m;
        break;
      }
    }
    
    if (map == null) {
      return null;
    } else if (map.getFileColIndex() != null ) {
      return fields.get(map.getFileColIndex());
    } else if (map.getFileColName() != null) {
      for (FieldInfo field : fields) {
        if (map.getFileColName().equals(field.name)) {
          return field;
        }
      }
    }
    
    return null;
  }

  private static class ColumnInfo implements Comparable<ColumnInfo> {
    String name;
    boolean nullable;
    int index;
    int sqlType;
    String typeName;

    ColumnInfo(ResultSet set) throws SQLException {
      name = set.getString(4);
      sqlType = set.getInt(5);
      typeName = set.getString(6);
      index = set.getInt(17);
      nullable = "YES".equals(set.getString(18));
    }

    @Override
    public int compareTo(ColumnInfo o) {
      return index - o.index;
    }

    boolean isDate() {
      switch (sqlType) {
      case Types.DATE:
      case Types.TIME:
      case Types.TIMESTAMP:
        return true;

      default:
        return false;
      }
    }
  }

  private static class FieldInfo implements Comparable<FieldInfo> {
    String name;
    int index;

    @Override
    public int compareTo(FieldInfo o) {
      return index - o.index;
    }

    @Override
    public String toString() {
      if (name == null || name.isEmpty()) {
        return "<" + (index + 1) + ">";
      } else {
        return name;
      }
    }
  }
}
