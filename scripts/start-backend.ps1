# CrowdSense - Start Backend (Windows PowerShell)
$env:Path = "C:\Users\KiTE\tools\apache-maven-3.9.9\bin;$env:Path"
Set-Location "$PSScriptRoot\..\backend"
mvn spring-boot:run
