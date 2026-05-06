@echo off
title NRO SERVER
java -Xms4G -Xmx6G -Xss512k -XX:+UseZGC -jar dist/Michelin_Boy.jar
pause
