/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
