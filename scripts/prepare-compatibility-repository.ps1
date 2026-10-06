param(
    [Parameter(Mandatory = $true)]
    [string]$Repository
)

$ErrorActionPreference = "Stop"
$rootDir = Split-Path -Parent $PSScriptRoot

if (-not [System.IO.Path]::IsPathRooted($Repository)) {
    $Repository = Join-Path $rootDir $Repository
}

if (Test-Path $Repository) {
    Remove-Item -Recurse -Force $Repository
}
New-Item -ItemType Directory -Force -Path $Repository | Out-Null

& (Join-Path $rootDir "gradlew.bat") `
    "-Dmaven.repo.local=$Repository" `
    :publishToMavenLocal `
    :codes-spring:publishToMavenLocal `
    --stacktrace
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$env:MAVEN_REPO_LOCAL = $Repository
sh (Join-Path $rootDir "scripts/verify-local-publications.sh")
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

python (Join-Path $rootDir "scripts/verify-published-pom-contracts.py") $Repository
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Output $Repository
