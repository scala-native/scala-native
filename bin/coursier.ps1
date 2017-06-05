# Windows version of coursier

$COURSIER="$PSScriptRoot\.coursier.jar"

#$url = "https://github.com/coursier/coursier/raw/master/coursier"
$url = "https://git.io/vgvpD"

Try
{
    $coursierExists = Test-Path $COURSIER
    if ($coursierExists -ne $True)
    {
        Invoke-WebRequest -Uri $url -OutFile $COURSIER
        $coursierExists = Test-Path $COURSIER
        if ($coursierExists -ne $True)
        {
            throw [System.IO.FileNotFoundException] "$COURSIER not found."
        }
    }

    $old_ErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = 'SilentlyContinue'
    $log = (&java -jar $COURSIER $args) -join "`n"
    $ErrorActionPreference = $old_ErrorActionPreference
    Write-Output $log
}
Catch
{
    $ErrorMessage = $_.Exception.Message
    Write-Output $ErrorMessage
    exit 1
}