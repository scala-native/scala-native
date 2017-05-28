# Format C/C++ code using clang-format.
#
# To ensure reproducible formatting the script checks that clang-format
# is from the most recent version of LLVM supported by Scala Native.
#
# Usage: clangfmt [--test]
#
# Set CLANG_FORMAT_PATH to configure path to clang-format or
# add path to clang-format.exe to enviromental variable 'PATH'

param (
    [ValidateSet("--test")] 
    [string]$testMode
)

$CLANG_FORMAT_VERSION="4.0"
$CLANG_FORMAT_VERSION_STRING="clang-format version $CLANG_FORMAT_VERSION"

function Get-ClangEnvPath
{
    [OutputType([string])]
    param (
        [Parameter(Mandatory=$true)][string]$file
    )

    $envCFP = $env:CLANG_FORMAT_PATH

    if ($envCFP) {
        if ($envCFP.EndsWith("clang-format.exe")) {
            Write-Output $envCFP
        }
        else {
            $fullpath = Join-Path $env:CLANG_FORMAT_PATH $file
            Write-Output $fullpath
        }
    }
    else {
        $fullpath = Join-Path "." $file
        Write-Output $fullpath
    }
}

function Resolve-ClangPath
{
    [OutputType([string])]
    param (
        [Parameter(Mandatory=$true)][string]$file
    )

    $returnPath = $file

    $testPath = Get-ClangEnvPath $file
    if (Test-Path $testPath) {
        $returnPath = $testPath
    } else {
        $allPaths = ($env:PATH).Split(";") | ForEach-Object {
            $testPath = Join-Path $_  $file
            if (Test-Path $testPath) {
                $testPath
                break
            }
        }
        if ($allPaths)
        {
            $returnPath = $allPaths[0]
        }
    }
    Write-Output $returnPath
}

function Show-Diff {
  param(
    [String] $prefix,
    [String] $string,
    [int] $i
  )
  $len = $string.Length
  $radius = 40
  $start = if ($i -lt $radius) {0} else {$i-$radius}
  $end = if ($i -ge ($len-$radius)) {$len-1} else {$i+$radius}
  $text = $string.Substring($start, $end - $start)
  $cursorEnd = "^ ($([int]$string[$i]))"
  $cursor = " "
  for ( $i = 0; $i -lt $prefix.Length; $i++ ) {
      $cursor += " "
  }
  for ( $i = 0; $i -lt $radius; $i++ ) {
      $cursor += " "
  }
  $cursor += $cursorEnd
  Write-Host -f Red "$prefix $text"
  Write-Host -f Red $cursor
}

function Compare-String {
  param(
    [String] $string1,
    [String] $string2
  )
  if ( $string1 -eq $string2 ) {
    return $true
  }
  for ( $i = 0; $i -lt $string1.Length; $i++ ) {
    if ( $string1[$i] -ne $string2[$i] ) {
        Show-Diff "o:" $string1 $i
        Show-Diff "f:" $string2 $i
        break
    }
  }
  return $false
}

function Test-File {
    param(
        [String] $cpath
    )
    $original = Get-Content $cpath -Encoding UTF8
    $formatted = &$clangfmt -style=file $cpath
    return Compare-String $original $formatted    
}

function Format-File {
    param(
        [String] $cpath
    )
    &$clangfmt -style=file -i $cpath
}

if ($env:CLANG_FORMAT_PATH) {Write-Host "CLANG_FORMAT_PATH=$($env:CLANG_FORMAT_PATH)"}

$clangfmt = Resolve-ClangPath "clang-format.exe"
Write-Host "Found clang-format: $clangfmt"

$clangfmtVersion = &$clangfmt --version
if (-Not $clangfmtVersion.StartsWith($CLANG_FORMAT_VERSION_STRING,"CurrentCultureIgnoreCase")) {
    Write-Host -f Red "Error: required clang-format version 4.0 but found $clangfmtVersion"
    exit 1
}
Write-Host (Get-Item .)
Get-ChildItem -Path ".\*" -Include *.c, *.cpp, *.h -Recurse | ForEach-Object {
    $cpath = Resolve-Path -Relative $_
    $verb = if ($testMode) {"Testing"} else {"Formatting"}
    Write-Host "$verb... $cpath"
    if ($testMode) {
        if ((Test-File $cpath) -eq $false) {
            Write-Host -f Red "Error: File $cpath is incorrectly formatted"
            exit 1
        }
    }
    else {
        Format-File $cpath
    }
}
Write-Host -f Green "Successful!"