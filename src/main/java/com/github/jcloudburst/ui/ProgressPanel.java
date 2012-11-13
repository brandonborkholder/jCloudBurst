package com.github.jcloudburst.ui;

import java.io.IOException;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class ProgressPanel extends ConfigStepPanel {
  public ProgressPanel() {
    super("Import");
  }

  @Override
  protected void flushConfigurationToUI() throws SQLException, IOException, IllegalStateException {
    // TODO Not implemented yet...
    throw new AssertionError("Not implemented yet...");
  }

  @Override
  protected void flushUIToConfiguration() throws SQLException, IOException, IllegalStateException {
    // TODO Not implemented yet...
    throw new AssertionError("Not implemented yet...");
  }
}
