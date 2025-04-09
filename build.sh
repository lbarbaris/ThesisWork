#!/bin/bash

# Остановить выполнение при ошибке
set -e

# Перейти в директорию проекта
cd "$(dirname "$0")"

# Функция для принудительной сборки модуля
force_build_module() {
    local module=$1
    echo "Force clean $module..."
    ./gradlew ":${module}:clean" --no-daemon

    echo "Force build $module..."
    ./gradlew ":${module}:build" --no-daemon --rerun-tasks --refresh-dependencies
}

# Принудительная сборка всех модулей
force_build_module "ThesisCommon"
force_build_module "ThesisBot"
force_build_module "ThesisGameClient"
force_build_module "ThesisGameServer"

echo "All components rebuilt successfully!"