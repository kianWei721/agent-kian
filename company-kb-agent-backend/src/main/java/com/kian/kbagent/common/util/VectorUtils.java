package com.kian.kbagent.common.util;

import java.util.List;
import java.util.stream.Collectors;

public final class VectorUtils {

    private VectorUtils() {
    }

    public static String toPgVectorLiteral(List<Double> vector) {
        return "[" + vector.stream()
                .map(value -> String.format(java.util.Locale.US, "%.12f", value))
                .collect(Collectors.joining(",")) + "]";
    }
}
