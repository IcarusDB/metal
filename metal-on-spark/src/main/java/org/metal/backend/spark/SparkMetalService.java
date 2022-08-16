package org.metal.backend.spark;

import org.apache.arrow.vector.types.pojo.Schema;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.metal.dataset.spark.SchemaConvertorWithSpark;
import org.metal.exception.MetalServiceException;
import org.metal.service.BaseMetalService;
import org.metal.translator.Translator;
import org.metal.core.props.IMetalProps;

import java.util.NoSuchElementException;

public class SparkMetalService <P extends IMetalProps> extends BaseMetalService<Dataset<Row>, SparkSession, P> {
    protected SparkMetalService(Translator<Dataset<Row>, SparkSession> translator) {
        super(translator);
    }

    @Override
    public Schema schema(String id) throws MetalServiceException {
       try {
            StructType schema = df(id).schema();
            String timeZoneId = translator().platform().sessionState().conf().sessionLocalTimeZone();
            Schema target = new SchemaConvertorWithSpark().from(schema, timeZoneId);
            return target;
        } catch (Exception e) {
            throw new MetalServiceException(e);
        }
    }

    public static <P extends IMetalProps> SparkMetalService<P> of(SparkTranslator translator) {
        return new SparkMetalService<P>(translator);
    }
}
