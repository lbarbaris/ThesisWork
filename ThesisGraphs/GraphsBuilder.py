from collections import Counter

import matplotlib.pyplot as plt
import os
import numpy as np  # Для удобных мат. операций

import matplotlib.pyplot as plt
import os
import numpy as np  # Для удобных мат. операций

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

    print("y_values:", ' '.join(format_number_smart(val) for val in y))

    # Подсчет точек
    counter = Counter(y)
    sorted_items = sorted(counter.items())

    values_str = ' '.join(format_number_smart(val) for val, _ in sorted_items)
    counts_str = ' '.join(str(count) for _, count in sorted_items)

    print(f"Значения: {values_str}")
    print(f"Количество: {counts_str}")

    mean_all = np.mean(y)
    std_all = np.std(y)
    cv_all = (std_all / mean_all) * 100 if mean_all != 0 else None

    y_wo_first = y[1:]
    if len(y_wo_first) > 0:
        mean_wo = np.mean(y_wo_first)
        std_wo = np.std(y_wo_first)
        cv_wo = (std_wo / mean_wo) * 100 if mean_wo != 0 else None
    else:
        mean_wo = None
        cv_wo = None

    # Вывод в требуемом формате
    if mean_all is not None and cv_all is not None:
        out = f"{mean_all:.4f}".replace('.', ',') + f" {cv_all:.2f}%".replace('.', ',')
    else:
        out = "0.0000 N/A"

#    if mean_wo is not None and cv_wo is not None:
#        out += f" {mean_wo:.4f} {cv_wo:.2f}%"
#    else:
#        out += " 0.0000 N/A"

    print(f"{title} (График {i + 1}) — {out}")


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
        if file.endswith(".DS_Store"):
            continue  # Пропускаем системный файл
        file_path = os.path.join(directory, file)
        print(f"\nОбрабатывается файл: {file}")
        plot_from_file(file_path)

def format_number_smart(num, decimals=4):
    s = f"{num:.{decimals}f}".replace('.', ',')
    # Убираем лишние нули и запятую, если нужно
    s = s.rstrip('0').rstrip(',') if ',' in s else s
    return s

# Использование
directory = "resources"  # Укажите путь к вашей папке
plot_from_directory(directory)