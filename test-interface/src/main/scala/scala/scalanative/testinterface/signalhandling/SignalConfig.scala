package scala.scalanative.testinterface.signalhandling

import scalanative.runtime.Platform._
import scala.scalanative.libc.stdlib._
import scala.scalanative.libc.signal._
import scala.scalanative.unsafe._

private[testinterface] object SignalConfig {
  def setDefaultHandlers(): Unit = {

    /* Default handler for signals like SIGSEGV etc.
     * Since it needs to be able to handle segmentation faults, it has to
     * exit the program on call, otherwise it will keep being called indefinetely.
     * This and IO operations in handlers leading to unspecified behavior mean we
     * can only communicate through exitcode values. In bash programs,
     * exitcode > 128 signifies a fatal signal n, where n = exitcode - 128.
     * This is the convention used here.
     */
    val defaultHandler = CFuncPtr1.fromScalaFunction { (sig: CInt) =>
      exit(128 + sig)
    }

    def YELLOW = "\u001b[0;33m"
    def RESET = "\u001b[0;0m"

    def setHandler(sig: CInt): Unit = {
      if (signal(sig, defaultHandler) == SIG_ERR)
        println(
          s"[${YELLOW}warn${RESET}] Could not set default handler for signal ${sig}"
        )
    }

    // Only these select "signals" can work on Windows
    setHandler(SIGABRT)
    setHandler(SIGFPE)
    setHandler(SIGILL)
    setHandler(SIGSEGV)
    setHandler(SIGTERM)

    if (isLinux() || isMac()) {
      import scala.scalanative.posix.signal._
      setHandler(SIGALRM)
      setHandler(SIGBUS)
      setHandler(SIGHUP)
      setHandler(SIGPIPE)
      setHandler(SIGQUIT)
      setHandler(SIGTTIN)
      setHandler(SIGTTOU)
      setHandler(SIGUSR1)
      setHandler(SIGUSR2)
      setHandler(SIGPROF)
      setHandler(SIGSYS)
      setHandler(SIGTRAP)
      setHandler(SIGVTALRM)
      setHandler(SIGXCPU)
      setHandler(SIGXFSZ)
    }
  }
}
