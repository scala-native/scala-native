# Windows version of coursier

$COURSIER="$PSScriptRoot/.coursier.jar"

#$url = "https://github.com/coursier/coursier/raw/master/coursier"
$url = "https://git.io/vgvpD"

Try
{
    $coursierExists = Test-Path $COURSIER
    if ($coursierExists -ne $True)
    {
        (new-object System.Net.WebClient).DownloadFile(
            $url,
            $COURSIER
        )
        $coursierExists = Test-Path $COURSIER
        if ($coursierExists -ne $True)
        {
            throw [System.IO.FileNotFoundException] "$COURSIER not found."
        }
    }
}
Catch
{
    $ErrorMessage = $_.Exception.Message
    Write-Output $ErrorMessage
    exit 1
}

&java -jar $COURSIER $args