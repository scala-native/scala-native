package java.util

class Random {
  def this(seed: Long) = this()

  def next(bits: scala.Int): scala.Int     = ???
  def nextBoolean(): scala.Boolean         = ???
  def nextBytes(bytes: Array[Byte]): Unit  = ???
  def nextDouble(): scala.Double           = ???
  def nextFloat(): scala.Float             = ???
  def nextGaussian(): scala.Double         = ???
  def nextInt(): scala.Int                 = ???
  def nextInt(bound: scala.Int): scala.Int = ???
  def nextLong(): scala.Long               = ???
  def setSeed(seed: scala.Long): Unit      = ???
}

object Random
