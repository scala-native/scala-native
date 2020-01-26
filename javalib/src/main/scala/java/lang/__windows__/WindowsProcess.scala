package java.lang
import java.util.concurrent.TimeUnit

private[lang] class WindowsProcess private (builder: ProcessBuilder) extends Process {
    override def destroy(): Unit = {
        throw new UnsupportedOperationException("Not implemented")
    }
    override def destroyForcibly(): Process = {
        throw new UnsupportedOperationException("Not implemented")
        null
    }
    override def exitValue(): Int = {
        throw new UnsupportedOperationException("Not implemented")
        0
    }
    override def getErrorStream(): java.io.InputStream = {
        throw new UnsupportedOperationException("Not implemented")
        null
    }
    override def getInputStream(): java.io.InputStream = {
        throw new UnsupportedOperationException("Not implemented")
        null
    }
    override def getOutputStream(): java.io.OutputStream = {
        throw new UnsupportedOperationException("Not implemented")
        null
    }
    override def isAlive(): scala.Boolean = {
        throw new UnsupportedOperationException("Not implemented")
        false
    }
    override def waitFor(timeout: scala.Long, unit: TimeUnit): scala.Boolean = {
        throw new UnsupportedOperationException("Not implemented")
        false
    }
    override def waitFor(): scala.Int = {
        throw new UnsupportedOperationException("Not implemented")
        0
    }
}

object WindowsProcess
{
    def apply(builder: ProcessBuilder): Process = {
        val msg = "No windows implementation of java.lang.Process"
        throw new UnsupportedOperationException(msg)
    }
}