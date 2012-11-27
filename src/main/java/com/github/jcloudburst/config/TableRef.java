package com.github.jcloudburst.config;

public class TableRef {
  public static char TABLE_SEP = '.';

  public final String catalog;
  public final String schema;
  public final String name;

  public TableRef(String name) {
    int nameIndex = name.lastIndexOf(TABLE_SEP);
    if (nameIndex >= 0) {
      this.name = name.substring(nameIndex + 1);
      int schemaIndex = name.lastIndexOf(TABLE_SEP, nameIndex - 1);

      if (schemaIndex >= 0) {
        this.schema = name.substring(schemaIndex + 1, nameIndex);
        this.catalog = name.substring(0, schemaIndex);
      } else {
        this.schema = name.substring(0, nameIndex);
        this.catalog = null;
      }
    } else {
      this.name = name;
      this.schema = null;
      this.catalog = null;
    }
  }

  public TableRef(String catalog, String schema, String name) {
    this.catalog = catalog;
    this.schema = schema;
    this.name = name;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    if (catalog != null) {
      b.append(catalog);
      b.append(TABLE_SEP);
    }

    if (schema != null) {
      b.append(schema);
      b.append(TABLE_SEP);
    }

    b.append(name);
    return b.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((catalog == null) ? 0 : catalog.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((schema == null) ? 0 : schema.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TableRef)) {
      return false;
    }

    TableRef other = (TableRef) obj;
    if (catalog == null) {
      if (other.catalog != null) {
        return false;
      }
    } else if (!catalog.equals(other.catalog)) {
      return false;
    }

    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }

    if (schema == null) {
      if (other.schema != null) {
        return false;
      }
    } else if (!schema.equals(other.schema)) {
      return false;
    }

    return true;
  }
}