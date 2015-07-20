@echo off
REM # ----------------------------------------------------------------
REM # Start Script for the ANT BUILD
REM # ----------------------------------------------------------------

REM modify below to your setting
set ANT_HOME=D:\project\apache-ant-1.7.1
set JAVA_HOME=D:\project\Java\jdk1.6.0_17

REM DO NOT modify below statements
if exist "%ANT_HOME%\bin\ant.bat" goto okAnt
echo "can NOT find %ANT_HOME%\bin\ant.bat"
goto end

:okAnt
if exist "%JAVA_HOME%\bin\java.exe" goto okJava
echo "can NOT find %JAVA_HOME%\bin\java.exe"
goto end

:okJava
set PATH=%JAVA_HOME%/bin;%PATH%
echo ************************************
echo USE ANT_HOME:%ANT_HOME%
echo USE JAVA_HOME:%JAVA_HOME%
echo ************************************
REM call ant script
%ANT_HOME%/bin/ant.bat %*

:end