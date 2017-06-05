# Windows version of coursier

$COURSIER="$PSScriptRoot\.coursier.jar"

$url = "https://github.com/coursier/coursier/raw/master/coursier"

Try
{
    Invoke-WebRequest -Uri $url -OutFile $COURSIER

    $coursierExists = Test-Path $COURSIER
    if ($coursierExists -ne $True)
    {
        throw [System.IO.FileNotFoundException] "$COURSIER not found."
    }

    &java -jar $COURSIER $args
}
Catch
{
    $ErrorMessage = $_.Exception.Message
    Write-Host $ErrorMessage
    exit 1
}