package filefilter;

import java.util.regex.Pattern;

/**
 * Классификатор строк: определяет тип данных (целое, вещественное, строка)
 */

public class DataClassifier {

    // Регулярные выражения для проверки типов
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");
    private static final Pattern FLOAT_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?$");

    /**
     * Классифицирует строку по её содержимому
     * @param line входная строка
     * @return тип данных
     */

    public static DataType classify(String line) {
        if (line == null || line.isEmpty()) {
            return DataType.STRING; // пустая строка → строка
        }

        String trimmed = line.trim();

        if (INTEGER_PATTERN.matcher(trimmed).matches()) {
            return DataType.INTEGER;
        }

        if (FLOAT_PATTERN.matcher(trimmed).matches()) {
            return DataType.FLOAT;
        }
        return DataType.STRING;
    }
}