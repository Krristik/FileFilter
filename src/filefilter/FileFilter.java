package filefilter;

import java.io.IOException;

/**
 * Главный класс утилиты фильтрации файлов
 * Точка входа: метод main()
 */

public class FileFilter {

    public static void main(String[] args) {
        // Парсинг аргументов
        CommandLineParser parser;
        try {
            parser = new CommandLineParser(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            printUsage();
            System.exit(1);
            return;
        }

        // Создание движка фильтрации
        try (FilterEngine engine = new FilterEngine(
                parser.getOutputDir(),
                parser.getPrefix(),
                parser.isAppendMode()
        )) {
            // Обработка всех входных файлов
            for (String inputFile : parser.getInputFiles()) {
                try {
                    engine.processFile(inputFile);
                } catch (IOException e) {
                    System.err.println("Ошибка обработки файла " + inputFile + ": " + e.getMessage());
                    // Продолжаем обработку остальных файлов
                }
            }

            // Вывод статистики (если запрошено)
            if (parser.isShowBriefStats()) {
                System.out.println("\n=== КРАТКАЯ СТАТИСТИКА ===");
                for (DataType type : DataType.values()) {
                    System.out.println(engine.getStatistics(type).getBrief());
                }
            } else if (parser.isShowFullStats()) {
                System.out.println("\n=== ПОЛНАЯ СТАТИСТИКА ===");
                for (DataType type : DataType.values()) {
                    System.out.println(engine.getStatistics(type).getFull());
                    System.out.println(); // пустая строка между типами
                }
            }

            System.out.println("Обработка завершена");

        } catch (IOException e) {
            System.err.println("Ошибка записи выходных файлов: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("""
                Использование:
                  java -cp <classpath> filefilter.FileFilter [опции] файл1 [файл2 ...]
                
                Опции:
                *   -o <path> - выходная директория (по умолчанию: текущая)
                *   -p <prefix> - префикс для выходных файлов (по умолчанию: пустой)
                *   -a - режим дозаписи, вместо перезаписи
                *   -s - выводить краткую статистику
                *   -f - выводить полную статистику
                
                Примеры:
                  java -cp out filefilter.FileFilter -s data1.txt data2.txt
                  java -cp out filefilter.FileFilter -o out -p sample_ -f *.txt
                """);
    }
}