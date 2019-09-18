@echo off
@color 0B
title Create account
set /p user="Enter account name: "
set /p pass="Enter account pass: "
java -Dfile.encoding=UTF-8 -Xms1024m -Xmx1024m -cp config;../lib/* ru.j2dev.authserver.AccountManager -c %user% %pass%
pause
exit
