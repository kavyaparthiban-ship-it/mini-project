# CrowdSense - Load Database (Windows PowerShell)
$mysql = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
Set-Location "$PSScriptRoot\.."
if (-not (Test-Path $mysql)) {
    throw "MySQL client not found at $mysql"
}

foreach ($file in @("database\schema.sql", "database\data.sql", "database\coimbatore-data.sql")) {
    Get-Content $file -Raw | & $mysql -u root --port=3306
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to load $file (mysql exit code $LASTEXITCODE)."
    }
}

Write-Host "CrowdSense database and Coimbatore demo network loaded."
