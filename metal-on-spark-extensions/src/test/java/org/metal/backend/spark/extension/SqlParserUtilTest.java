package org.metal.backend.spark.extension;

import org.junit.Test;

public class SqlParserUtilTest {

  @Test
  public void case0() {
    String sql = "SELECT * FROM T1 INNER JOIN (SELECT * FROM T2 WHERE ID = 1) AS T3 ON T3.ID = T1.ID";
    System.out.println(SqlParserUtil.table(sql));
  }

  @Test
  public void case1() {
    String query = "SELECT * FROM T1 INNER JOIN (SELECT * FROM T2 WHERE ID = 1) AS T3 ON T3.ID = T1.ID";
    System.out.println(SqlParserUtil.isQuery(query));

    String create = "CREATE TABLE T1(ID STRING)";
    System.out.println(SqlParserUtil.isQuery(create));
  }
}
