@echo off

REM confirm JYTHON_HOME and JAVA_HOME is valid directory
set JAVA_HOME=D:\Java\jdk1.6.0_17
set CWD=%cd%
set JYTHON_HOME=%CWD%

REM check if java_home exist
if exist "%JAVA_HOME%\bin\keytool.exe" goto SETENV
echo "ERROR:can NOT find %JAVA_HOME%\bin\java.exe"

REM exit with a signal
exit /b 1

:SETENV
set PATH=%JAVA_HOME%\bin;%PATH%

REM set classpath
set PROJECT_HOME=%CWD%\..\..

set CP=.;%PROJECT_HOME%\build\classes;%JYTHON_HOME%\grinder-3.2.jar;%JYTHON_HOME%\grinder-j2se5.jar;
set CP=%CP%;%JYTHON_HOME%\jython.jar;%JYTHON_HOME%\picocontainer-1.3.jar;%PROJECT_HOME%\lib\jakarta-commons\commons-logging.jar

REM execute grinder agent
echo use JAVA_HOME=%JAVA_HOME%
echo use JYTHON_HOME=%JYTHON_HOME%
echo use PROJECT_HOME=%PROJECT_HOME%
echo CP=%CP%


