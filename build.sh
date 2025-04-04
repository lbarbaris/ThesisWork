#!/bin/bash

# Остановить выполнение при ошибке
set -e

# Перейти в директорию проекта (если нужно)
cd "$(dirname "$0")"

echo "Очищение ThesisCommon..."
./gradlew :ThesisCommon:clean

echo "Сборка ThesisCommon..."
./gradlew :ThesisCommon:build

echo "Очищение ThesisBot..."
./gradlew :ThesisBot:clean

echo "Сборка ThesisBot..."
./gradlew :ThesisBot:build

echo "Очищение ThesisGameClient..."
./gradlew :ThesisGameClient:clean

echo "Сборка ThesisGameClient..."
./gradlew :ThesisGameClient:build

echo "Очищение ThesisGameServer..."
./gradlew :ThesisGameServer:clean

echo "Сборка ThesisGameServer..."
./gradlew :ThesisGameServer:build

echo "Сборка завершена!"