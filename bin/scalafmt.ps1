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
$SCALAFMTTEST="$PSScriptRoot\scalafmt-CI-$SCALAFMT_VERSION.jar"
$COURSIER="$PSScriptRoot\coursier.ps1"

Try
{
    if ($testMode -eq "--install")
    {
        $scalafmtExists = Test-Path $SCALAFMTTEST
        if ($scalafmtExists -ne $True)
        {
            throw [System.IO.FileNotFoundException] "$SCALAFMTTEST not found."
            &$COURSIER bootstrap com.geirsson:scalafmt-cli_2.11:$SCALAFMT_VERSION --quiet --main org.scalafmt.cli.Cli -o $SCALAFMTTEST -f
            $scalafmtExists = Test-Path $SCALAFMTTEST
            if ($scalafmtExists -ne $True)
            {
                throw [System.IO.FileNotFoundException] "$SCALAFMTTEST not found."
            }
        }
    }
    else
    {
        $ScalaFmtRun = if ($testMode -eq "--test") {
            $SCALAFMTTEST
        }
        else {
            $SCALAFMT
        }

        $scalafmtExists = Test-Path $ScalaFmtRun
        if ($scalafmtExists -ne $True)
        {
            Write-Host "Trying to download $ScalaFmtRun"
            if ($testMode -eq "--test") {
                throw [System.IO.FileNotFoundException] "$ScalaFmtRun not found."
                &$COURSIER bootstrap com.geirsson:scalafmt-cli_2.11:$SCALAFMT_VERSION --quiet --main org.scalafmt.cli.Cli -o $SCALAFMTTEST -f
            }
            else {
                throw [System.IO.FileNotFoundException] "$ScalaFmtRun not found."
                &$COURSIER bootstrap --standalone com.geirsson:scalafmt-cli_2.11:$SCALAFMT_VERSION -o $SCALAFMT -f --quiet --main org.scalafmt.cli.Cli
            }
            $scalafmtExists = Test-Path $ScalaFmtRun
            if ($scalafmtExists -ne $True)
            {
                throw [System.IO.FileNotFoundException] "$ScalaFmtRun not found."
            }
        }

        if ($testMode) {
            &java -jar $ScalaFmtRun $testMode
        }
        else {
            &java -jar $ScalaFmtRun
        }
    }
}
Catch
{
    $ErrorMessage = $_.Exception.Message
    Write-Output $ErrorMessage
    exit 1
}

        