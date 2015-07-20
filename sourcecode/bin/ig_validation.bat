@echo off
REM # ----------------------------------------------------------------
REM # Start Script for the ANT BUILD
REM # ----------------------------------------------------------------

REM modify below to your setting
set JAVA_HOME=D:\Java\jdk1.6.0_17
set PROJECT_HOME=E:\Projects\M.Lottery\M.Lottery-TE\malawi-1.3.8

if exist "%JAVA_HOME%\bin\java.exe" goto okJava
echo "can NOT find %JAVA_HOME%\bin\java.exe"
goto end

:okJava
set CP=.;%PROJECT_HOME%\build\classes;%PROJECT_HOME%\lib\jakarta-commons\commons-logging.jar
%JAVA_HOME%/bin/java -cp %CP% com.mpos.lottery.te.gameimpl.instantgame.support.EGameValidationStrategy %*

:end