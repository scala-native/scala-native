package java.security

trait PrivilegedAction[T] {
  def run(): T
}
