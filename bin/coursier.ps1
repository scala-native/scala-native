# Windows version of coursier

$SN = "$env:USERPROFILE\.scala-native"
$COURSIER="$SN\.coursier.jar"

#$url = "https://github.com/coursier/coursier/raw/master/coursier"
$url = "https://git.io/vgvpD"

Try
{
    $snExists = Test-Path $SN
    if ($snExists -eq $False)
    {
        New-Item -ItemType Directory -Force -Path $SN
    }
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

    &java -jar $COURSIER $args
}
Catch
{
    $ErrorMessage = $_.Exception.Message
    Write-Output $ErrorMessage
    exit 1
}