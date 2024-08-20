package java.lang.management

trait OperatingSystemMXBean {

  /** Returns the name of the operating system.
   *
   *  The equivalent of `sys.props("os.name")`.
   */
  def getName(): String

  /** Returns the architecture of the operating system.
   *
   *  The equivalent of `sys.props("os.arch")`.
   */
  def getArch(): String

  /** Returns the version of the operating system.
   *
   *  The equivalent of `sys.props("os.version")`.
   *
   *  @note
   *    the `null` will be returned on Windows and MacOS platforms
   */
  def getVersion(): String

  /** Returns the number of processors available to the process, never smaller
   *  than one.
   *
   *  The equivalent of `Runtime.getRuntime().availableProcessors()`.
   */
  def getAvailableProcessors(): Int

}

object OperatingSystemMXBean {

  private[management] def apply(): OperatingSystemMXBean =
    new Impl

  private class Impl extends OperatingSystemMXBean {
    def getName(): String =
      System.getProperty("os.name")

    def getArch(): String =
      System.getProperty("os.arch")

    def getVersion(): String =
      System.getProperty("os.version")

    def getAvailableProcessors(): Int =
      Runtime.getRuntime().availableProcessors()
  }

}
