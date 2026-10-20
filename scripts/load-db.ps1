# CrowdSense - Load Database (Windows PowerShell)
$mysql = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
Set-Location "$PSScriptRoot\.."
Get-Content database\schema.sql -Raw | & $mysql -u root --port=3306
Get-Content database\data.sql   -Raw | & $mysql -u root --port=3306
Write-Host "Database loaded."
