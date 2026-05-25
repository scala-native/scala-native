<#
.SYNOPSIS
  Set Scala Native Windows environment variables for the current shell.

.DESCRIPTION
  Configures PATH and variables used by Scala Native on Windows (x64 and ARM64).
  Does not install tools; run scripts/setup-windows.ps1 first if needed.

  Session (dot-source so variables apply in the current shell):
    Set-ExecutionPolicy -Scope Process Bypass
    . .\scripts\env-windows.ps1

  Persist for your user account (registry only; open a new terminal for PATH):
    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\env-windows.ps1 -Persist User

.EXAMPLE
  cd C:\scala-native
  Set-ExecutionPolicy -Scope Process Bypass
  . .\scripts\env-windows.ps1
  sbt sandbox3/run
#>
[CmdletBinding()]
param(
    [ValidateSet('None', 'User', 'Machine')]
    [string] $Persist = 'None',

    [string] $VcpkgRoot = 'C:\vcpkg',
    [string] $VcpkgTriplet = '',
    [string] $LlvmBin = 'C:\Program Files\LLVM\bin',
    [string] $SbtBin = "${env:ProgramFiles(x86)}\sbt\bin",
    [string] $GitCmd = "${env:ProgramFiles}\Git\cmd"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Find-JavaHome {
    if ($env:JAVA_HOME -and (Test-Path -LiteralPath $env:JAVA_HOME)) {
        return $env:JAVA_HOME
    }
    $candidates = @(
        (Get-ChildItem 'C:\Program Files\Microsoft' -Filter 'jdk-25*' -Directory -ErrorAction SilentlyContinue),
        (Get-ChildItem 'C:\Program Files\Microsoft' -Filter 'jdk-*' -Directory -ErrorAction SilentlyContinue),
        (Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Filter 'jdk-*' -Directory -ErrorAction SilentlyContinue)
    ) | ForEach-Object { $_ } | Sort-Object Name -Descending
    $found = $candidates | Select-Object -First 1
    if ($found) { return $found.FullName }
    throw "JAVA_HOME not set and no JDK found under C:\Program Files\Microsoft"
}

function Add-PathEntry {
    param([string] $Directory)
    if ([string]::IsNullOrWhiteSpace($Directory)) { return }
    if (-not (Test-Path -LiteralPath $Directory)) {
        Write-Warning "PATH entry does not exist (skipped): $Directory"
        return
    }
    $normalized = $Directory.TrimEnd('\')
    if ($script:PathEntries -notcontains $normalized) {
        $script:PathEntries += $normalized
    }
}

function Set-EnvVar {
    param([string] $Name, [string] $Value)
    Set-Item -Path "Env:$Name" -Value $Value
    if ($Persist -ne 'None') {
        [Environment]::SetEnvironmentVariable($Name, $Value, $Persist)
    }
}

function Test-WindowsArm64 {
    # Avoid RuntimeInformation.OSArchitecture (missing on some Windows PowerShell hosts).
    return $env:PROCESSOR_ARCHITECTURE -eq 'ARM64'
}

function Get-DefaultVcpkgTriplet {
    if (Test-WindowsArm64) { return 'arm64-windows-static' }
    return 'x64-windows-static'
}

function Get-DefaultVcpkgHostTriplet {
    if (Test-WindowsArm64) { return 'arm64-windows' }
    return 'x64-windows'
}

function Install-VcpkgZlibAlias {
    param([string] $LibDir)
    $zlib = Join-Path $LibDir 'zlib.lib'
    $zs = Join-Path $LibDir 'zs.lib'
    if (-not (Test-Path -LiteralPath $zlib) -and (Test-Path -LiteralPath $zs)) {
        Copy-Item -LiteralPath $zs -Destination $zlib
        Write-Host "Installed zlib.lib (copy of zs.lib) for @link(zlib)"
    }
}

function Find-SbtBin {
    param([string] $Default)
    foreach ($dir in @(
            "${env:ProgramFiles(x86)}\sbt\bin",
            "${env:ProgramFiles}\sbt\bin",
            $Default
        )) {
        if ($dir -and (Test-Path -LiteralPath (Join-Path $dir 'sbt.bat'))) {
            return $dir
        }
    }
    if ($Default -and (Test-Path -LiteralPath $Default)) { return $Default }
    Write-Warning "sbt not found; install with: winget install sbt.sbt"
    return $Default
}

function Update-SessionPathFromRegistry {
  param([string[]] $Prepend)
  $machine = [Environment]::GetEnvironmentVariable('Path', 'Machine')
  $user = [Environment]::GetEnvironmentVariable('Path', 'User')
  $base = @()
  if ($machine) { $base += $machine }
  if ($user) { $base += $user }
  $merged = ($Prepend + $base) -join ';'
  $seen = [System.Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
  $deduped = foreach ($part in $merged.Split(';')) {
    $p = $part.Trim()
    if ($p -and $seen.Add($p)) { $p }
  }
  Set-Item -Path 'Env:Path' -Value ($deduped -join ';')
}

if ([string]::IsNullOrWhiteSpace($VcpkgTriplet)) {
    $VcpkgTriplet = Get-DefaultVcpkgTriplet
}
$vcpkgHostTriplet = Get-DefaultVcpkgHostTriplet

$vcpkgLibs = Join-Path $VcpkgRoot "installed\$VcpkgTriplet"
$vcpkgInclude = Join-Path $vcpkgLibs 'include'
$vcpkgLib = Join-Path $vcpkgLibs 'lib'
Install-VcpkgZlibAlias -LibDir $vcpkgLib

$javaHome = Find-JavaHome
$sbtBinResolved = Find-SbtBin -Default $SbtBin
$script:PathEntries = @()

# Order: Java and build tools first, then LLVM, Git, vcpkg CLI
Add-PathEntry (Join-Path $javaHome 'bin')
Add-PathEntry $LlvmBin
Add-PathEntry $sbtBinResolved
Add-PathEntry $GitCmd
Add-PathEntry $VcpkgRoot

if ($Persist -ne 'None') {
    $userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
    $prepend = $script:PathEntries | Where-Object {
        -not $userPath -or $userPath -notlike "*$_*"
    }
    if ($prepend) {
        $userMerged = if ($userPath) { ($prepend -join ';') + ';' + $userPath } else { $prepend -join ';' }
        [Environment]::SetEnvironmentVariable('Path', $userMerged, $Persist)
    }
}

# Apply PATH in this process (prepend toolchain dirs ahead of Machine + User PATH).
Update-SessionPathFromRegistry -Prepend $script:PathEntries

$envVars = @{
    JAVA_HOME                    = $javaHome
    VCPKG_INSTALLATION_ROOT      = $VcpkgRoot
    VCPKG_DEFAULT_HOST_TRIPLET   = $vcpkgHostTriplet
    LLVM_BIN                     = $LlvmBin
    SCALANATIVE_INCLUDE_DIRS     = $vcpkgInclude
    SCALANATIVE_LIB_DIRS         = $vcpkgLib
    SCALANATIVE_MODE             = 'debug'
    SCALANATIVE_GC               = 'immix'
    SCALANATIVE_LTO              = 'none'
    SCALANATIVE_OPTIMIZE         = 'true'
    ENABLE_EXPERIMENTAL_COMPILER = 'false'
}

foreach ($entry in $envVars.GetEnumerator()) {
    Set-EnvVar -Name $entry.Key -Value $entry.Value
}

if (Test-WindowsArm64) {
    $arm64Libcpmt = $null
    foreach ($msvcRoot in @(
            "${env:ProgramFiles(x86)}\Microsoft Visual Studio\2022\BuildTools\VC\Tools\MSVC",
            'C:\Program\VC\Tools\MSVC'
        )) {
        if (-not (Test-Path -LiteralPath $msvcRoot)) { continue }
        $arm64Libcpmt = Get-ChildItem -LiteralPath $msvcRoot -Directory -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending |
            ForEach-Object {
                foreach ($subdir in @('lib\arm64', 'lib\onecore\arm64')) {
                    Join-Path $_.FullName "$subdir\libcpmt.lib"
                }
            } |
            Where-Object { Test-Path -LiteralPath $_ } |
            Select-Object -First 1
        if ($arm64Libcpmt) { break }
    }
    if (-not $arm64Libcpmt) {
        $emu = if ($env:PROCESSOR_ARCHITECTURE -eq 'AMD64') {
            "`nNote: This shell is x64-emulated; the installer script relaunches as ARM64-native PowerShell."
        } else { '' }
        Write-Warning @"
MSVC ARM64 link libraries are not installed (required for aarch64-pc-windows-msvc).
From repo root (elevated): powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\install-vs-arm64-msvc.ps1$emu
"@
    }
}

$zlibHeader = Join-Path $vcpkgInclude 'zlib.h'
$zlibLib = Join-Path $vcpkgLib 'zlib.lib'
if (-not (Test-Path -LiteralPath $zlibHeader)) {
    Write-Warning @"
Missing $zlibHeader (required on Windows for java.util.zip / z.c).
On ARM64, if vcpkg install fails, run: scripts\build-zlib-arm64.ps1
Otherwise: vcpkg install zlib --triplet=$VcpkgTriplet
"@
} elseif (-not (Test-Path -LiteralPath $zlibLib)) {
    Write-Warning @"
Missing $zlibLib (required on Windows for java.util.zip).
Install native libraries for this machine:
  vcpkg install bdwgc zlib --triplet=$VcpkgTriplet
Or on ARM64 when vcpkg fails: scripts\build-zlib-arm64.ps1
"@
}

Write-Host 'Scala Native Windows environment loaded.'
Write-Host "  JAVA_HOME=$env:JAVA_HOME"
Write-Host "  LLVM_BIN=$env:LLVM_BIN"
Write-Host "  VCPKG triplet=$VcpkgTriplet"
Write-Host "  SCALANATIVE_INCLUDE_DIRS=$env:SCALANATIVE_INCLUDE_DIRS"
Write-Host "  SCALANATIVE_LIB_DIRS=$env:SCALANATIVE_LIB_DIRS"
Write-Host ("  PATH prepended: " + ($script:PathEntries -join '; '))
if ($Persist -ne 'None') {
    Write-Host "  (persisted to $Persist; PATH refreshed in this window)"
}
