package utils.graphs;

import utils.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GraphResource<T> {
    private final String title;
    private final String xLabel;
    private final String yLabel;
    private final List<LinkedList<T>> data;

    public GraphResource(String title, String xLabel, String yLabel) {
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

    private Number subtractNumbers(T oldValue, T value) {
        if (oldValue instanceof Integer) {
            return (Integer) oldValue - (Integer) value;
        } else if (oldValue instanceof Long) {
            return (Long) oldValue - (Long) value;
        } else if (oldValue instanceof Float) {
            return (Float) oldValue - (Float) value;
        } else if (oldValue instanceof Double) {
            return (Double) oldValue - (Double) value;
        }
        return (Number) oldValue;
    }

    public void addValue(int seriesIndex, T value) {
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
            File file = new File(Constants.GRAPH_PATH + fileName);
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

                System.out.println("Данные успешно экспортированы в " + Constants.GRAPH_PATH + fileName);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }
}
