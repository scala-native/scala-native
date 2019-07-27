package java.util.function

trait Supplier[T] {
  def get(): T
}
