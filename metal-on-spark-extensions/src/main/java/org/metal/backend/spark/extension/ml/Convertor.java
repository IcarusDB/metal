package org.metal.backend.spark.extension.ml;

public class Convertor {
    public static double[] flat2DArray(Double[][] v) {
        double[] values = new double[v.length * v[0].length];
        for(int row = 0, start = 0; row < v.length; row++, start+= v[0].length) {
            for (int col = 0; col < v[0].length; col++) {
                values[start + col] = v[row][col];
            }
        }
        return values;
    }

    public static double[] convert2double(Double[] v) {
        double[] values = new double[v.length];
        for(int idx = 0; idx < v.length; idx++) {
            values[idx] = v[idx];
        }
        return values;
    }
}
