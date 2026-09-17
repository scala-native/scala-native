<#
.SYNOPSIS
  Install MSVC ARM64 link libraries for Scala Native on Windows ARM64.

.DESCRIPTION
  Adds the VC.Tools.ARM64 component to VS 2022 Build Tools and verifies
  libcpmt.lib and legacy_stdio_definitions.lib exist under lib\arm64.

  Must run as ARM64-native PowerShell on WoA. If started from x64-emulated
  PowerShell (common under Parallels), the script relaunches automatically.

.EXAMPLE
  # From repo root (elevated; relaunches as ARM64-native and requests admin if needed):
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\install-vs-arm64-msvc.ps1

  # Verify without admin:
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-msvc-arm64.ps1
#>
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$VsBuildToolsPath = "${env:ProgramFiles(x86)}\Microsoft Visual Studio\2022\BuildTools"
$VsSetupExe = "${env:ProgramFiles(x86)}\Microsoft Visual Studio\Installer\setup.exe"
$VsMsvcRoot = Join-Path $VsBuildToolsPath 'VC\Tools\MSVC'
$LogFile = Join-Path $env:TEMP 'scalanative-vs-arm64-install.log'
$ResultFile = Join-Path $env:TEMP 'scalanative-vs-arm64-result.txt'

function Get-Arm64PowerShell {
    Join-Path $env:Windir 'System32\WindowsPowerShell\v1.0\powershell.exe'
}

function Test-IsAdministrator {
    $principal = New-Object Security.Principal.WindowsPrincipal(
        [Security.Principal.WindowsIdentity]::GetCurrent())
    $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Test-NeedsArm64Relaunch {
    ([System.Runtime.InteropServices.RuntimeInformation]::OSArchitecture -eq
        [System.Runtime.InteropServices.Architecture]::Arm64) -and
        ($env:PROCESSOR_ARCHITECTURE -eq 'AMD64')
}

function Restart-InPowerShell {
    param(
        [string]$PowerShellPath,
        [switch]$RunAsAdmin
    )
    $argList = @(
        '-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $PSCommandPath
    )
    $params = @{
        FilePath     = $PowerShellPath
        ArgumentList = $argList
        PassThru     = $true
        Wait         = $true
    }
    if ($RunAsAdmin) { $params['Verb'] = 'RunAs' }
    $proc = Start-Process @params
    exit $proc.ExitCode
}

# Bootstrap: admin + ARM64-native (VS excludes ARM64 packages when installer runs as x64).
if ([System.Runtime.InteropServices.RuntimeInformation]::OSArchitecture -eq
    [System.Runtime.InteropServices.Architecture]::Arm64) {
    $arm64Ps = Get-Arm64PowerShell
    if (-not (Test-Path -LiteralPath $arm64Ps)) {
        throw "ARM64 PowerShell not found at $arm64Ps"
    }
    if (-not (Test-IsAdministrator)) {
        Write-Host 'Requesting administrator privileges (ARM64-native PowerShell)...'
        Restart-InPowerShell -PowerShellPath $arm64Ps -RunAsAdmin
    }
    if (Test-NeedsArm64Relaunch) {
        Write-Host @"
Detected x64-emulated PowerShell on ARM64 Windows.
Visual Studio would skip ARM64 libraries - relaunching as ARM64-native...
"@
        Restart-InPowerShell -PowerShellPath $arm64Ps
    }
} elseif (-not (Test-IsAdministrator)) {
    Write-Host 'Requesting administrator privileges...'
    Restart-InPowerShell -PowerShellPath (Get-Command powershell.exe).Source -RunAsAdmin
}

function Write-Log {
    param([string]$Message)
    $line = "[$(Get-Date -Format 'HH:mm:ss')] $Message"
    Write-Host $line
    Add-Content -LiteralPath $LogFile -Value $line
}

function Get-MsvcArm64LibSearchRoots {
    $roots = [System.Collections.Generic.List[string]]::new()
    if (Test-Path -LiteralPath $VsMsvcRoot) { [void]$roots.Add($VsMsvcRoot) }
    $bases = @(
        ${env:ProgramFiles(x86)},
        $env:ProgramFiles
    ) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    foreach ($year in @('2022', '2019')) {
        foreach ($edition in @('BuildTools', 'Community', 'Professional', 'Enterprise')) {
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

function Get-VisualStudioInstallPath {
    if (Test-Path -LiteralPath $VsBuildToolsPath) { return $VsBuildToolsPath }
    $vswhere = Join-Path ${env:ProgramFiles(x86)} 'Microsoft Visual Studio\Installer\vswhere.exe'
    if (-not (Test-Path -LiteralPath $vswhere)) { return $null }
    $path = & $vswhere -latest -products * -property installationPath 2>$null | Select-Object -First 1
    if ([string]::IsNullOrWhiteSpace($path)) { return $null }
    $path
}

function Get-LatestMsvcArm64LibDir {
    $required = @('libcpmt.lib', 'legacy_stdio_definitions.lib')
    foreach ($msvcRoot in Get-MsvcArm64LibSearchRoots) {
        if (-not (Test-Path -LiteralPath $msvcRoot)) { continue }
        $msvcVersion = Get-ChildItem -LiteralPath $msvcRoot -Directory |
            Sort-Object Name -Descending |
            Select-Object -First 1
        if (-not $msvcVersion) { continue }
        foreach ($subdir in @('lib\arm64', 'lib\onecore\arm64')) {
            $libDir = Join-Path $msvcVersion.FullName $subdir
            $ok = $true
            foreach ($lib in $required) {
                if (-not (Test-Path -LiteralPath (Join-Path $libDir $lib))) { $ok = $false; break }
            }
            if ($ok) { return $libDir }
        }
    }
    $null
}

function Test-Arm64MsvcLibs {
    $required = @('libcpmt.lib', 'legacy_stdio_definitions.lib')
    $libDir = Get-LatestMsvcArm64LibDir
    if (-not $libDir) { return $false }
    foreach ($lib in $required) {
        if (-not (Test-Path -LiteralPath (Join-Path $libDir $lib))) { return $false }
        Write-Log "OK: $(Join-Path $libDir $lib)"
    }
    return $true
}

function Wait-VisualStudioInstallerIdle {
    $names = @('setup', 'vs_installer', 'vs_buildtools')
    for ($i = 0; $i -lt 72; $i++) {
        $running = @(Get-Process -Name $names -ErrorAction SilentlyContinue)
        if ($running.Count -eq 0) { return }
        Write-Log "Waiting for Visual Studio Installer ($($running.Count) process(es))..."
        Start-Sleep -Seconds 5
    }
    throw 'Visual Studio Installer still running - close it and retry.'
}

function Install-Arm64Msvc {
    if (Test-Arm64MsvcLibs) {
        Write-Log 'ARM64 MSVC libraries already present; skipping VS modify.'
        return
    }

    if (-not (Test-Path -LiteralPath $VsSetupExe)) {
        throw "Visual Studio Installer not found at $VsSetupExe. Run scripts\setup-windows.ps1 first."
    }

    Wait-VisualStudioInstallerIdle

    $installPath = Get-VisualStudioInstallPath
    if (-not $installPath) {
        throw 'No Visual Studio 2022 installation found (Build Tools or Enterprise). Run scripts\setup-windows.ps1 first.'
    }
    $vsAction = 'modify'
    Write-Log "Running setup.exe $vsAction (MSVC ARM64 + x64/x86 + Windows SDK)"
    Write-Log "PROCESSOR_ARCHITECTURE=$($env:PROCESSOR_ARCHITECTURE)"
    Write-Log "Install path: $installPath"

    $argumentList = @(
        $vsAction,
        '--installPath', $installPath,
        '--add', 'Microsoft.VisualStudio.Workload.VCTools',
        '--add', 'Microsoft.VisualStudio.Component.VC.Tools.ARM64',
        '--add', 'Microsoft.VisualStudio.Component.VC.Tools.x86.x64',
        '--add', 'Microsoft.VisualStudio.Component.Windows11SDK.22621',
        '--includeRecommended',
        '--passive', '--norestart'
    )

    $p = Start-Process -FilePath $VsSetupExe -ArgumentList $argumentList -PassThru -Wait
    Write-Log "setup.exe exit code: $($p.ExitCode)"

    if ($p.ExitCode -notin 0, 3010) {
        $setupLog = Get-ChildItem -LiteralPath $env:TEMP -Filter 'dd_setup*.log' -ErrorAction SilentlyContinue |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1
        $logHint = if ($setupLog) { " See: $($setupLog.FullName)" } else { '' }
        $extra = switch ($p.ExitCode) {
            87   { ' Invalid command-line arguments (see installer help output above).' }
            1618 { ' Another Visual Studio install is in progress - wait for it to finish.' }
            1001 { ' Visual Studio Installer is already running.' }
            default { '' }
        }
        throw "setup.exe $vsAction failed with exit code $($p.ExitCode).$extra$logHint"
    }
    if ($p.ExitCode -eq 3010) {
        Write-Log 'Install succeeded; reboot may be required before libraries appear.'
    }
}

try {
    '' | Out-File -LiteralPath $LogFile -Encoding utf8
    Write-Log "Log file: $LogFile"
    Write-Log "Running as: $([Security.Principal.WindowsIdentity]::GetCurrent().Name)"
    Write-Log "PROCESSOR_ARCHITECTURE=$($env:PROCESSOR_ARCHITECTURE)"

    Install-Arm64Msvc
    if (-not (Test-Arm64MsvcLibs)) {
        throw @"
ARM64 MSVC libraries still missing after setup.exe completed.
Checked: $VsMsvcRoot and C:\Program\VC\Tools\MSVC (ARM64-native layout).
If the log contains 'chip value different than ... x64 will be excluded', the installer
ran as x64 - rerun with ARM64-native PowerShell (see .EXAMPLE in this script).
"@
    }

    'OK' | Out-File -LiteralPath $ResultFile -Encoding ascii -NoNewline
    Write-Log 'SUCCESS: ARM64 MSVC link libraries are installed.'
    exit 0
}
catch {
    $msg = "FAIL: $($_.Exception.Message)"
    Write-Log $msg
    $msg | Out-File -LiteralPath $ResultFile -Encoding utf8
    Write-Host "`n$msg" -ForegroundColor Red
    Write-Host "Full log: $LogFile" -ForegroundColor Yellow
    Write-Host @"

Manual install: open "Visual Studio Installer" -> Modify Build Tools -> Individual components:
  [x] MSVC v143 - VS 2022 C++ ARM64/ARM64EC build tools (ARM64)
  [x] Windows 11 SDK (10.0.22621)

Use the ARM64 Visual Studio Installer if offered (not the x64 one under Program Files (x86)).
"@ -ForegroundColor Yellow
    exit 1
}
