#
# Records performance data for the command (could be application executable).
#
# Params:
# -command <Command to run, or path to executable>
# -etl <path for output etl file, default "out.etl">
#
# Usage: perfApp c:\myprogram.exe myprogram.etl
#
param (
    [Parameter(Mandatory=$true)][string]$command = "",
    [string]$etl = "out.etl"
 )
&wpr -start CPU
Measure-Command { &$command }
&wpr -stop $etl