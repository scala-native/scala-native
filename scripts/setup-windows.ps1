#Requires -RunAsAdministrator
<#
.SYNOPSIS
  Install Scala Native development dependencies on Windows (ARM64 or x64).

.DESCRIPTION
  Mirrors CI in .github/actions/windows-arm-setup-env (ARM64).
  Toolchain notes for end users: docs/user/setup.md (Windows on ARM64).
  Contributor usage: docs/contrib/build-setup.md. x64 Windows CI uses Coursier in
  .github/actions/windows-setup-env instead.
  Run in an elevated PowerShell, then open a new terminal.

  Installs: Git, Microsoft OpenJDK 25, LLVM/Clang, sbt, VS 2022 Build Tools
  (C++ workload, MSVC x64/x86 + ARM64, Windows SDK), vcpkg (bdwgc, zlib
  for arm64-windows-static on WoA, x64-windows-static on x64).

  On Windows ARM64, native linking needs MSVC libraries under VC\Tools\MSVC\*\lib\arm64
  (for example libcpmt.lib and legacy_stdio_definitions.lib). The script installs
  the ARM64 toolset and verifies those files exist (including C:\Program\VC\...
  layout). On WoA, if ARM64 libs are missing after this script, run
  scripts\install-vs-arm64-msvc.ps1 (see .EXAMPLE in that file).

.EXAMPLE
  # Elevated ARM64-native PowerShell on WoA (or elevated x64 PowerShell on x64 Windows):
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\setup-windows.ps1
#>
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$VsBuildToolsPath = "${env:ProgramFiles(x86)}\Microsoft Visual Studio\2022\BuildTools"
$VsMsvcRoot = Join-Path $VsBuildToolsPath 'VC\Tools\MSVC'
$IsWindowsArm64 = [System.Runtime.InteropServices.RuntimeInformation]::OSArchitecture -eq
    [System.Runtime.InteropServices.Architecture]::Arm64

function Refresh-Path {
    $env:Path = [Environment]::GetEnvironmentVariable('Path', 'Machine') + ';' +
        [Environment]::GetEnvironmentVariable('Path', 'User')
}

function Install-VisualStudioBuildTools {
    Write-Host '==> Visual Studio 2022 Build Tools (C++ workload + Windows SDK)'
    $vsInstaller = "$env:TEMP\vs_BuildTools.exe"
    curl.exe -fsSL -o $vsInstaller 'https://aka.ms/vs/17/release/vs_BuildTools.exe'
    $vsAction = if (Test-Path -LiteralPath $VsBuildToolsPath) { 'modify' } else { 'install' }
    Write-Host "    Using vs_BuildTools.exe $vsAction (adds x64/x86 + ARM64 MSVC if missing)"
  # Pass each flag as its own argument - a single joined string breaks --installPath
  # when it contains spaces (e.g. Program Files (x86)).
    $p = Start-Process -FilePath $vsInstaller -ArgumentList @(
        $vsAction,
        '--installPath', $VsBuildToolsPath,
        '--add', 'Microsoft.VisualStudio.Workload.VCTools',
        '--add', 'Microsoft.VisualStudio.Component.VC.Tools.ARM64',
        '--add', 'Microsoft.VisualStudio.Component.VC.Tools.x86.x64',
        '--add', 'Microsoft.VisualStudio.Component.Windows11SDK.22621',
        '--includeRecommended',
        '--passive', '--wait', '--norestart'
    ) -PassThru -Wait
    if ($p.ExitCode -ne 0) { throw "VS Build Tools $vsAction failed: $($p.ExitCode)" }
}

function Get-MsvcLibSearchRoots {
    $roots = @($VsMsvcRoot)
    $programVc = 'C:\Program\VC\Tools\MSVC'
    if ((Test-Path -LiteralPath $programVc) -and ($roots -notcontains $programVc)) {
        $roots += $programVc
    }
    $roots
}

function Assert-MsvcLinkLibraries {
    param([string[]]$Architectures)
    $required = @('libcpmt.lib', 'legacy_stdio_definitions.lib')
    $found = $false
    foreach ($msvcRoot in Get-MsvcLibSearchRoots) {
        if (-not (Test-Path -LiteralPath $msvcRoot)) { continue }
        $msvcVersion = Get-ChildItem -LiteralPath $msvcRoot -Directory |
            Sort-Object Name -Descending |
            Select-Object -First 1
        if (-not $msvcVersion) { continue }
        foreach ($arch in $Architectures) {
            foreach ($subdir in @("lib\$arch", "lib\onecore\$arch")) {
                $libDir = Join-Path $msvcVersion.FullName $subdir
                $ok = $true
                foreach ($lib in $required) {
                    if (-not (Test-Path -LiteralPath (Join-Path $libDir $lib))) {
                        $ok = $false
                        break
                    }
                }
                if ($ok) {
                    foreach ($lib in $required) {
                        Write-Host "    OK: $(Join-Path $libDir $lib)"
                    }
                    $found = $true
                    break
                }
            }
            if ($found) { break }
        }
        if ($found) { break }
    }
    if (-not $found) {
        throw @"
Missing MSVC link library required for Scala Native on Windows.
Checked: $VsMsvcRoot and C:\Program\VC\Tools\MSVC

Install or repair Visual Studio 2022 Build Tools with:
  - Desktop development with C++ (VCTools workload)
  - MSVC v143 C++ ARM64 build tools (on Windows ARM64)
  - MSVC v143 C++ x64/x86 build tools
  - Windows 11 SDK (10.0.22621)

On WoA, run scripts\install-vs-arm64-msvc.ps1 from an elevated shell (see script .EXAMPLE).
"@
    }
}

function Install-Winget {
    param([string[]]$Ids, [string]$Override)
    $args = @(
        'install', '--disable-interactivity',
        '--accept-package-agreements', '--accept-source-agreements',
        '--source', 'winget'
    ) + $Ids
    if ($Override) { $args += '--override', $Override }
    & winget @args
    if ($LASTEXITCODE -ne 0) { throw "winget install failed: $($Ids -join ', ')" }
}

Write-Host '==> Git, JDK 25, LLVM'
Install-Winget @('Git.Git', 'Microsoft.OpenJDK.25', 'LLVM.LLVM')

Write-Host '==> sbt'
Install-Winget @('sbt.sbt')

Install-VisualStudioBuildTools

Write-Host '==> Verify MSVC link libraries (libcpmt, legacy_stdio_definitions)'
$msvcArchs = if ($IsWindowsArm64) { @('arm64', 'x64') } else { @('x64') }
Assert-MsvcLinkLibraries -Architectures $msvcArchs

Write-Host '==> vcpkg + native libraries'
$vcpkgRoot = 'C:\vcpkg'
$vcpkgTriplet = if ($IsWindowsArm64) { 'arm64-windows-static' } else { 'x64-windows-static' }
$vcpkgHostTriplet = if ($IsWindowsArm64) { 'arm64-windows' } else { 'x64-windows' }
if (-not (Test-Path "$vcpkgRoot\vcpkg.exe")) {
    git clone https://github.com/microsoft/vcpkg.git $vcpkgRoot
    & "$vcpkgRoot\bootstrap-vcpkg.bat" -disableMetrics
}
$env:VCPKG_DEFAULT_HOST_TRIPLET = $vcpkgHostTriplet
[Environment]::SetEnvironmentVariable('VCPKG_DEFAULT_HOST_TRIPLET', $vcpkgHostTriplet, 'Machine')
& "$vcpkgRoot\vcpkg.exe" install bdwgc zlib --triplet=$vcpkgTriplet

$libDir = "$vcpkgRoot\installed\$vcpkgTriplet\lib"
$zlib = Join-Path $libDir 'zlib.lib'
$zs = Join-Path $libDir 'zs.lib'
if (-not (Test-Path $zlib) -and (Test-Path $zs)) {
    Copy-Item $zs $zlib
}

Write-Host '==> Environment variables'
$llvmBin = 'C:\Program Files\LLVM\bin'
$javaHome = (Get-ChildItem 'C:\Program Files\Microsoft' -Filter 'jdk-25*' -Directory |
    Select-Object -First 1).FullName
$vars = @{
    JAVA_HOME                   = $javaHome
    VCPKG_INSTALLATION_ROOT     = $vcpkgRoot
    LLVM_BIN                    = $llvmBin
    SCALANATIVE_INCLUDE_DIRS    = "$vcpkgRoot\installed\$vcpkgTriplet\include"
    SCALANATIVE_LIB_DIRS        = "$vcpkgRoot\installed\$vcpkgTriplet\lib"
    SCALANATIVE_MODE            = 'debug'
    SCALANATIVE_GC              = 'immix'
    SCALANATIVE_LTO             = 'none'
    SCALANATIVE_OPTIMIZE        = 'true'
    ENABLE_EXPERIMENTAL_COMPILER = 'false'
}
foreach ($k in $vars.Keys) {
    [Environment]::SetEnvironmentVariable($k, $vars[$k], 'Machine')
}

$machinePath = [Environment]::GetEnvironmentVariable('Path', 'Machine')
$pathEntries = @(
    (Join-Path $javaHome 'bin'),
    $llvmBin,
    "${env:ProgramFiles(x86)}\sbt\bin",
    "${env:ProgramFiles}\Git\cmd",
    $vcpkgRoot
)
foreach ($p in $pathEntries) {
    if ($p -and (Test-Path -LiteralPath $p) -and $machinePath -notlike "*$p*") {
        $machinePath = "$p;$machinePath"
    }
}
[Environment]::SetEnvironmentVariable('Path', $machinePath, 'Machine')

& git config --global core.autocrlf false
& git config --global core.eol lf
& git config --global core.symlinks true

# Git for Windows often materializes symlinked patch files as tiny text stubs unless
# Developer Mode (or an elevated shell) allows real symlinks. Repair known stubs.
Get-ChildItem "$PSScriptRoot\..\scalalib" -Recurse -Filter '*.patch' -File |
    Where-Object { $_.Length -lt 120 } |
    ForEach-Object {
        $text = Get-Content $_.FullName -Raw
        if ($text -match '^\.\./') {
            $target = Resolve-Path (Join-Path $_.DirectoryName $text.Trim())
            Copy-Item -Force $target $_.FullName
            Write-Host "Repaired symlink stub: $($_.FullName)"
        }
    }

Refresh-Path
Write-Host "`nDone. In a new terminal:"
Write-Host '  cd <scala-native>'
Write-Host '  Set-ExecutionPolicy -Scope Process Bypass'
Write-Host '  . .\scripts\env-windows.ps1    # PATH + SCALANATIVE_* for this shell'
Write-Host '  git reset --hard HEAD          # if checkout happened before autocrlf=false'
Write-Host '  sbt'
if ($IsWindowsArm64) {
    Write-Host "`nIf linking fails, run (elevated): scripts\install-vs-arm64-msvc.ps1"
    Write-Host "Verify (ARM64 native link): clang --version ; sandbox3/run"
} else {
    Write-Host "`nVerify: java -version ; clang --version ; sbt --version"
}
