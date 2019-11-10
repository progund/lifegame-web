#!/bin/bash

while(true)
do
    java -jar lib/winstone.jar --webroot=www --ajp13Port=8010 --httpPort=8081
done
