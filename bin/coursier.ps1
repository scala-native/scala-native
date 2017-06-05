# Windows version of coursier

$COURSIER="$PSScriptRoot\.coursier.jar"

$url = "https://github.com/coursier/coursier/raw/master/coursier"

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

    $log = (&java -jar $COURSIER $args) -join "`n"
    Write-Host $log
}
Catch
{
    $ErrorMessage = $_.Exception.Message
    Write-Host $ErrorMessage
    exit 1
}