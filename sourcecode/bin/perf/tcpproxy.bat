@echo off
REM setLocal

REM set environment variables.
call setenv.bat
if %errorlevel% == "1" goto END

REM  display console interface.
%JAVA_HOME%/bin/java -cp %CP% net.grinder.TCPProxy -localhost 10.40.0.10 -localport 2010 -remotehost 10.40.0.10 -remoteport 8080

:END 