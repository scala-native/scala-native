package java.security

// Ported from Harmony

object AccessController {
  def doPrivileged[T](action: PrivilegedAction[T]): T =
    action.run()
}
