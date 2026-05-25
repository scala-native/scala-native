<#
.SYNOPSIS
  Verify MSVC ARM64 link libraries are installed (no admin required).

.DESCRIPTION
  Checks for libcpmt.lib and legacy_stdio_definitions.lib under the latest
  MSVC version in Visual Studio 2022 installs (Build Tools, Enterprise, etc.)
  and C:\Program\VC layouts. Matches scala.scalanative.build.Discover.

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-msvc-arm64.ps1
#>
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$required = @('libcpmt.lib', 'legacy_stdio_definitions.lib')

function Get-MsvcLibSearchRoots {
    $roots = [System.Collections.Generic.List[string]]::new()
    $bases = @(
        ${env:ProgramFiles(x86)},
        $env:ProgramFiles
    ) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    $editions = @('BuildTools', 'Community', 'Professional', 'Enterprise')
    foreach ($year in @('2022', '2019')) {
        foreach ($edition in $editions) {
            foreach ($base in $bases) {
                $candidate = Join-Path $base "Microsoft Visual Studio\$year\$edition\VC\Tools\MSVC"
                if ((Test-Path -LiteralPath $candidate) -and ($roots -notcontains $candidate)) {
                    [void]$roots.Add($candidate)
                }
            }
        }
    }
    $programVc = 'C:\Program\VC\Tools\MSVC'
    if ((Test-Path -LiteralPath $programVc) -and ($roots -notcontains $programVc)) {
        [void]$roots.Add($programVc)
    }
    $vswhere = Join-Path ${env:ProgramFiles(x86)} 'Microsoft Visual Studio\Installer\vswhere.exe'
    if (Test-Path -LiteralPath $vswhere) {
        $installPaths = & $vswhere -latest -products * -property installationPath 2>$null
        foreach ($installPath in @($installPaths)) {
            if ([string]::IsNullOrWhiteSpace($installPath)) { continue }
            $candidate = Join-Path $installPath 'VC\Tools\MSVC'
            if ((Test-Path -LiteralPath $candidate) -and ($roots -notcontains $candidate)) {
                [void]$roots.Add($candidate)
            }
        }
    }
    $roots
}

foreach ($root in Get-MsvcLibSearchRoots) {
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
