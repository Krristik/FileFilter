package filefilter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Сбор статистики для одного типа данных
 */

public class Statistics {
    private final DataType type;
    private long count = 0;

    // Для чисел
    private BigDecimal sum = BigDecimal.ZERO;
    private BigDecimal min = null;
    private BigDecimal max = null;

    // Для строк
    private int minLength = Integer.MAX_VALUE;
    private int maxLength = 0;
    private final List<String> samples = new ArrayList<>(3); // первые 3 строки для примера

    public Statistics(DataType type) {
        this.type = type;
    }

    /**
     * Обновляет статистику для целых чисел
     */

    public void addInteger(String value) {
        count++;
        BigDecimal num = new BigDecimal(value.trim());
        updateNumericStats(num);
    }

    /**
     * Обновляет статистику для вещественных чисел
     */

    public void addFloat(String value) {
        count++;
        BigDecimal num = new BigDecimal(value.trim());
        updateNumericStats(num);
    }

    /**
     * Обновляет статистику для строк
     */

    public void addString(String value) {
        count++;
        int length = value.length();

        if (length < minLength) minLength = length;
        if (length > maxLength) maxLength = length;

        if (samples.size() < 3) {
            samples.add(value);
        }
    }

    private void updateNumericStats(BigDecimal num) {
        sum = sum.add(num);
        if (min == null || num.compareTo(min) < 0) min = num;
        if (max == null || num.compareTo(max) > 0) max = num;
    }

    /**
     * Возвращает краткую статистику
     */

    public String getBrief() {
        return String.format("%s: %d элементов", type.name().toLowerCase(), count);
    }

    /**
     * Возвращает полную статистику
     */

    public String getFull() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s: %d элементов", type.name().toLowerCase(), count));

        if (type == DataType.INTEGER || type == DataType.FLOAT) {
            if (count > 0) {
                BigDecimal avg = sum.divide(BigDecimal.valueOf(count), 10, BigDecimal.ROUND_HALF_UP);
                sb.append(String.format("%n  min: %s%n  max: %s%n  sum: %s%n  avg: %s",
                        min.toPlainString(), max.toPlainString(), sum.toPlainString(), avg.toPlainString()));
            }
        } else if (type == DataType.STRING) {
            if (count > 0) {
                sb.append(String.format("%n  min length: %d%n  max length: %d", minLength, maxLength));
                if (!samples.isEmpty()) {
                    sb.append(String.format("%n  примеры: "));
                    for (int i = 0; i < samples.size(); i++) {
                        if (i > 0) sb.append(", ");
                        sb.append("\"").append(samples.get(i)).append("\"");
                    }
                }
            }
        }

        return sb.toString();
    }
}