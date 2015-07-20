@echo off
REM setLocal
REM if setLocal here, a exception will be thrown out:
REM     java.net.SocketException: Unrecognized Windows Sockets error: 10106: create
REM I don't know why!

REM set environment variables.
call setenv.bat
if %errorlevel% == "1" goto END

REM run performance test scripts
%JAVA_HOME%/bin/java -cp %CP% net.grinder.Grinder ./script/grinder.properties

:END
