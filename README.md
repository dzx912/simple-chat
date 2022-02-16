# Simple Chat

[![Build status](https://github.com/dzx912/simple-chat/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/dzx912/simple-chat/actions/workflows/gradle.yml)

Lightweight chat by Java Vert.x

## Compile

`gradle clean build`

## Prepare run

`docker container run --detach --publish 27017:27017 mongo`

## Run

`java -jar build/libs/simple-chat-fat-1.0-SNAPSHOT.jar`

## Client

[http://localhost:8080/](http://localhost:8080/)