package scala.scalanative.testinterface.signalhandling

import scala.scalanative.meta.LinktimeInfo._
import scala.scalanative.libc.stdlib._
import scala.scalanative.libc.signal._
import scala.scalanative.libc.string._
import scala.scalanative.posix.unistd._
import scala.scalanative.runtime.unwind
import scala.scalanative.unsafe._
import scalanative.unsigned._
import scala.scalanative.windows._
import scala.scalanative.runtime.SymbolFormatter

private[testinterface] object SignalConfig {

  /* StackTrace.currentStackTrace had to be rewritten to accomodate using
   * only async-signal-safe methods. Because of that, printf was replaced
   * with write/WriteFile, and only stack allocations were used.
   * While it is unknown if windows' WriteFile is async-signal-safe here,
   * the fact that the function is called synchronously suggests so.
   * Unfortunately, Windows does not provide specification on
   * async-signal-safe methods the way POSIX does.
   */
  private def asyncSafePrintStackTrace(sig: CInt): Unit = {
    val errorTag = c"[\u001b[0;31merror\u001b[0;0m]"

    def printError(str: CString): Unit =
      if (isWindows) {
        val written = stackalloc[DWord]()
        FileApi.WriteFile(
          ConsoleApiExt.stdErr,
          str,
          (sizeof[CChar] * strlen(str).toULong).toUInt,
          written,
          null
        )
      } else {
        write(
          STDERR_FILENO,
          str,
          sizeof[CChar] * strlen(str)
        )
      }

    def signalToCString(str: CString, signal: Int): Unit = {
      val reversedStr: Ptr[CChar] = stackalloc[CChar](8)
      var index = 0
      var signalPart = signal
      while (signalPart > 0) {
        val digit = signalPart % 10
        reversedStr(index) = (digit + '0').toByte
        index += 1
        signalPart = signalPart / 10
      }
      reversedStr(index) = 0.toByte
      for (i <- 0 until index) {
        str(i) = reversedStr(index - 1 - i)
      }
      str(index) = 0.toByte
    }

    val signalNumberStr: Ptr[CChar] =
      if (!isWindows) {
        import scala.scalanative.posix.string.strsignal
        strsignal(sig)
      } else {
        val str: Ptr[CChar] = stackalloc[CChar](8)
        signalToCString(str, sig)
        str
      }

    val stackTraceHeader: Ptr[CChar] = stackalloc[CChar](100)
    strcat(stackTraceHeader, errorTag)
    strcat(stackTraceHeader, c" Fatal signal ")
    strcat(stackTraceHeader, signalNumberStr)
    strcat(stackTraceHeader, c" caught\n")
    printError(stackTraceHeader)

    val cursor = stackalloc[Byte](unwind.sizeOfCursor)
    val context = stackalloc[Byte](unwind.sizeOfContext)
    unwind.get_context(context)
    unwind.init_local(cursor, context)

    while (unwind.step(cursor) > 0) {
      val offset = stackalloc[Long]()
      val pc = stackalloc[CSize]()
      unwind.get_reg(cursor, unwind.UNW_REG_IP, pc)
      if (!pc == 0.toUInt) return
      val symMax = 1024
      val sym: Ptr[CChar] = stackalloc[CChar](symMax)
      if (unwind.get_proc_name(
            cursor,
            sym,
            sizeof[CChar] * symMax.toUInt,
            offset
          ) == 0) {
        sym(symMax - 1) = 0.toByte
        val className: Ptr[CChar] = stackalloc[CChar](512)
        val methodName: Ptr[CChar] = stackalloc[CChar](512)
        SymbolFormatter.asyncSafeFromSymbol(sym, className, methodName)

        val formattedSymbol: Ptr[CChar] = stackalloc[CChar](1100)
        formattedSymbol(0) = 0.toByte
        strcat(formattedSymbol, errorTag)
        strcat(formattedSymbol, c"   at ")
        strcat(formattedSymbol, className)
        strcat(formattedSymbol, c".")
        strcat(formattedSymbol, methodName)
        strcat(formattedSymbol, c"(Unknown Source)\n")
        printError(formattedSymbol)
      }
    }
  }

  def setDefaultHandlers(): Unit = {

    /* Default handler for signals like SIGSEGV etc.
     * Only async-signal-safe methods can be used here.
     * Since it needs to be able to handle segmentation faults,
     * it has to exit the program on call, otherwise it will
     * keep being called indefinetely. In bash programs,
     * exitcode > 128 signifies a fatal signal n, where n = exitcode - 128.
     * This is the convention used here.
     */
    val defaultHandler = CFuncPtr1.fromScalaFunction { (sig: CInt) =>
      asyncSafePrintStackTrace(sig)
      exit(128 + sig)
    }

    val YELLOW = "\u001b[0;33m"
    val RESET = "\u001b[0;0m"

    def setHandler(sig: CInt): Unit = {
      if (signal(sig, defaultHandler) == SIG_ERR)
        Console.err.println(
          s"[${YELLOW}warn${RESET}] Could not set default handler for signal ${sig}"
        )
    }

    // Only these select signals can work on Windows
    setHandler(SIGABRT)
    setHandler(SIGFPE)
    setHandler(SIGILL)
    setHandler(SIGTERM)
    if (!isMultithreadingEnabled || isMac) {
      // Used in GC traps, MacOS uses SIGBUS instead
      setHandler(SIGSEGV)
    }

    if (!isWindows) {
      import scala.scalanative.posix.signal._
      if (!isMultithreadingEnabled || !isMac) {
        // Used in Immix GC traps on MacOS
        setHandler(SIGBUS)
      }
      setHandler(SIGALRM)
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
      // Boehm GC and None GC are the only GCs without weak reference support
      if (!isMultithreadingEnabled || isWeakReferenceSupported) {
        // Used by Boehm GC StopTheWorld signal handlers
        setHandler(SIGXCPU)
        setHandler(SIGXFSZ)
      }
    }
  }
}
