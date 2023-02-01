// Ported from Scala.js, commit sha:db63dabed dated:2020-10-06
package java.util.function

@FunctionalInterface
trait DoubleSupplier {
  def getAsDouble(): Double
}