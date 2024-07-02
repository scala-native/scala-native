// Ported from Scala.js, commit SHA: 5df5a4142 dated: 2020-09-06
package java.util.function

@FunctionalInterface
trait Supplier[T] {
  def get(): T
}
