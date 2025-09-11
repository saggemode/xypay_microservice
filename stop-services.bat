@echo off
echo 🛑 Stopping XyPay Microservices...

echo Stopping all Spring Boot services...
taskkill /f /im java.exe 2>nul

echo Stopping infrastructure containers...
docker stop postgres redis kafka zookeeper 2>nul

echo ✅ All services stopped!
echo 💡 To start again, run: start-services.bat
pause
