<#
.SYNOPSIS
  Verify MSVC ARM64 link libraries are installed (no admin required).

.DESCRIPTION
  Checks for libcpmt.lib and legacy_stdio_definitions.lib under the latest
  MSVC version in Build Tools and C:\Program\VC layouts.

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-msvc-arm64.ps1
#>
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$required = @('libcpmt.lib', 'legacy_stdio_definitions.lib')
$roots = @(
    'C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\VC\Tools\MSVC',
    'C:\Program\VC\Tools\MSVC'
)

foreach ($root in $roots) {
    if (-not (Test-Path -LiteralPath $root)) { continue }
    $versionDir = Get-ChildItem -LiteralPath $root -Directory |
        Sort-Object Name -Descending |
        Select-Object -First 1
    if (-not $versionDir) { continue }
    foreach ($sub in @('lib\arm64', 'lib\onecore\arm64')) {
        $libDir = Join-Path $versionDir.FullName $sub
        $ok = $true
        foreach ($lib in $required) {
            if (-not (Test-Path -LiteralPath (Join-Path $libDir $lib))) {
                $ok = $false
                break
            }
        }
        if ($ok) {
            Write-Host "READY: $libDir"
            exit 0
        }
    }
}

Write-Host 'NOT READY - run: powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\install-vs-arm64-msvc.ps1'
exit 1
