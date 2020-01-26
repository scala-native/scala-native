package java.nio.file.attribute

trait FileAttribute[T] {
  def name(): String
  def value: T
}
