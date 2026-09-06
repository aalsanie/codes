param(
    [Parameter(Mandatory = $false)]
    [string]$Repository = "build\compatibility-maven"
)

$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot
if (-not [System.IO.Path]::IsPathRooted($Repository)) {
    $Repository = Join-Path $Root $Repository
}
$Repository = [System.IO.Path]::GetFullPath($Repository)

if (Test-Path $Repository) {
    Remove-Item -Recurse -Force $Repository
}
New-Item -ItemType Directory -Force -Path $Repository | Out-Null

& (Join-Path $Root "gradlew.bat") `
    "-Dmaven.repo.local=$Repository" `
    :publishToMavenLocal `
    :codes-spring:publishToMavenLocal `
    :codes-grpc-java:publishToMavenLocal `
    --stacktrace

if ($LASTEXITCODE -ne 0) {
    throw "Compatibility publication failed with exit code $LASTEXITCODE"
}

$VersionLine = Get-Content (Join-Path $Root "gradle.properties") |
    Where-Object { $_ -like "VERSION_NAME=*" } |
    Select-Object -First 1
$Version = $VersionLine.Substring("VERSION_NAME=".Length)

foreach ($Artifact in @("codes", "codes-spring", "codes-grpc-java")) {
    $ArtifactDir = Join-Path $Repository "io\github\aalsanie\$Artifact\$Version"
    $Jar = Join-Path $ArtifactDir "$Artifact-$Version.jar"
    $Pom = Join-Path $ArtifactDir "$Artifact-$Version.pom"

    if (-not (Test-Path $Jar) -or (Get-Item $Jar).Length -eq 0) {
        throw "Missing compatibility publication JAR: $Jar"
    }
    if (-not (Test-Path $Pom) -or (Get-Item $Pom).Length -eq 0) {
        throw "Missing compatibility publication POM: $Pom"
    }
}

python (Join-Path $Root "scripts\verify-published-pom-contracts.py") $Repository
if ($LASTEXITCODE -ne 0) {
    throw "Published POM verification failed with exit code $LASTEXITCODE"
}

Write-Output $Repository
