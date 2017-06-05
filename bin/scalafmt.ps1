# Format Scala code using scalafmt.
#
# Usage: scalafmt [--test]
#
# no parameters: format files
#      "--test": test correctness

param (
    [ValidateSet("--test")] 
    [string]$testMode
)

$SCALAFMT_VERSION="0.6.8"
$SCALAFMT="$PSScriptRoot\.scalafmt-$SCALAFMT_VERSION.jar"

Try
{
    &"$PSScriptRoot/coursier.ps1" bootstrap --standalone com.geirsson:scalafmt-cli_2.11:$SCALAFMT_VERSION -o $SCALAFMT -f --main org.scalafmt.cli.Cli

    $scalafmtExists = Test-Path $SCALAFMT
    if ($scalafmtExists -ne $True)
    {
        throw [System.IO.FileNotFoundException] "$SCALAFMT not found."
    }

    if ($testMode) {
        &java -jar $SCALAFMT $testMode
    }
    else {
        &java -jar $SCALAFMT
    }
}
Catch
{
    $ErrorMessage = $_.Exception.Message
    Write-Host $ErrorMessage
    exit 1
}