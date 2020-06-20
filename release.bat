@echo off
call mvnw clean package -DskipTests=True
timeout /t 30
