# Format Scala code using scalafmt.
#
# Usage: scalafmt [--test]
#
# no parameters: format files
#      "--test": test correctness

param (
    [ValidateSet("--test", "--install")] 
    [string]$testMode
)

$SCALAFMT_VERSION="0.6.8"
$SCALAFMT="$PSScriptRoot\.scalafmt-$SCALAFMT_VERSION.jar"
$COURSIER="$PSScriptRoot\coursier.ps1"

Try
{
    $scalafmtExists = Test-Path $SCALAFMT
    if ($scalafmtExists -ne $True)
    {
        Write-Host "Trying to download $SCALAFMT"
        Write-Host "$COURSIER bootstrap --standalone com.geirsson:scalafmt-cli_2.11:$SCALAFMT_VERSION -o $SCALAFMT -f --quiet --main org.scalafmt.cli.Cli"
        &$COURSIER bootstrap --standalone com.geirsson:scalafmt-cli_2.11:$SCALAFMT_VERSION -o $SCALAFMT -f --quiet --main org.scalafmt.cli.Cli

        #$scalafmtExists = Test-Path $SCALAFMT
        #if ($scalafmtExists -ne $True)
        #{
        #    throw [System.IO.FileNotFoundException] "$SCALAFMT not found."
        #}
    }

    if ($testMode -ne "--install") {
        if ($testMode) {
            &java -jar $SCALAFMT $testMode 2>&1
        }
        else {
            &java -jar $SCALAFMT 2>&1
        }
    }
}
Catch
{
    $ErrorMessage = $_.Exception.Message
    Write-Output $ErrorMessage
    exit 1
}

        