@echo off
setLocal

REM set environment variables.
call setenv.bat
if %errorlevel% == "1" goto END

REM  display console interface.
%JAVA_HOME%/bin/java -cp %CP% -Duser.language="en" net.grinder.Console

:END 