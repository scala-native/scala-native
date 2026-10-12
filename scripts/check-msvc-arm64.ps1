<#
.SYNOPSIS
  Verify MSVC ARM64 link libraries are installed (no admin required).

.DESCRIPTION
  Checks for libcpmt.lib and legacy_stdio_definitions.lib under Visual Studio 2022
  installs (Build Tools, Enterprise, etc.) and C:\Program\VC layouts.
  Matches scala.scalanative.build.Discover (all MSVC versions, not only the latest).

.PARAMETER Diagnostic
  On failure, print search roots and any partial matches.

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-msvc-arm64.ps1
#>
[CmdletBinding()]
param([switch]$Diagnostic)

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

function Test-LibDirReady {
    param([string]$LibDir)
    foreach ($lib in $required) {
        if (-not (Test-Path -LiteralPath (Join-Path $LibDir $lib))) {
            return $false
        }
    }
    return $true
}

function Find-ReadyArm64LibDir {
    $libSubdirs = @(
        'lib\arm64',
        'lib\ARM64',
        'lib\onecore\arm64',
        'lib\onecore\ARM64',
        'lib\spectre\arm64',
        'lib\spectre\ARM64'
    )
    foreach ($root in Get-MsvcLibSearchRoots) {
        if (-not (Test-Path -LiteralPath $root)) { continue }
        foreach ($versionDir in (Get-ChildItem -LiteralPath $root -Directory |
                Sort-Object Name -Descending)) {
            foreach ($sub in $libSubdirs) {
                $libDir = Join-Path $versionDir.FullName $sub
                if (Test-LibDirReady -LibDir $libDir) {
                    return $libDir
                }
            }
        }
        # Fallback: any libcpmt.lib under this MSVC root (WoA layouts vary).
        foreach ($libcpmt in (Get-ChildItem -LiteralPath $root -Recurse -Filter 'libcpmt.lib' -File -ErrorAction SilentlyContinue)) {
            $libDir = $libcpmt.DirectoryName
            if ((Test-LibDirReady -LibDir $libDir) -and
                ($libDir -match '\\lib(\\|\\\\)(onecore\\|spectre\\)?(arm64|ARM64)(\\|$)')) {
                return $libDir
            }
        }
    }
    $null
}

$ready = Find-ReadyArm64LibDir
if ($ready) {
    Write-Host "READY: $ready"
    exit 0
}

if ($Diagnostic) {
    Write-Host 'MSVC ARM64 link libraries not found. Diagnostics:'
    Write-Host "Required: $($required -join ', ')"
    foreach ($root in Get-MsvcLibSearchRoots) {
        Write-Host "Root: $root (exists=$(Test-Path -LiteralPath $root))"
        if (-not (Test-Path -LiteralPath $root)) { continue }
        Get-ChildItem -LiteralPath $root -Recurse -Filter 'libcpmt.lib' -File -ErrorAction SilentlyContinue |
            Select-Object -First 8 |
            ForEach-Object {
                $dir = $_.DirectoryName
                $missing = @($required | Where-Object {
                        -not (Test-Path -LiteralPath (Join-Path $dir $_))
                    })
                Write-Host "  libcpmt at $dir (missing: $($missing -join ', '))"
            }
    }
}

Write-Host 'NOT READY - run: powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\install-vs-arm64-msvc.ps1'
exit 1
