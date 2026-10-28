# Exercise the installer with controlled download failures on a disposable CI runner.
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("recover", "all-fail", "bad-sha", "unsupported")]
    [string]$Mode
)

Set-StrictMode -Version 3.0
$ErrorActionPreference = "Stop"
$PSNativeCommandUseErrorActionPreference = $false

$clang = Join-Path $env:ProgramFiles "LLVM\bin\clang.exe"
$before = if (Test-Path -LiteralPath $clang) {
    (Get-FileHash -LiteralPath $clang -Algorithm SHA256).Hash
} else { $null }
$installed = if ($before) { & $clang --version | Select-Object -First 1 } else { "" }
# Choose a pinned version different from the image's LLVM to exercise installation.
$version = if ($installed -match 'clang version 20\.1\.4(\s|$)') { "19.1.7" } else { "20.1.4" }
if ($Mode -eq "unsupported") { $version = "0.0.0" }

$global:LlvmTestMode = $Mode
$global:LlvmTestCurl = (Get-Command curl.exe -CommandType Application | Select-Object -First 1).Source
$global:LlvmTestAttempts = 0

# Override just the download command; keep the installer's retry, checksum,
# uninstall and install paths intact. No third-party test endpoint is needed.
function global:curl.exe {
    $global:LlvmTestAttempts++
    if ($global:LlvmTestMode -eq "all-fail" -or
        ($global:LlvmTestMode -eq "recover" -and $global:LlvmTestAttempts -eq 1)) {
        $global:LASTEXITCODE = 6 # Could not resolve host.
        return "000"
    }
    if ($global:LlvmTestMode -eq "recover" -and $global:LlvmTestAttempts -eq 2) {
        $global:LASTEXITCODE = 0
        return "503"
    }
    if ($global:LlvmTestMode -eq "bad-sha") {
        $outputIndex = [array]::IndexOf($args, "--output")
        if ($outputIndex -lt 0) { throw "Missing curl output path" }
        Set-Content -LiteralPath $args[$outputIndex + 1] -Value "Not an LLVM installer"
        $global:LASTEXITCODE = 0
        return "200"
    }
    if ($global:LlvmTestMode -eq "unsupported") {
        throw "Unsupported version reached the download command"
    }
    & $global:LlvmTestCurl @args
    $global:LASTEXITCODE = $LASTEXITCODE
}

$failure = $null
try {
    & "$PSScriptRoot/install-llvm.ps1" -Version $version -Architecture x64
} catch {
    $failure = $_.Exception.Message
    Write-Host $failure
}

if ($Mode -eq "recover") {
    if ($failure) { throw "Recovery failed: $failure" }
    if ($global:LlvmTestAttempts -lt 3 -or $global:LlvmTestAttempts -gt 5) {
        throw "Expected recovery within 3..5 attempts, got $global:LlvmTestAttempts"
    }
    $line = & $clang --version | Select-Object -First 1
    if ($LASTEXITCODE -ne 0 -or $line -notmatch ("clang version " + [regex]::Escape($version) + "(\s|$)")) {
        throw "Recovery did not install LLVM $version"
    }
} else {
    $expected = switch ($Mode) {
        "all-fail" { "Download failed after 5 attempts" }
        "bad-sha" { "SHA-256 mismatch" }
        "unsupported" { "No known SHA-256" }
    }
    if (-not $failure -or -not $failure.Contains($expected)) {
        throw "Expected '$expected', got '$failure'"
    }
    $expectedAttempts = switch ($Mode) {
        "all-fail" { 5 }
        "bad-sha" { 1 }
        "unsupported" { 0 }
    }
    if ($global:LlvmTestAttempts -ne $expectedAttempts) {
        throw "Expected $expectedAttempts downloads, got $global:LlvmTestAttempts"
    }
    $after = if (Test-Path -LiteralPath $clang) {
        (Get-FileHash -LiteralPath $clang -Algorithm SHA256).Hash
    } else { $null }
    if ($before -ne $after) { throw "Failure changed the image's LLVM installation" }
}

Write-Host "PASS: $Mode"
exit 0
