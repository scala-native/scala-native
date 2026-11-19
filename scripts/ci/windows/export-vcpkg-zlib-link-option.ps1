# Export SCALANATIVE_LINKING_OPTIONS=-l<name> for vcpkg's static zlib in $LibDir
# to later GitHub Actions steps. The name varies by zlib version, see
# docs/user/setup.md. Lists $LibDir and fails when none is found.
param(
    [Parameter(Mandatory = $true)]
    [string]$LibDir
)

Set-StrictMode -Version 3.0
$ErrorActionPreference = "Stop"

foreach ($name in @("libzs", "zs", "zlib")) {
    if (Test-Path -LiteralPath (Join-Path $LibDir "$name.lib")) {
        $option = "SCALANATIVE_LINKING_OPTIONS=-l$name"
        Write-Host $option
        $option >> $env:GITHUB_ENV
        exit 0
    }
}

Write-Host "Contents of ${LibDir}:"
Get-ChildItem -LiteralPath $LibDir | ForEach-Object { Write-Host "  $($_.Name)" }
Write-Error "None of libzs.lib, zs.lib, zlib.lib in $LibDir"
