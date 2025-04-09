#!/bin/bash

# Остановить выполнение при ошибке
set -e

# Перейти в директорию проекта (если нужно)
cd "$(dirname "$0")"

echo "Clean ThesisCommon..."
./gradlew :ThesisCommon:clean

echo "Build ThesisCommon..."
./gradlew :ThesisCommon:build

echo "Clean ThesisBot..."
./gradlew :ThesisBot:clean

echo "Build ThesisBot..."
./gradlew :ThesisBot:build

echo "Clean ThesisGameClient..."
./gradlew :ThesisGameClient:clean

echo "Build ThesisGameClient..."
./gradlew :ThesisGameClient:build

echo "Clean ThesisGameServer..."
./gradlew :ThesisGameServer:clean

echo "Build ThesisGameServer..."
./gradlew :ThesisGameServer:build

echo "Build completed!"