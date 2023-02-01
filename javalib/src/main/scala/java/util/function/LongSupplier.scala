// Ported from Scala.js, commit SHA: db63dabed dated: 2020-10-06
package java.util.function

@FunctionalInterface
trait LongSupplier {
  def getAsLong(): Long
}
