package io.resys.thena.docdb.sql.support;

public class SqlStatement {
  
  private final StringBuilder result = new StringBuilder();
  
  public SqlStatement append(String value) {
    result.append(value);
    return this;
  }
  public SqlStatement ln() {
    result.append("\n");
    return this;
  }
  public String toString() {
    return this.result.toString();
  }
  public String build() {
    return this.result.toString();
  }
  
  public static SqlStatement builder() {
    return new SqlStatement();
  } 
  
}
