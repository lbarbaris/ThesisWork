#!/bin/bash

set -e
cd "$(dirname "$0")"

COMMON_JAR="ThesisCommon/build/libs/ThesisCommon-1.0-SNAPSHOT.jar"
SERVER_JAR="ThesisGameServer/build/libs/ThesisGameServer-1.0-SNAPSHOT.jar"
CLIENT_JAR="ThesisGameClient/build/libs/ThesisGameClient-1.0-SNAPSHOT.jar"
BOT_JAR="ThesisBot/build/libs/ThesisBot-1.0-SNAPSHOT.jar"

echo "Запуск сервера..."
java -cp "$SERVER_JAR:$COMMON_JAR" server.SimpleGameServer &

sleep 3

echo "Запуск клиента..."
java -cp "$CLIENT_JAR:$COMMON_JAR" Main &

sleep 3

echo "Запуск бота..."
java -cp "$BOT_JAR:$COMMON_JAR" Main &

echo "Все компоненты запущены!"