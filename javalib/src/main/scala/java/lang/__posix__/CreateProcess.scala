package java.lang

object CreateProcess
{
    def apply(builder: ProcessBuilder): Process = UnixProcess(builder)
}