# Simple Chat

[![Build status](https://github.com/dzx912/simple-chat/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/dzx912/simple-chat/actions/workflows/build.yml)

Простой и легковесный чат

## Попробовать
### Запуск
С помощью [docker-compose](https://docs.docker.com/compose/install/)
```shell
docker-compose up
```
### Web клиент
[http://localhost:8080/](http://localhost:8080/)

## Обычный запуск
### Пререквизиты
Запустите [MongoDb](https://www.mongodb.com)

Пример запуска в [docker](https://www.docker.com/get-started)
```shell
docker container run --detach --publish 27017:27017 mongo
```

### Compile

```shell
gradle clean build
```

### Запуск

```shell
java -jar build/libs/simple-chat-1.0-SNAPSHOT.jar
```

### Web клиент
[http://localhost:8080/](http://localhost:8080/)