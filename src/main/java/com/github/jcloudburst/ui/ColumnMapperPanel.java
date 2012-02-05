package com.github.jcloudburst.ui;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import com.github.jcloudburst.ColumnMapType;
import com.github.jcloudburst.ColumnsType;
import com.github.jcloudburst.ConfigurationType;
import com.github.jcloudburst.JDBCType;

@SuppressWarnings("serial")
public class ColumnMapperPanel extends ConfigStepPanel {
  private List<ColumnMapType> columnMaps;

  private void populateColumns(List<ColumnInfo> columns, List<FieldInfo> fields, List<FieldInfo> defaultFields) {
    removeAll();

    columnMaps = new ArrayList<ColumnMapType>(columns.size());
    for (ColumnInfo column : columns) {
      ColumnMapType map = new ColumnMapType();
      map.setDbColumn(column.name);
      columnMaps.add(map);
    }

    Vector<FieldInfo> fieldsVector = new Vector<FieldInfo>(fields);

    JPanel listPanel = new JPanel(new MigLayout("", "[|grow,sg|grow,sg|grow,sg|grow,sg]"));
    for (int index = 0; index < columns.size(); index++) {
      addColumn(index, listPanel, columns.get(index), fieldsVector, defaultFields.get(index));
    }

    setLayout(new BorderLayout());
    add(new JScrollPane(listPanel), BorderLayout.CENTER);
  }

  private void addColumn(final int index, JPanel container, ColumnInfo column, Vector<FieldInfo> fields, FieldInfo defaultField) {
    final JCheckBox skipBox = new JCheckBox();
    JLabel columnInfoLabel = new JLabel(column.name + " (" + column.typeName + ")");
    final JComboBox<FieldInfo> fieldChooser = new JComboBox<FieldInfo>(fields);
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
          columnMaps.get(index).setFormat(dateFormatField.getText());
        }
      });
    }

    fieldChooser.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        columnMaps.get(index).setFileColIndex(((FieldInfo) fieldChooser.getSelectedItem()).index);
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
        columnMaps.get(index).setFixedValue(fixedValueField.getText());
      }
    });

    skipBox.setEnabled(column.nullable);
    fieldChooser.setSelectedItem(defaultField);
    dateFormatField.setEditable(column.isDate());

    container.add(skipBox, "grow");
    container.add(columnInfoLabel, "grow");
    container.add(fieldChooser, "grow");
    container.add(fixedValueField, "grow");
    container.add(dateFormatField, "grow,wrap");
  }

  private List<ColumnInfo> getColumns(JDBCType connectionInfo, String table) throws SQLException {
    String url = connectionInfo.getUrl();
    String username = connectionInfo.getUsername();
    String password = connectionInfo.getPassword();

    Connection connection = DriverManager.getConnection(url, username, password);
    try {
      DatabaseMetaData metadata = connection.getMetaData();
      ResultSet set = metadata.getColumns(null, null, table, null);

      List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
      while (set.next()) {
        columns.add(new ColumnInfo(set));
      }

      set.close();
      Collections.sort(columns);
      return columns;
    } finally {
      try {
        connection.close();
      } catch (SQLException e) {
        // ignore
      }
    }
  }
  
  private List<FieldInfo> getFields(ConfigurationType config) throws IOException {
    
  }
  
  private List<FieldInfo> getDefaultFields(List<ColumnInfo> columns, List<FieldInfo> fields, ColumnsType columnMap) {
  }

  @Override
  public void initFromConfiguration(ConfigurationType config) throws SQLException, IOException {
    List<ColumnInfo> columns = getColumns(config.getJdbc(), config.getTable());
    List<FieldInfo> fields = getFields(config);
    List<FieldInfo> defaultFields = getDefaultFields(columns, fields, config.getMapping());

    populateColumns(columns, fields, defaultFields);
  }

  @Override
  public void addConfiguration(ConfigurationType config) {
    config.getMapping().getColumn().clear();
    config.getMapping().getColumn().addAll(columnMaps);
  }

  private static class ColumnInfo implements Comparable<ColumnInfo> {
    String name;
    boolean nullable;
    int index;
    int sqlType;
    String typeName;
    String defValue;

    ColumnInfo(ResultSet set) throws SQLException {
      name = set.getString(4);
      sqlType = set.getInt(5);
      typeName = set.getString(6);
      defValue = set.getString(13);
      index = set.getInt(17);
      nullable = set.getInt(18) == DatabaseMetaData.columnNullable;
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
