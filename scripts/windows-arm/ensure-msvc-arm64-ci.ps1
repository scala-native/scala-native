<#
.SYNOPSIS
  Install missing MSVC ARM64 libraries on CI runners.

.DESCRIPTION
  Checks the toolset, adds missing components, and verifies the result.
  Requires an elevated native ARM64 shell.

.EXAMPLE
  .\scripts\windows-arm\ensure-msvc-arm64-ci.ps1
#>
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$check = Join-Path $PSScriptRoot 'check-msvc-arm64.ps1'

& $check
if ($LASTEXITCODE -eq 0) { exit 0 }

$vswhere = Join-Path ${env:ProgramFiles(x86)} 'Microsoft Visual Studio\Installer\vswhere.exe'
$setup = Join-Path ${env:ProgramFiles(x86)} 'Microsoft Visual Studio\Installer\setup.exe'
if (-not (Test-Path -LiteralPath $vswhere) -or -not (Test-Path -LiteralPath $setup)) {
    Write-Error "Visual Studio Installer not found (vswhere=$vswhere)"
    exit 1
}

$installPath = & $vswhere -latest -products * -property installationPath 2>$null |
    Select-Object -First 1
if ([string]::IsNullOrWhiteSpace($installPath)) {
    Write-Error 'No Visual Studio installation found via vswhere'
    exit 1
}

Write-Host "MSVC ARM64 libs missing; modifying Visual Studio at: $installPath"
Write-Host "PROCESSOR_ARCHITECTURE=$($env:PROCESSOR_ARCHITECTURE)"

$p = Start-Process -FilePath $setup -ArgumentList @(
    'modify',
    '--installPath', $installPath,
    '--add', 'Microsoft.VisualStudio.Component.VC.Tools.ARM64',
    '--add', 'Microsoft.VisualStudio.Component.VC.Tools.x86.x64',
    '--add', 'Microsoft.VisualStudio.Component.Windows11SDK.22621',
    '--includeRecommended',
    '--passive', '--norestart', '--wait'
) -PassThru -Wait
Write-Host "setup.exe exit code: $($p.ExitCode)"
if ($p.ExitCode -notin 0, 3010) { exit $p.ExitCode }

& $check -Diagnostic
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host 'MSVC ARM64 link libraries are ready.'
