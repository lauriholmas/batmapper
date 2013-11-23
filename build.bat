call mvn clean install
echo y|copy target\uber-batUtils-1.2.jar ..\..\batclient\plugins
cd C:\Users\lauri\Documents\batclient\
call debug.bat
cd c:\users\lauri\workspace\batUtils
pause

