#!/bin/bash

mkdir -p resource/log

echo '----------------'
echo 'MAVEN BEGIN WORK'
echo '----------------'

mvn clean package

echo '-----------'
echo 'BOT STARTED'
echo '-----------'

java -jar target/LentaBot-1.jar