# Exposes vcpkg static libraries to Scala Native builds
param([Parameter(Mandatory)][string]$VcpkgLibs)
$ErrorActionPreference = 'Stop'

$libDir = Join-Path $VcpkgLibs 'lib'
"SCALANATIVE_INCLUDE_DIRS=$VcpkgLibs\include" >> $env:GITHUB_ENV
"SCALANATIVE_LIB_DIRS=$libDir" >> $env:GITHUB_ENV

# javalib does not link zlib on Windows, its name depends on the zlib version
$zlib = 'libzs', 'zs', 'zlib' |
  Where-Object { Test-Path -LiteralPath (Join-Path $libDir "$_.lib") } |
  Select-Object -First 1
if (-not $zlib) {
  Get-ChildItem -LiteralPath $libDir | ForEach-Object { Write-Host $_.Name }
  Write-Error "None of libzs.lib, zs.lib, zlib.lib in $libDir"
}
"SCALANATIVE_LINKING_OPTIONS=-l$zlib" >> $env:GITHUB_ENV
