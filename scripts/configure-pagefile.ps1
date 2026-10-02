<#
.SYNOPSIS
  Configure the Windows pagefile for GitHub Actions runners (including windows-11-arm).

.DESCRIPTION
  al-cheb/configure-pagefile-action calls NtCreatePagingFile, which can throw a spurious
  Win32Exception("The operation completed successfully") on windows-11-arm. This script
  configures the pagefile via WMI/CIM (with explicit uint32 sizes) and falls back to the
  PagingFiles registry value when CIM creation fails.

.PARAMETER MinimumSizeGB
  Initial pagefile size in gigabytes.

.PARAMETER MaximumSizeGB
  Maximum pagefile size in gigabytes.

.PARAMETER DiskRoot
  Drive letter for pagefile.sys (e.g. C:).

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\configure-pagefile.ps1 -MinimumSizeGB 4 -MaximumSizeGB 12 -DiskRoot C:
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [int] $MinimumSizeGB,

    [Parameter(Mandatory = $true)]
    [int] $MaximumSizeGB,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[A-Za-z]:$')]
    [string] $DiskRoot
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Test-PageFilePropertyTypes {
    param(
        [string] $PagefilePath,
        [uint32] $InitialMb,
        [uint32] $MaximumMb
    )
    $cimClass = Get-CimClass -ClassName Win32_PageFileSetting
    New-CimInstance -CimClass $cimClass -Property @{
        Name        = $PagefilePath
        InitialSize = $InitialMb
        MaximumSize = $MaximumMb
    } -ClientOnly | Out-Null
}

function Set-PageFileRegistry {
    param(
        [string] $PagefilePath,
        [uint32] $InitialMb,
        [uint32] $MaximumMb
    )
    $regPath = 'HKLM:\SYSTEM\CurrentControlSet\Control\Session Manager\Memory Management'
    $entry = '{0} {1} {2}' -f $PagefilePath, $InitialMb, $MaximumMb
    Set-ItemProperty -LiteralPath $regPath -Name 'PagingFiles' -Type MultiString -Value @($entry) -Force
    Write-Host "Set registry PagingFiles entry: $entry"
}

if ($MinimumSizeGB -le 0 -or $MaximumSizeGB -le 0) {
    throw 'Pagefile sizes must be positive.'
}
if ($MaximumSizeGB -lt $MinimumSizeGB) {
    throw "MaximumSizeGB ($MaximumSizeGB) must be >= MinimumSizeGB ($MinimumSizeGB)."
}

$initialMb = [uint32]($MinimumSizeGB * 1024)
$maximumMb = [uint32]($MaximumSizeGB * 1024)
$pagefilePath = Join-Path $DiskRoot 'pagefile.sys'

Write-Host "Configuring pagefile at $pagefilePath (${MinimumSizeGB}GB .. ${MaximumSizeGB}GB, ${initialMb}MB .. ${maximumMb}MB)"

# Validate CIM property types before touching the system (no admin required).
Test-PageFilePropertyTypes -PagefilePath $pagefilePath -InitialMb $initialMb -MaximumMb $maximumMb
Write-Host 'CIM property types validated.'

$computerSystem = Get-CimInstance -ClassName Win32_ComputerSystem
if ($computerSystem.AutomaticManagedPagefile) {
    Set-CimInstance -InputObject $computerSystem -Property @{ AutomaticManagedPagefile = $false }
    Write-Host 'Disabled automatic pagefile management.'
}

Get-CimInstance -ClassName Win32_PageFileSetting -ErrorAction SilentlyContinue |
    ForEach-Object {
        Write-Host "Removing existing pagefile setting: $($_.Name)"
        Remove-CimInstance -InputObject $_
    }

$cimConfigured = $false
try {
    New-CimInstance -ClassName Win32_PageFileSetting -Property @{
        Name        = $pagefilePath
        InitialSize = $initialMb
        MaximumSize = $maximumMb
    } | Out-Null
    $cimConfigured = $true
    Write-Host 'Created Win32_PageFileSetting via CIM.'
} catch {
    Write-Warning "CIM pagefile creation failed: $($_.Exception.Message)"
    Write-Host 'Falling back to PagingFiles registry configuration.'
    Set-PageFileRegistry -PagefilePath $pagefilePath -InitialMb $initialMb -MaximumMb $maximumMb
}

$settings = @(Get-CimInstance -ClassName Win32_PageFileSetting -ErrorAction SilentlyContinue)
if ($settings.Count -eq 0 -and -not $cimConfigured) {
    $registryEntry = (Get-ItemProperty -LiteralPath 'HKLM:\SYSTEM\CurrentControlSet\Control\Session Manager\Memory Management' -Name 'PagingFiles').PagingFiles
    if ($registryEntry -notcontains ('{0} {1} {2}' -f $pagefilePath, $initialMb, $maximumMb)) {
        throw 'Pagefile configuration was not applied.'
    }
    Write-Host "Verified registry PagingFiles: $($registryEntry -join '; ')"
} else {
    foreach ($setting in $settings) {
        Write-Host ("Verified pagefile setting: {0} (initial={1}MB, maximum={2}MB)" -f $setting.Name, $setting.InitialSize, $setting.MaximumSize)
        if ($setting.Name -ieq $pagefilePath) {
            if ([uint32]$setting.InitialSize -ne $initialMb -or [uint32]$setting.MaximumSize -ne $maximumMb) {
                throw ("Pagefile sizes mismatch: expected {0}..{1} MB, got {2}..{3} MB" -f $initialMb, $maximumMb, $setting.InitialSize, $setting.MaximumSize)
            }
        }
    }
}

$usage = Get-CimInstance -ClassName Win32_PageFileUsage -ErrorAction SilentlyContinue |
    Select-Object -First 1
if ($usage) {
    Write-Host "Current pagefile usage: $($usage.Name) (allocated MB: $($usage.AllocatedBaseSize), current usage: $($usage.CurrentUsage))"
}

Write-Host 'Pagefile configuration complete.'
