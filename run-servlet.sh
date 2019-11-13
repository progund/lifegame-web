#!/bin/bash

LOG_DIR="/tmp/lifegame-web"
ERR_LOG="${LOG_DIR}/lifegame-err.log"
INFO_LOG="${LOG_DIR}/lifegame.log"

mkdir -p "${LOG_DIR}"

while(true)
do
    echo "lifegame:$0 $(date) | starting server" >> "${INFO_LOG}"
    java -jar lib/winstone.jar --webroot=www --ajp13Port=8010 --httpPort=8081 >> "${INFO_LOG}" 2>> "${ERR_LOG}"
done
