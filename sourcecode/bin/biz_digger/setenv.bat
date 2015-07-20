@echo off

REM confirm JYTHON_HOME and JAVA_HOME is valid directory
set JAVA_HOME=d:\project\Java\jdk1.6.0_17
set JYTHON_HOME=d:\project\jython2.5.1

REM check if java_home exist
if exist "%JAVA_HOME%\bin\keytool.exe" goto SETENV
echo "ERROR:can NOT find %JAVA_HOME%\bin\java.exe"

REM check if jython_home exist
if exist "%JYTHON_HOME%\jython.bat" goto SETENV
echo "ERROR:can NOT find %JYTHON_HOME%\jython.bat"

REM exit with a signal
exit /b 1

:SETENV
set PATH=%JAVA_HOME%\bin;%JYTHON_HOME%;%PATH%

REM set classpath
set PROJECT_HOME=%cd%\..\..

set ClassPath=.;%PROJECT_HOME%\build\classes;%PROJECT_HOME%\lib\oracle\ojdbc14.jar

REM execute grinder agent
echo use JAVA_HOME=%JAVA_HOME%
echo use JYTHON_HOME=%JYTHON_HOME%
echo use PROJECT_HOME=%PROJECT_HOME%
echo CP=%ClassPath%


