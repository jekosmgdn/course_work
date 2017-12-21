#!/bin/bash

echo '------------'
echo 'MONGODB INIT'
echo '------------'

mkdir -p resource/db

mongod --dbpath $PWD/resource/db