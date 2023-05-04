package org.metal.backend.spark.extension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.spark.sql.catalyst.parser.SqlBaseLexer;
import org.apache.spark.sql.catalyst.parser.SqlBaseParser;
import org.apache.spark.sql.catalyst.parser.SqlBaseParserBaseListener;
import org.apache.spark.sql.catalyst.parser.UpperCaseCharStream;

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
      return "Tables{" +
          "primary=" + primary +
          ", alias=" + alias +
          '}';
    }
  }

  public static Tables table(String sqlText) {
    SqlBaseLexer lexer = new SqlBaseLexer(new UpperCaseCharStream(CharStreams.fromString(sqlText)));
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    SqlBaseParser parser = new SqlBaseParser(tokenStream);
    parser.removeParseListeners();
    Tables.Builder builder = Tables.builder();
    parser.addParseListener(new SqlBaseParserBaseListener() {
      @Override
      public void exitTableName(SqlBaseParser.TableNameContext ctx) {
        builder.addPrimary(ctx.getText());
        super.exitTableName(ctx);
      }
//
//            @Override
//            public void exitRelation(SqlBaseParser.RelationContext ctx) {
//                if (ctx.relationPrimary() != null &&
//                        ctx.relationPrimary() instanceof SqlBaseParser.TableNameContext) {
//                    SqlBaseParser.TableNameContext subCtx = (SqlBaseParser.TableNameContext)ctx.relationPrimary();
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
//                if (!(parentCtx instanceof SqlBaseParser.ErrorCapturingIdentifierContext)) {
//                    return;
//                }
//                parentCtx = parentCtx.getParent();
//                if (!(parentCtx instanceof SqlBaseParser.MultipartIdentifierContext)) {
//                    return;
//                }
//                parentCtx = parentCtx.getParent();
//                if (!(parentCtx instanceof SqlBaseParser.RelationPrimaryContext)) {
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
    SqlBaseLexer lexer = new SqlBaseLexer(new UpperCaseCharStream(CharStreams.fromString(sqlText)));
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    SqlBaseParser parser = new SqlBaseParser(tokenStream);
    parser.removeParseListeners();
    final boolean[] isQuery = {false};

    parser.addParseListener(new SqlBaseParserBaseListener() {
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
