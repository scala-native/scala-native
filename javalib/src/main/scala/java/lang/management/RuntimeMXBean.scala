package java.lang.management

trait RuntimeMXBean {

  /** Returns the process ID of the running virtual machine (process).
   *
   *  @since 10
   */
  def getPid(): Long

  /** Returns the name of the running virtual machine (process).
   */
  def getName(): String

  /** Returns the name of the virtual machine implementation.
   *
   *  The equivalent of `sys.props("java.vm.name")`.
   */
  def getVmName(): String

  /** Returns the vendor of the virtual machine implementation.
   *
   *  The equivalent of `sys.props("java.vm.vendor")`.
   */
  def getVmVendor(): String

  /** Returns the version of the virtual machine implementation.
   *
   *  The equivalent of `sys.props("java.vm.version")`.
   */
  def getVmVersion(): String

  /** Returns the name of the virtual machine specification.
   *
   *  The equivalent of `sys.props("java.vm.specification.name")`.
   */
  def getSpecName(): String

  /** Returns the vendor of the virtual machine specification.
   *
   *  The equivalent of `sys.props("java.vm.specification.vendor")`.
   */
  def getSpecVendor(): String

  /** Returns the version of the virtual machine specification.
   *
   *  The equivalent of `sys.props("java.vm.specification.version")`.
   */
  def getSpecVersion(): String

  /** Returns the uptime of the virtual machine (process) in milliseconds.
   */
  def getUptime(): Long

  /** Returns the start time of the virtual machine (process) in milliseconds.
   */
  def getStartTime(): Long

  /** Returns a map of names and values of the system properties whose name and
   *  value is represented as a string.
   */
  def getSystemProperties(): java.util.Map[String, String]
}

object RuntimeMXBean {

  private[management] def apply(): RuntimeMXBean =
    new Impl

  private class Impl extends RuntimeMXBean {
    import scala.scalanative.posix._
    import scala.scalanative.unsafe._
    import scala.scalanative.unsigned._

    def getPid(): Long =
      unistd.getpid()

    def getName(): String = {
      val pid = getPid()

      // InetAddress.getLocalHost returns IP (e.g. /127.0.0.1) while we need the name
      val hostname = {
        val hostNameLength = unistd._SC_HOST_NAME_MAX.toCSize
        val hostName = stackalloc[Byte](hostNameLength)
        if (unistd.gethostname(hostName, hostNameLength) == 0) {
          fromCString(hostName)
        } else {
          "localhost"
        }
      }

      pid + "@" + hostname
    }

    def getVmName(): String =
      System.getProperty("java.vm.name")

    def getVmVendor(): String =
      System.getProperty("java.vm.vendor")

    def getVmVersion(): String =
      System.getProperty("java.vm.version")

    def getSpecName(): String =
      System.getProperty("java.vm.specification.name")

    def getSpecVendor(): String =
      System.getProperty("java.vm.specification.vendor")

    def getSpecVersion(): String =
      System.getProperty("java.vm.specification.version")

    def getUptime(): Long =
      scalanative.runtime.uptime

    def getStartTime(): Long =
      scalanative.runtime.startTime

    def getSystemProperties(): java.util.Map[String, String] = {
      val result = new java.util.HashMap[String, String]()
      System.getProperties().forEach { (key, value) =>
        (key, value) match {
          case (k: String, v: String) => result.put(k, v)
          case _                      =>
        }
      }
      result
    }
  }

}
