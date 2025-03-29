import matplotlib.pyplot as plt
import os

def plot_from_file(filename):
    with open(filename, 'r', encoding='utf-8') as file:
        lines = file.readlines()

    if len(lines) < 5:
        print(f"Недостаточно данных в файле {filename}")
        return

    title = lines[0].strip()
    xlabel = lines[1].strip()
    ylabel = lines[2].strip()
    x_values = list(map(float, lines[3].strip().split()))
    y_values = [list(map(float, line.strip().split())) for line in lines[4:]]

    plt.figure(figsize=(8, 5))
    for i, y in enumerate(y_values):
        plt.plot(x_values, y, marker='o', linestyle='-', label=f'График {i + 1}')

    plt.title(title)
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    plt.legend()
    plt.grid()
    plt.show()

def plot_from_directory(directory):
    if not os.path.exists(directory):
        print(f"Директория {directory} не найдена")
        return

    files = [f for f in os.listdir(directory) if os.path.isfile(os.path.join(directory, f))]
    if not files:
        print(f"В директории {directory} нет файлов")
        return

    for file in files:
        file_path = os.path.join(directory, file)
        print(f"Обрабатывается файл: {file}")
        plot_from_file(file_path)

# Использование
directory = "resources"  # Укажите путь к вашей папке
plot_from_directory(directory)
