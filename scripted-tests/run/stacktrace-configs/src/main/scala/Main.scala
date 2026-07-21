import scala.scalanative.meta.LinktimeInfo

/** Test that stack traces work correctly across different build configurations.
 *
 *  This tests both:
 *    1. Eager stack trace capture (Windows, 32-bit platforms)
 *    2. Lazy stack trace materialization (64-bit POSIX) where:
 *       - fillInStackTrace() captures raw instruction pointers
 *       - getStackTrace() materializes them to StackTraceElements later
 *
 *  Symbol resolution varies by configuration:
 *    - With source-level debugging: DWARF-based symbol lookup
 *    - Without source-level debugging: dladdr (POSIX) / SymFromAddrW (Windows)
 *
 *  In debug mode: all methods should be present in stack trace In release mode:
 *  LLVM may inline functions, so only key methods are required
 *
 *  Note: On Windows in release mode without debug symbols, mangled symbol names
 *  are not preserved in the executable. Only C-level names like "main" are
 *  available, so we can only verify that stack traces are captured, not that
 *  they contain properly resolved Scala method names.
 */
object Main {
  // Windows in release mode without source-level debugging has limited symbol info
  private def hasLimitedSymbolInfo: Boolean =
    LinktimeInfo.isWindows &&
      LinktimeInfo.releaseMode &&
      !LinktimeInfo.sourceLevelDebuging.generateFunctionSourcePositions
  def main(args: Array[String]): Unit = {
    printConfiguration()
    testBasicStackTrace()
    testExceptionStackTrace()
    println("All stack trace tests passed!")
  }

  def printConfiguration(): Unit = {
    println("=== Stack Trace Test Configuration ===")
    println(s"  OS: ${if (LinktimeInfo.isLinux) "Linux"
      else if (LinktimeInfo.isMac) "macOS"
      else if (LinktimeInfo.isWindows) "Windows"
      else "Other"}")
    println(s"  Architecture: ${if (LinktimeInfo.is32BitPlatform) "32-bit"
      else "64-bit"}")
    println(s"  Debug mode: ${LinktimeInfo.debugMode}")
    println(s"  Release mode: ${LinktimeInfo.releaseMode}")
    println(
      s"  Source-level debugging: ${LinktimeInfo.sourceLevelDebuging.generateFunctionSourcePositions}"
    )
    println("======================================")
  }

  @noinline def testBasicStackTrace(): Unit = {
    println("\nTesting basic stack trace capture...")
    level1()
  }

  @noinline def level1(): Unit = level2()
  @noinline def level2(): Unit = level3()
  @noinline def level3(): Unit = verifyStackTrace()

  @noinline def verifyStackTrace(): Unit = {
    val error = new Error("test")
    val stacktrace = error.getStackTrace()

    // Filter to only our Scala test methods (Main$ or Main.)
    val ourMethods = stacktrace.filter(_.getClassName.startsWith("Main"))

    println(s"Captured ${ourMethods.length} frames from Main:")
    ourMethods.foreach(m => println(s"  $m"))

    if (hasLimitedSymbolInfo) {
      // On Windows release mode without debug symbols, we can only verify
      // that stack traces are captured, not that symbols are properly resolved
      println(
        "Note: Limited symbol info on Windows release mode without debug symbols"
      )
      assert(
        stacktrace.nonEmpty,
        "Expected non-empty stack trace"
      )
      // Should at least have some frames with "main" method name
      val hasMainFrames = stacktrace.exists(_.getMethodName == "main")
      assert(
        hasMainFrames,
        s"Expected at least 'main' in stack trace, got:\n${stacktrace.map(e => s"  ${e.getClassName}.${e.getMethodName}").mkString("\n")}"
      )
    } else {
      // Key test: Verify that frames with "Main" class have proper resolution
      // (not "<none>" which would indicate broken symbol lookup)
      val mainClassNames = ourMethods.map(_.getClassName).distinct
      assert(
        mainClassNames.forall(c => c == "Main$" || c == "Main"),
        s"Expected class names to be 'Main$$' or 'Main', got: ${mainClassNames.mkString(", ")}"
      )

      // In debug mode all methods should be present; in release mode LLVM may inline
      val expectedMethods =
        if (LinktimeInfo.debugMode)
          Set(
            "verifyStackTrace",
            "level3",
            "level2",
            "level1",
            "testBasicStackTrace",
            "main"
          )
        else Set("verifyStackTrace")

      val ourMethodNames = ourMethods.map(_.getMethodName).toSet
      val missingMethods = expectedMethods -- ourMethodNames
      assert(
        missingMethods.isEmpty,
        s"Expected methods missing: ${missingMethods.mkString(", ")}\n" +
          s"Found: ${ourMethodNames.mkString(", ")}\n" +
          s"Full stack trace:\n${stacktrace.map(e => s"  ${e.getClassName}.${e.getMethodName}").mkString("\n")}"
      )

      // Verify the C entry point exists (it's <none>.main, not filtered into ourMethods)
      val hasCEntryPoint = stacktrace.exists(e =>
        e.getClassName == "<none>" && e.getMethodName == "main"
      )
      assert(
        hasCEntryPoint,
        s"Expected 'main' C entry point in stack trace, got:\n${stacktrace.map(e => s"  ${e.getClassName}.${e.getMethodName}").mkString("\n")}"
      )
    }

    println("Basic stack trace test passed!")
  }

  @noinline def testExceptionStackTrace(): Unit = {
    println("\nTesting exception stack trace...")
    try {
      throwingMethod()
      assert(false, "Expected exception was not thrown")
    } catch {
      case e: RuntimeException =>
        val stacktrace = e.getStackTrace()

        // Filter to our Scala methods
        val ourMethods = stacktrace.filter(_.getClassName.startsWith("Main"))

        println(
          s"Exception stack trace (${ourMethods.length} frames from Main):"
        )
        ourMethods.foreach(m => println(s"  $m"))

        if (hasLimitedSymbolInfo) {
          // On Windows release mode without debug symbols, we can only verify
          // that stack traces are captured
          println(
            "Note: Limited symbol info on Windows release mode without debug symbols"
          )
          assert(
            stacktrace.nonEmpty,
            "Expected non-empty exception stack trace"
          )
        } else {
          // Key test: All Main frames should have proper class names (not "<none>")
          val mainClassNames = ourMethods.map(_.getClassName).distinct
          assert(
            mainClassNames.forall(c => c == "Main$" || c == "Main"),
            s"Expected class names to be 'Main$$' or 'Main', got: ${mainClassNames.mkString(", ")}"
          )

          // In debug mode all methods should be present; in release mode LLVM may inline
          val expectedMethods =
            if (LinktimeInfo.debugMode)
              Set("throwingMethod", "testExceptionStackTrace", "main")
            else Set("throwingMethod")

          val ourMethodNames = ourMethods.map(_.getMethodName).toSet
          val missingMethods = expectedMethods -- ourMethodNames
          assert(
            missingMethods.isEmpty,
            s"Expected methods missing: ${missingMethods.mkString(", ")}\n" +
              s"Found: ${ourMethodNames.mkString(", ")}\n" +
              s"Full stack trace:\n${stacktrace.map(e => s"  ${e.getClassName}.${e.getMethodName}").mkString("\n")}"
          )
        }

        println("Exception stack trace test passed!")
    }
  }

  @noinline def throwingMethod(): Unit = {
    throw new RuntimeException("Test exception for stack trace verification")
  }
}
