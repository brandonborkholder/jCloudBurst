package com.github.jcloudburst.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class TableChooserPanel extends ConfigStepPanel {
  private JList<String> tableList;
  private JTable columnsTable;

  private JCheckBox truncateCheckbox;
  private JButton populateTablesButton;

  private Map<String, Map<String, String>> tableToColumnsMap;

  public TableChooserPanel() {
    super("Table");
    tableToColumnsMap = new TreeMap<>();

    tableList = new JList<>();
    truncateCheckbox = new JCheckBox("Truncate before insert");
    populateTablesButton = new JButton("Get Tables");
    populateTablesButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        populateTables();
      }
    });

    tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tableList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        selectedTableChanged();
      }
    });

    setLayout(new MigLayout("", "[grow|grow]", "[|grow|]"));
    add(new JLabel("Destination Table"), "");
    add(new JLabel("Table Schema"), "wrap");
    add(new JScrollPane(tableList), "grow");
    add(new JScrollPane(columnsTable), "grow,wrap");
    add(populateTablesButton, "grow");
    add(truncateCheckbox, "grow");
  }

  protected void populateTables() {
    new SwingWorker<List<String>, Void>() {
      @Override
      protected List<String> doInBackground() throws Exception {
        return fetchTableNames();
      }

      @Override
      protected void done() {
        try {
          DefaultListModel<String> model = new DefaultListModel<>();
          for (String table : get()) {
            model.addElement(table);
          }

          tableToColumnsMap.clear();
          tableList.setModel(model);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }.done();
  }

  private List<String> fetchTableNames() throws SQLException {
    try (Connection c = getConnection()) {
      DatabaseMetaData metadata = c.getMetaData();
      ResultSet set = metadata.getTables(null, null, "%", new String[] { "TABLE" });

      List<String> names = new ArrayList<String>();
      while (set.next()) {
        names.add(set.getString(3));
      }

      set.close();
      return names;
    }
  }

  private void selectedTableChanged() {
    final String selected = tableList.getSelectedValue();
    if (selected == null) {
      columnsTable.setModel(new MapTableModel(Collections.<String, String> emptyMap()));
    }

    Map<String, String> columns = tableToColumnsMap.get(selected);
    if (columns == null) {
      new SwingWorker<Map<String, String>, Void>() {
        @Override
        protected Map<String, String> doInBackground() throws Exception {
          return fetchTableStructure(selected);
        }

        @Override
        protected void done() {
          try {
            tableToColumnsMap.put(selected, get());
            selectedTableChanged();
          } catch (Exception e) {
            // what now?
          }
        }
      }.execute();
    }
  }

  private Map<String, String> fetchTableStructure(String table) throws SQLException {
    try (Connection c = getConnection()) {
      Map<String, String> columns = new LinkedHashMap<>();

      DatabaseMetaData metadata = c.getMetaData();
      ResultSet set = metadata.getColumns(null, null, table, null);
      while (set.next()) {
        String name = set.getString(4);
        String type = set.getString(6);
        int size = set.getInt(7);

        columns.put(name, type + "(" + size + ")");
      }

      set.close();
      return columns;
    }
  }

  @Override
  protected void flushConfigurationToUI() throws SQLException, IOException, IllegalStateException {
    String table = config.getTable();
    tableList.setSelectedValue(table, true);
    truncateCheckbox.setSelected(config.isAppend() == null || !config.isAppend());
  }

  @Override
  protected void flushUIToConfiguration() throws SQLException, IOException, IllegalStateException {
    verifyNotEmpty("Table", tableList.getSelectedValue());

    config.setTable(tableList.getSelectedValue());
    config.setAppend(!truncateCheckbox.isSelected());
  }

  private static class MapTableModel extends AbstractTableModel {
    final Map<String, String> data;

    public MapTableModel(Map<String, String> map) {
      data = map;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
      case 0:
        return "Column";

      case 1:
        return "Type";

      default:
        return null;
      }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public int getRowCount() {
      return data.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      int r = rowIndex;
      for (Entry<String, String> entry : data.entrySet()) {
        if (r == 0) {
          switch (columnIndex) {
          case 0:
            return entry.getKey();

          case 1:
            return entry.getValue();

          default:
            return null;
          }
        }

        r--;
      }

      return null;
    }
  }
}
