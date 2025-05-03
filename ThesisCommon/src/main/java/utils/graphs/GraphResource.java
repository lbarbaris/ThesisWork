package utils.graphs;

import utils.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GraphResource<T> {
    private String title;
    private final String xLabel;
    private final String yLabel;
    private final List<LinkedList<T>> data;
    private int counter;

    public GraphResource(String title, String xLabel, String yLabel) {
        this.counter = 0;
        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.data = new ArrayList<>();
    }

    public void addSeries() {
        data.add(new LinkedList<>());
    }

    public void subtractFromCell(int seriesIndex, int cellIndex, Long value) {
        if (seriesIndex < 0 || seriesIndex >= data.size()) {
            throw new IndexOutOfBoundsException("Серия с индексом " + seriesIndex + " не существует.");
        }
        LinkedList<Long> series = (LinkedList<Long>) data.get(seriesIndex);

        if (cellIndex < 0 || cellIndex >= series.size()) {
            throw new IndexOutOfBoundsException("Ячейка с индексом " + cellIndex + " не существует в серии " + seriesIndex);
        }

        //System.out.println(series.get(seriesIndex) - value);
        if (series.get(cellIndex) - value > 0){
            series.set(cellIndex, series.get(cellIndex) - value);
        }

    }

    public void addValue(int seriesIndex, T value) {
        if (seriesIndex == 1){
            counter++;
        }
        if (seriesIndex < 0 || seriesIndex >= data.size()) {
            throw new IndexOutOfBoundsException("Серия с индексом " + seriesIndex + " не существует.");
        }
        data.get(seriesIndex).add(value);
    }

    public int getDataSize(){
        return data.size();
    }

    public int getSeriesSize(int seriesIndex){
        return data.get(seriesIndex).size();
    }

    public LinkedList<T> getSeries(int seriesIndex) {
        if (seriesIndex < 0 || seriesIndex >= data.size()) {
            throw new IndexOutOfBoundsException("Серия с индексом " + seriesIndex + " не существует.");
        }
        return data.get(seriesIndex);
    }

    public void exportToTxt(String fileName) {
        try {
            // Определяем путь к файлу
            String basePath = Constants.GRAPH_PATH;
            String baseName = fileName;
            String extension = "";

            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex != -1) {
                baseName = fileName.substring(0, dotIndex);
                extension = fileName.substring(dotIndex);
            }

            File file = new File(basePath + fileName);
            int count = 1;
            // Если файл уже существует — ищем свободное имя
            while (file.exists()) {
                file = new File(basePath + baseName + "_" + count + extension);
                this.title = baseName + "_" + count;
                count++;
            }

            file.getParentFile().mkdirs(); // Создает директории, если их нет
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(title + "\n");
                writer.write(xLabel + "\n");
                writer.write(yLabel + "\n");

                for (LinkedList<T> series : data) {
                    for (T value : series) {
                        writer.write(value + " ");
                    }
                    writer.write("\n");
                }

                System.out.println("Данные успешно экспортированы в " + file.getAbsolutePath());

            }
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    public void extendSeries() {
        // Получаем первую и вторую серии
        LinkedList<T> firstSeries = data.get(0);
        LinkedList<T> secondSeries = data.get(1);


        // Добавляем числа в первую серию от inputNumber до endValue
        for (int i = firstSeries.size(); i <= 1200; i++) {
            firstSeries.add((T) Integer.valueOf(i));
            secondSeries.add((T) Integer.valueOf(0));
        }
    }

    public String getTitle() {
        return title;
    }
}
