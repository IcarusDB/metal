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

import org.apache.spark.sql.catalyst.parser.SqlBaseLexer;
import org.apache.spark.sql.catalyst.parser.SqlBaseParser;
import org.apache.spark.sql.catalyst.parser.SqlBaseParserBaseListener;
import org.apache.spark.sql.catalyst.parser.UpperCaseCharStream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SqlParserUtil {

    public static class Tables {

        private Set<String> primary;
        private Set<String> alias;

        private Tables() {
            primary = new HashSet<>();
            alias = new HashSet<>();
        }

        public Set<String> primary() {
            return primary;
        }

        public Set<String> alias() {
            return alias;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private Tables inner;

            private Builder() {
                inner = new Tables();
            }

            public Builder addPrimary(String table) {
                inner.primary.add(table);
                return this;
            }

            public Builder addAlias(String table) {
                inner.alias.add(table);
                return this;
            }

            public Tables build() {
                inner.alias = Collections.unmodifiableSet(inner.alias);
                inner.primary = Collections.unmodifiableSet(inner.primary);
                return inner;
            }
        }

        @Override
        public String toString() {
            return "Tables{" + "primary=" + primary + ", alias=" + alias + '}';
        }
    }

    public static Tables table(String sqlText) {
        SqlBaseLexer lexer =
                new SqlBaseLexer(new UpperCaseCharStream(CharStreams.fromString(sqlText)));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SqlBaseParser parser = new SqlBaseParser(tokenStream);
        parser.removeParseListeners();
        Tables.Builder builder = Tables.builder();
        parser.addParseListener(
                new SqlBaseParserBaseListener() {
                    @Override
                    public void exitTableName(SqlBaseParser.TableNameContext ctx) {
                        builder.addPrimary(ctx.getText());
                        super.exitTableName(ctx);
                    }
                    //
                    //            @Override
                    //            public void exitRelation(SqlBaseParser.RelationContext ctx) {
                    //                if (ctx.relationPrimary() != null &&
                    //                        ctx.relationPrimary() instanceof
                    // SqlBaseParser.TableNameContext) {
                    //                    SqlBaseParser.TableNameContext subCtx =
                    // (SqlBaseParser.TableNameContext)ctx.relationPrimary();
                    //                    System.out.println("HIT TABLE:" + subCtx.getText());
                    //                }
                    //                super.exitRelation(ctx);
                    //            }

                    @Override
                    public void exitTableAlias(SqlBaseParser.TableAliasContext ctx) {
                        SqlBaseParser.StrictIdentifierContext subCtx = ctx.strictIdentifier();
                        if (subCtx != null) {
                            builder.addAlias(subCtx.getText());
                        }
                        super.exitTableAlias(ctx);
                    }

                    //            @Override
                    //            public void exitIdentifier(SqlBaseParser.IdentifierContext ctx) {
                    //                ParserRuleContext parentCtx = ctx.getParent();
                    //                if (!(parentCtx instanceof
                    // SqlBaseParser.ErrorCapturingIdentifierContext)) {
                    //                    return;
                    //                }
                    //                parentCtx = parentCtx.getParent();
                    //                if (!(parentCtx instanceof
                    // SqlBaseParser.MultipartIdentifierContext)) {
                    //                    return;
                    //                }
                    //                parentCtx = parentCtx.getParent();
                    //                if (!(parentCtx instanceof
                    // SqlBaseParser.RelationPrimaryContext)) {
                    //                    return;
                    //                }
                    //                builder.addPrimary(ctx.getText());
                    //                super.exitIdentifier(ctx);
                    //            }
                });
        parser.statement();
        return builder.build();
    }

    public static boolean isQuery(String sqlText) {
        SqlBaseLexer lexer =
                new SqlBaseLexer(new UpperCaseCharStream(CharStreams.fromString(sqlText)));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SqlBaseParser parser = new SqlBaseParser(tokenStream);
        parser.removeParseListeners();
        final boolean[] isQuery = {false};

        parser.addParseListener(
                new SqlBaseParserBaseListener() {
                    @Override
                    public void enterQuery(SqlBaseParser.QueryContext ctx) {
                        if (ctx.getParent() instanceof SqlBaseParser.StatementContext) {
                            System.out.println("Hit");
                            isQuery[0] = true;
                        }
                        super.enterQuery(ctx);
                    }
                });

        parser.statement();
        return isQuery[0];
    }
}
