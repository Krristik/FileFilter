package filefilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Парсер аргументов командной строки
 * Поддерживаемые флаги:
 *   -o <path> - выходная директория (по умолчанию: текущая)
 *   -p <prefix> - префикс для выходных файлов (по умолчанию: пустой)
 *   -a - режим дозаписи, вместо перезаписи
 *   -s - выводить краткую статистику
 *   -f - выводить полную статистику
 *   остальные - пути к входным файлам
 */

public class CommandLineParser {
    private String outputDir = null;
    private String prefix = "";
    private boolean appendMode = false;
    private boolean showBriefStats = false;
    private boolean showFullStats = false;
    private final List<String> inputFiles = new ArrayList<>();

    public CommandLineParser(String[] args) {
        parse(args);
    }

    private void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-") && arg.length() == 2) {
                char flag = arg.charAt(1);
                switch (flag) {
                    case 'o' -> {
                        if (i + 1 >= args.length) {
                            throw new IllegalArgumentException("Флаг -o требует аргумент (путь к директории)");
                        }
                        outputDir = args[++i];
                    }
                    case 'p' -> {
                        if (i + 1 >= args.length) {
                            throw new IllegalArgumentException("Флаг -p требует аргумент (префикс)");
                        }
                        prefix = args[++i];
                    }
                    case 'a' -> appendMode = true;
                    case 's' -> showBriefStats = true;
                    case 'f' -> showFullStats = true;
                    default -> throw new IllegalArgumentException("Неизвестный флаг: " + arg);
                }
            } else {
                inputFiles.add(arg);
            }
        }

        if (inputFiles.isEmpty()) {
            throw new IllegalArgumentException("Не указаны входные файлы");
        }

        // Валидация: нельзя одновременно -s и -f
        if (showBriefStats && showFullStats) {
            throw new IllegalArgumentException("Нельзя использовать одновременно флаги -s и -f");
        }
    }

    public String getOutputDir() {
        return outputDir;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isAppendMode() {
        return appendMode;
    }

    public boolean isShowBriefStats() {
        return showBriefStats;
    }

    public boolean isShowFullStats() {
        return showFullStats;
    }

    public List<String> getInputFiles() {
        return inputFiles;
    }
}