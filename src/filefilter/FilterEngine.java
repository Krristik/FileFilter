package filefilter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.EnumMap;
import java.util.Map;

/**
 * Основной движок фильтрации: чтение входных файлов -> классификация -> запись в выходные файлы
 * Файлы создаются лениво
 */

public class FilterEngine implements AutoCloseable {
    private final Path outputDir;
    private final String prefix;
    private final boolean appendMode;
    private final Map<DataType, Statistics> statistics = new EnumMap<>(DataType.class);
    private final Map<DataType, BufferedWriter> writers = new EnumMap<>(DataType.class);

    // Имена выходных файлов по умолчанию
    private static final String INT_FILE = "integers.txt";
    private static final String FLOAT_FILE = "floats.txt";
    private static final String STRING_FILE = "strings.txt";

    public FilterEngine(String outputPath, String prefix, boolean appendMode) {
        this.outputDir = outputPath == null || outputPath.isEmpty()
                ? Paths.get(".")
                : Paths.get(outputPath);
        this.prefix = prefix == null ? "" : prefix;
        this.appendMode = appendMode;

        // Инициализация статистики
        for (DataType type : DataType.values()) {
            statistics.put(type, new Statistics(type));
        }
    }

    /**
     * Обрабатывает один входной файл.
     * @param inputPath путь к входному файлу
     * @throws IOException при критических ошибках чтения
     */

    public void processFile(String inputPath) throws IOException {
        Path input = Paths.get(inputPath);
        if (!Files.exists(input)) {
            System.err.println("Файл не найден: " + inputPath + " - пропускаем");
            return;
        }
        if (!Files.isReadable(input)) {
            System.err.println("Нет прав на чтение: " + inputPath + " - пропускаем");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    processLine(line);
                } catch (Exception e) {
                    System.err.println(String.format(
                            "Ошибка обработки строки %d в файле %s: %s - пропускаем",
                            lineNumber, inputPath, e.getMessage()));
                }
            }
        }
    }

    private void processLine(String line) throws IOException {
        if (line == null) return;

        DataType type = DataClassifier.classify(line);
        Statistics stats = statistics.get(type);

        // Ленивая инициализация выходного файла
        if (!writers.containsKey(type)) {
            openWriterForType(type);
        }

        // Запись в файл + обновление статистики
        BufferedWriter writer = writers.get(type);
        writer.write(line);
        writer.newLine();

        switch (type) {
            case INTEGER -> stats.addInteger(line);
            case FLOAT -> stats.addFloat(line);
            case STRING -> stats.addString(line);
        }
    }

    private void openWriterForType(DataType type) throws IOException {
        // Создаём выходную директорию, если её нет
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        String fileName = switch (type) {
            case INTEGER -> prefix + INT_FILE;
            case FLOAT -> prefix + FLOAT_FILE;
            case STRING -> prefix + STRING_FILE;
        };

        Path outputPath = outputDir.resolve(fileName);
        OpenOption[] options = appendMode
                ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

        BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8, options);
        writers.put(type, writer);
    }

    /**
     * Возвращает статистику для указанного типа.
     */

    public Statistics getStatistics(DataType type) {
        return statistics.get(type);
    }

    /**
     * Закрывает все открытые выходные файлы.
     * Вызывается автоматически при использовании try-with-resources.
     */

    @Override
    public void close() throws IOException {
        IOException firstException = null;
        for (Map.Entry<DataType, BufferedWriter> entry : writers.entrySet()) {
            try {
                entry.getValue().close();
            } catch (IOException e) {
                if (firstException == null) firstException = e;
            }
        }
        if (firstException != null) throw firstException;
    }
}