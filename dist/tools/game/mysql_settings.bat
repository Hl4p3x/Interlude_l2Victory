@echo off

set PATH=C:\Program Files\MariaDB 10.2\bin\

set USER=root
set PASS=root
set DBNAME=j2dev_it_game
set DBHOST=localhost

mysql -h %DBHOST% -u %USER% --password=%PASS% -Bse "use %DBNAME%" > nul 2>&1

if errorlevel 9009 goto notfound
if errorlevel 1 goto error
goto end

:error
echo Can't use %DBNAME%!
exit /b 1

:notfound
echo Can't find mysql binary!
exit /b 1

:END