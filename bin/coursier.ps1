# Windows version of coursier

$COURSIER="$PSScriptRoot\coursier.jar"

#$url = "https://github.com/coursier/coursier/raw/master/coursier"
$url = "https://git.io/vgvpD"

Try
{
    $coursierExists = Test-Path $COURSIER
    if ($coursierExists -ne $True)
    {
        #Invoke-WebRequest -Uri $url -OutFile $COURSIER
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

    Try {
        $old_ErrorActionPreference = $ErrorActionPreference
        $ErrorActionPreference = 'SilentlyContinue'
        $log = (&java -disableassertions -dsa -jar $COURSIER $args 2>&1) -join "`n"
        $ErrorActionPreference = $old_ErrorActionPreference
        Write-Output $log
    } Catch {
        $ErrorMessage = $_.Exception.Message
        Write-Output $ErrorMessage
    }

}
Catch
{
    $ErrorMessage = $_.Exception.Message
    Write-Output $ErrorMessage
    exit 1
}