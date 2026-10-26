<#
.SYNOPSIS
  Build static ARM64 zlib with LLVM into the vcpkg installation layout.

.DESCRIPTION
  Provides a fallback when the vcpkg zlib build fails. Writes zlib headers
  and zlib.lib under installed/arm64-windows-static.

.EXAMPLE
  .\scripts\windows-arm\build-zlib-arm64.ps1
#>
[CmdletBinding()]
param(
    [string] $VcpkgRoot = 'C:\vcpkg',
    [string] $ZlibVersion = '1.3.1',
    [string] $LlvmBin = 'C:\Program Files\LLVM\bin'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$triplet = 'arm64-windows-static'
$installRoot = Join-Path $VcpkgRoot "installed\$triplet"
$includeDir = Join-Path $installRoot 'include'
$libDir = Join-Path $installRoot 'lib'
$work = Join-Path $env:TEMP "scalanative-zlib-arm64-$ZlibVersion"

$clang = Join-Path $LlvmBin 'clang.exe'
$llvmAr = Join-Path $LlvmBin 'llvm-ar.exe'
foreach ($tool in @($clang, $llvmAr)) {
    if (-not (Test-Path -LiteralPath $tool)) {
        throw "Missing LLVM tool: $tool (set -LlvmBin or install LLVM)"
    }
}

New-Item -ItemType Directory -Force -Path $work, $includeDir, $libDir | Out-Null

$archive = Join-Path $work "zlib-$ZlibVersion.tar.gz"
$srcRoot = Join-Path $work "zlib-$ZlibVersion"
if (-not (Test-Path -LiteralPath $srcRoot)) {
    Write-Host "Downloading zlib $ZlibVersion..."
    Invoke-WebRequest -Uri "https://github.com/madler/zlib/archive/refs/tags/v$ZlibVersion.tar.gz" -OutFile $archive
    tar -xzf $archive -C $work
}

$sources = @(
    'adler32.c', 'compress.c', 'crc32.c', 'deflate.c', 'inflate.c', 'infback.c',
    'inftrees.c', 'inffast.c', 'trees.c', 'uncompr.c', 'zutil.c',
    'gzlib.c', 'gzread.c', 'gzwrite.c', 'gzclose.c'
)

Write-Host "Compiling ARM64 objects..."
$objects = foreach ($source in $sources) {
    $object = Join-Path $work ($source -replace '\.c$', '.o')
    & $clang -target aarch64-pc-windows-msvc -c (Join-Path $srcRoot $source) -o $object -I $srcRoot -O2
    if ($LASTEXITCODE -ne 0) { throw "clang failed on $source" }
    $object
}

$zlibLib = Join-Path $libDir 'zlib.lib'
Write-Host "Creating $zlibLib..."
& $llvmAr rcs $zlibLib @objects
if ($LASTEXITCODE -ne 0) { throw 'llvm-ar failed' }

Copy-Item (Join-Path $srcRoot 'zlib.h') (Join-Path $includeDir 'zlib.h') -Force
$zconf = Join-Path $srcRoot 'zconf.h'
if (Test-Path -LiteralPath $zconf) {
    Copy-Item $zconf (Join-Path $includeDir 'zconf.h') -Force
}

Write-Host "Done."
Write-Host "  $includeDir\zlib.h"
Write-Host "  $zlibLib"
Write-Host "Load env and rebuild: . .\scripts\windows-arm\env-windows.ps1 ; sbt tests3/test"
