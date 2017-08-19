package java.lang

class InheritableThreadLocal[T] extends ThreadLocal[T] {
  def childValue(parentValue: T): T = parentValue
}
