# Install LLVM into "C:\Program Files\LLVM" from the official llvm-project
# GitHub release. We do not use Chocolatey: its feed often fails with HTTP 503/504.
param(
    [Parameter(Mandatory = $true)]
    [string]$Version,
    [ValidateSet("x64", "arm64")]
    [string]$Architecture = "x64"
)

Set-StrictMode -Version 3.0
$ErrorActionPreference = "Stop"
# We check native exit codes ourselves.
$PSNativeCommandUseErrorActionPreference = $false

# SHA-256 of the release installers. Add a line when CI uses a new version.
$KnownSha256 = @{
    "x64" = @{
        "19.1.7" = "f19ae5bc4823ac69ec01dc2ded503ec80a04ad2208dda1595d1f0413c148ef90"
        "20.1.4" = "ee6206043ce6b24edbd6ea60645852f833744d9b5898d9c5f7f7684445381a25"
        "21.1.8" = "7a5386c26497db1691f320121e5b113364dd0274b98e55f15f4dbc00c0450113"
        "22.1.8" = "16e5709785fef73c854646241c4a92c5cd574318d1b33c63330dd7721903e55c"
        "23.1.3" = "5153f848ac87553118340f16f8d3cd4351d729bcba6e4785c6a4a9af29676de9"
    }
    "arm64" = @{
        "22.1.8" = "76f44ef1ba6eeb5a65904e9500f042f588fade49952778ce48f0374daa934396"
    }
}

if (-not $KnownSha256[$Architecture].ContainsKey($Version)) {
    throw "No known SHA-256 for LLVM $Version ($Architecture). Verify the official release checksum and add it to scripts/ci/windows/install-llvm.ps1."
}

$BaseUrl = "https://github.com/llvm/llvm-project/releases/download"
$Attempts = 5
$Target = Join-Path $env:ProgramFiles "LLVM"
$Clang = Join-Path $Target "bin\clang.exe"
$ReleaseArch = if ($Architecture -eq "arm64") { "woa64" } else { "win64" }
$ExpectedTarget = if ($Architecture -eq "arm64") { "aarch64" } else { "x86_64" }

function Test-MatchingClang {
    if (-not (Test-Path -LiteralPath $Clang)) {
        return $false
    }
    try {
        $versionOutput = & $Clang --version
        if ($LASTEXITCODE -ne 0) { return $false }
        $line = "$($versionOutput | Select-Object -First 1)"
        $machine = & $Clang -dumpmachine
        if ($LASTEXITCODE -ne 0) { return $false }
    } catch {
        Write-Host "Cannot run ${Clang}: $_"
        return $false
    }
    Write-Host "Found: $line, target $machine"
    return ($line -match ("clang version " + [regex]::Escape($Version) + "(\s|$)")) -and
        ($machine -match "^$ExpectedTarget-.*windows-msvc")
}

# Returns the file path, or $null when the release has no such file (HTTP 404).
function Save-ReleaseFile {
    param([string]$Name)
    $url = "$BaseUrl/llvmorg-$Version/$Name"
    $dest = Join-Path $env:RUNNER_TEMP $Name
    $curlArgs = @(
        "--silent", "--show-error", "--location",
        "--connect-timeout", "30", "--max-time", "600",
        # Give up when the speed stays under 1 MB/s for 60 s.
        "--speed-limit", "1000000", "--speed-time", "60",
        "--output", $dest, "--write-out", "%{http_code}", $url
    )
    for ($attempt = 1; $attempt -le $Attempts; $attempt++) {
        Remove-Item -LiteralPath $dest -Force -ErrorAction SilentlyContinue
        Write-Host "Downloading $url (attempt $attempt of $Attempts)"
        $http = "$(& curl.exe @curlArgs)".Trim()
        $curlExit = $LASTEXITCODE
        Write-Host "curl exit $curlExit, HTTP $http"
        if ($curlExit -eq 0 -and $http -eq "200") {
            return $dest
        }
        if ($curlExit -eq 0 -and $http -eq "404") {
            return $null
        }
        if ($attempt -lt $Attempts) {
            $delay = 15 * $attempt
            Write-Host "Retrying in $delay s"
            Start-Sleep -Seconds $delay
        }
    }
    throw "Download failed after $Attempts attempts: $url"
}

# Start-Process -Wait has no timeout. A silent installer that opens a dialog
# would otherwise block until the job times out.
function Invoke-Installer {
    param([string]$FilePath, [string]$Arguments, [int]$TimeoutMinutes = 20)
    Write-Host "Running $FilePath $Arguments"
    $proc = Start-Process -FilePath $FilePath -ArgumentList $Arguments -PassThru
    # Read Handle now, or ExitCode is empty after the process exits.
    $null = $proc.Handle
    if (-not $proc.WaitForExit($TimeoutMinutes * 60 * 1000)) {
        Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        throw "$FilePath did not finish in $TimeoutMinutes minutes"
    }
    Write-Host "Exit code $($proc.ExitCode)"
    return $proc.ExitCode
}

# The runner image has its own LLVM. Remove it so the new version goes into an
# empty folder and the cache saves only that version.
function Remove-InstalledLlvm {
    $nsis = Join-Path $Target "Uninstall.exe"
    if (Test-Path -LiteralPath $nsis) {
        # Without _?= the NSIS uninstaller copies itself to %TEMP% and returns
        # at once, before the files are removed. _?= must be the last argument.
        $code = Invoke-Installer -FilePath $nsis -Arguments "/S _?=$Target"
        if ($code -ne 0) {
            Write-Host "::warning::LLVM uninstaller exited $code"
        }
    }
    $uninstallKeys = @(
        "HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*",
        "HKLM:\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\*"
    )
    # MSI products use their product code as the key name.
    Get-ItemProperty -Path $uninstallKeys -ErrorAction SilentlyContinue |
        Where-Object {
            $_.PSChildName -match "^\{[0-9A-Fa-f-]+\}$" -and
            $_.PSObject.Properties["DisplayName"] -and
            "$($_.DisplayName)" -match "^LLVM\b"
        } |
        ForEach-Object {
            $code = Invoke-Installer -FilePath "$env:SystemRoot\System32\msiexec.exe" `
                -Arguments "/x $($_.PSChildName) /qn /norestart"
            if ($code -ne 0) {
                Write-Host "::warning::msiexec /x $($_.DisplayName) exited $code"
            }
        }
    # Files can stay locked for a short time after the uninstall.
    for ($attempt = 1; Test-Path -LiteralPath $Target; $attempt++) {
        try {
            Remove-Item -LiteralPath $Target -Recurse -Force
        } catch {
            if ($attempt -ge 3) {
                throw
            }
            Write-Host "Cannot remove ${Target}: $_"
            Start-Sleep -Seconds 5
        }
    }
}

if (Test-MatchingClang) {
    Write-Host "LLVM $Version is already installed."
    exit 0
}

# LLVM 23 and newer ship an msi instead of an exe.
$installer = Save-ReleaseFile -Name "LLVM-$Version-$ReleaseArch.exe"
if (-not $installer) {
    $installer = Save-ReleaseFile -Name "LLVM-$Version-$ReleaseArch.msi"
}
if (-not $installer) {
    throw "Release llvmorg-$Version has no LLVM-$Version-$ReleaseArch.exe or .msi"
}

$sha256 = (Get-FileHash -LiteralPath $installer -Algorithm SHA256).Hash.ToLowerInvariant()
Write-Host "SHA-256 $sha256"
if ($sha256 -ne $KnownSha256[$Architecture][$Version]) {
    throw "SHA-256 mismatch for ${installer}: expected $($KnownSha256[$Architecture][$Version]), got $sha256"
}

# Real-time scanning of thousands of new files makes the install much slower.
Add-MpPreference -ExclusionPath $Target -ErrorAction SilentlyContinue

Remove-InstalledLlvm

if ($installer.EndsWith(".exe")) {
    # /D= must be the last argument and must not be quoted.
    $code = Invoke-Installer -FilePath $installer -Arguments "/S /D=$Target"
    if ($code -ne 0) {
        throw "LLVM installer exited $code"
    }
} else {
    $log = Join-Path $env:RUNNER_TEMP "llvm-msi.log"
    $code = Invoke-Installer -FilePath "$env:SystemRoot\System32\msiexec.exe" `
        -Arguments "/i `"$installer`" /qn /norestart /l*v `"$log`""
    # 3010 and 1641: success, reboot required.
    if ($code -notin @(0, 3010, 1641)) {
        if (Test-Path -LiteralPath $log) {
            Get-Content -LiteralPath $log -Tail 50 | Write-Host
        }
        throw "LLVM msi installer exited $code"
    }
}

if (-not (Test-MatchingClang)) {
    throw "clang $Version ($Architecture) not found at $Clang after the install"
}
Write-Host "Installed LLVM $Version"
exit 0
