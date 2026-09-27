package org.scalanative.testsuite.javalib.util.concurrent

/** Item constants for ConcurrentSkipList tests.
 *
 *  These shadow JSR166Test's Integer `zero`/`one`/... values. Companions that
 *  extend this trait must not `import JSR166Test._`, or those names become
 *  ambiguous.
 */
trait ConcurrentSkipListItems {
  val zero: Item = JSR166Test.itemFor(0)
  val one: Item = JSR166Test.itemFor(1)
  val two: Item = JSR166Test.itemFor(2)
  val three: Item = JSR166Test.itemFor(3)
  val four: Item = JSR166Test.itemFor(4)
  val five: Item = JSR166Test.itemFor(5)
  val six: Item = JSR166Test.itemFor(6)
  val seven: Item = JSR166Test.itemFor(7)
  val eight: Item = JSR166Test.itemFor(8)
  val nine: Item = JSR166Test.itemFor(9)
  val ten: Item = JSR166Test.itemFor(10)
  val minusOne: Item = JSR166Test.itemFor(-1)
  val minusTwo: Item = JSR166Test.itemFor(-2)
  val minusThree: Item = JSR166Test.itemFor(-3)
  val minusFour: Item = JSR166Test.itemFor(-4)
  val minusFive: Item = JSR166Test.itemFor(-5)
  val minusSix: Item = JSR166Test.itemFor(-6)
  val minusSeven: Item = JSR166Test.itemFor(-7)
  val minusEight: Item = JSR166Test.itemFor(-8)
  val minusNine: Item = JSR166Test.itemFor(-9)
  val minusTen: Item = JSR166Test.itemFor(-10)
}
