<#
.SYNOPSIS
  Configure the Windows pagefile for GitHub Actions runners (including windows-11-arm).

.DESCRIPTION
  al-cheb/configure-pagefile-action can throw Win32Exception("The operation completed
  successfully") on ARM64 hosts. Scala Native Windows CI needs a larger pagefile because
  Windows commits reserved virtual memory up front (see windows-setup-env comments).

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

if ($MinimumSizeGB -le 0 -or $MaximumSizeGB -le 0) {
    throw 'Pagefile sizes must be positive.'
}
if ($MaximumSizeGB -lt $MinimumSizeGB) {
    throw "MaximumSizeGB ($MaximumSizeGB) must be >= MinimumSizeGB ($MinimumSizeGB)."
}

$initialMb = $MinimumSizeGB * 1024
$maximumMb = $MaximumSizeGB * 1024
$pagefilePath = Join-Path $DiskRoot 'pagefile.sys'

Write-Host "Configuring pagefile at $pagefilePath (${MinimumSizeGB}GB .. ${MaximumSizeGB}GB)"

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

New-CimInstance -ClassName Win32_PageFileSetting -Property @{
    Name        = $pagefilePath
    InitialSize = $initialMb
    MaximumSize = $maximumMb
} | Out-Null

$usage = Get-CimInstance -ClassName Win32_PageFileUsage -ErrorAction SilentlyContinue |
    Select-Object -First 1
if ($usage) {
    Write-Host "Current pagefile: $($usage.Name) (allocated MB: $($usage.AllocatedBaseSize), current usage: $($usage.CurrentUsage))"
}

Write-Host 'Pagefile configuration applied (restart may be required on some images; GHA runners pick this up for new commits).'
