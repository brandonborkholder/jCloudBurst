package com.github.jcloudburst.ui;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.github.jcloudburst.ConfigurationType;
import com.github.jcloudburst.JDBCType;

@SuppressWarnings("serial")
public class TableChooserPanel extends ConfigStepPanel {
  private JComboBox<String> tableChooser;

  private JCheckBox appendCheckbox;

  public TableChooserPanel() {
    tableChooser = new JComboBox<String>();
    appendCheckbox = new JCheckBox("Append");

    setLayout(new MigLayout("", "[|grow]"));
    add(new JLabel("Table: "), "");
    add(tableChooser, "grow,wrap");
    add(new JLabel("<html>Append to table or truncate before inserting data:</html>"), "");
    add(appendCheckbox, "grow,wrap");
  }

  private void populateTables(JDBCType connectionInfo) throws SQLException {
    String url = connectionInfo.getUrl();
    String username = connectionInfo.getUsername();
    String password = connectionInfo.getPassword();

    Connection connection = DriverManager.getConnection(url, username, password);
    try {
      DatabaseMetaData metadata = connection.getMetaData();
      ResultSet set = metadata.getTables(null, null, "%", null);

      Vector<String> names = new Vector<String>();
      while (set.next()) {
        names.add(set.getString(3));
      }

      set.close();
      tableChooser.setModel(new DefaultComboBoxModel<String>(names));
    } finally {
      try {
        connection.close();
      } catch (SQLException e) {
        // ignore
      }
    }
  }

  @Override
  public void initFromConfiguration(ConfigurationType config) throws SQLException {
    populateTables(config.getJdbc());

    String table = config.getTable();
    tableChooser.setSelectedItem(table);
    appendCheckbox.setSelected(config.isAppend() == null ? false : config.isAppend());
  }

  @Override
  public void addConfiguration(ConfigurationType config) {
    config.setTable((String) tableChooser.getSelectedItem());
    config.setAppend(appendCheckbox.isSelected());
  }
}
