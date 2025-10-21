package java.util.random

import java.math.BigInteger
import java.util.stream.Stream
import java.util.{HashMap, Random, SplittableRandom}
import java.{lang => jl, util => ju}

private object L32X64MixRandomFactory
    extends RandomGeneratorFactory[RandomGenerator.SplittableGenerator] {

  type T = RandomGenerator.SplittableGenerator

  override def create(): T =
    new L32X64MixRandom()

  override def create(seed: Array[scala.Byte]): T = {
    val requiredLength = 16
    val seedLength = seed.length

    if (seed.length != requiredLength)
      throw new jl.IllegalArgumentException(
        RandomGenFSupport.unsupportedSeedLengthMsg(requiredLength, seedLength)
      )

    new L32X64MixRandom(seed)
  }

  override def create(seed: scala.Long): T =
    new L32X64MixRandom(seed)

  override def equidistribution(): Int = 1

  override def group(): String = "LXM"

  override def isArbitrarilyJumpable(): Boolean = false

  override def isDeprecated(): Boolean = false

  override def isHardware(): Boolean = false

  override def isJumpable(): Boolean = false

  override def isLeapable(): Boolean = false

  override def isSplittable(): Boolean = true

  override def isStatistical(): Boolean = true

  override def isStochastic(): Boolean = false

  override def isStreamable(): Boolean = true

  override def name(): String = "L32X64MixRandom"

  override def period(): BigInteger =
    BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE).shiftLeft(32)

  override def stateBits(): Int = 96
}

private object L64X128MixRandomFactory
    extends RandomGeneratorFactory[RandomGenerator.SplittableGenerator] {

  type T = RandomGenerator.SplittableGenerator

  override def create(): T =
    new L64X128MixRandom()

  override def create(seed: Array[scala.Byte]): T = {
    val requiredLength = 32
    val seedLength = seed.length

    if (seed.length != requiredLength)
      throw new jl.IllegalArgumentException(
        RandomGenFSupport.unsupportedSeedLengthMsg(requiredLength, seedLength)
      )

    new L64X128MixRandom(seed)
  }

  override def create(seed: scala.Long): T =
    new L64X128MixRandom(seed)

  override def equidistribution(): Int = 2

  override def group(): String = "LXM"

  override def isArbitrarilyJumpable(): Boolean = false

  override def isDeprecated(): Boolean = false

  override def isHardware(): Boolean = false

  override def isJumpable(): Boolean = false

  override def isLeapable(): Boolean = false

  override def isSplittable(): Boolean = true

  override def isStatistical(): Boolean = true

  override def isStochastic(): Boolean = false

  override def isStreamable(): Boolean = true

  override def name(): String = "L64X128MixRandom"

  override def period(): BigInteger =
    BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE).shiftLeft(64)

  override def stateBits(): Int = 192
}

// Factory for java.util.Random instances
private[util] object JuRandomFactory
    extends RandomGeneratorFactory[RandomGenerator] {

  type T = RandomGenerator

  override def create(): T =
    new ju.Random()

  /* java.util.Random has no public seed Array[Byte] constructor.
   * The specified behavior changed in JDK 23. JDK [17, 22] specified
   * silently falling back to using the zero argument constructor.
   * JDK 23 specifies throwing this Exception.
   *
   * Scala Native JuRandomFactory & SplittableRandomFactory alway follows the
   * safer JDK 23 specification.
   */
  override def create(seed: Array[scala.Byte]): T =
    throw new UnsupportedOperationException(
      RandomGenFSupport.unsupportedArrayByteSeedMsg(name())
    )

  override def create(seed: scala.Long): T =
    new ju.Random(seed.toInt)

  override def equidistribution(): Int = 0

  override def group(): String = "Legacy"

  override def isArbitrarilyJumpable(): Boolean = false

  override def isDeprecated(): Boolean = false

  override def isHardware(): Boolean = false

  override def isJumpable(): Boolean = false

  override def isLeapable(): Boolean = false

  override def isSplittable(): Boolean = false

  override def isStatistical(): Boolean = true

  override def isStochastic(): Boolean = false

  override def isStreamable(): Boolean = false

  override def name(): String = "Random"

  override def period(): BigInteger =
    BigInteger.ONE.shiftLeft(48)

  override def stateBits(): Int = 48
}

private[util] object SplittableRandomFactory
    extends RandomGeneratorFactory[RandomGenerator.SplittableGenerator] {

  type T = RandomGenerator.SplittableGenerator

  override def create(): T =
    new ju.SplittableRandom()

  /* java.util.Splittable Random has no public seed Array[Byte] constructor.
   * See comments above corresponding method in JuRandomFactory.
   */
  override def create(seed: Array[scala.Byte]): T =
    throw new UnsupportedOperationException(
      RandomGenFSupport.unsupportedArrayByteSeedMsg(name())
    )

  override def create(seed: scala.Long): T =
    new ju.SplittableRandom(seed)

  override def equidistribution(): Int = 1

  override def group(): String = "Legacy"

  override def isArbitrarilyJumpable(): Boolean = false

  override def isDeprecated(): Boolean = false

  override def isHardware(): Boolean = false

  override def isJumpable(): Boolean = false

  override def isLeapable(): Boolean = false

  override def isSplittable(): Boolean = true

  override def isStatistical(): Boolean = true

  override def isStochastic(): Boolean = false

  override def isStreamable(): Boolean = true

  override def name(): String = "SplittableRandom"

  override def period(): BigInteger =
    BigInteger.ONE.shiftLeft(64)

  override def stateBits(): Int = 64
}

private object Xoroshiro128PlusPlusRandomFactory
    extends RandomGeneratorFactory[RandomGenerator.LeapableGenerator] {

  type T = RandomGenerator.LeapableGenerator

  override def create(): T =
    new Xoroshiro128PlusPlus()

  override def create(seed: Array[scala.Byte]): T = {
    val requiredLength = 16
    val seedLength = seed.length

    if (seed.length != requiredLength)
      throw new jl.IllegalArgumentException(
        RandomGenFSupport.unsupportedSeedLengthMsg(requiredLength, seedLength)
      )

    new Xoroshiro128PlusPlus(seed)
  }

  override def create(seed: scala.Long): T = {
    val expandedSeed = RandomSupport.getVignaInitialState2x64(seed)
    new Xoroshiro128PlusPlus(expandedSeed)
  }

  override def equidistribution(): Int = 1

  override def group(): String = "Xoroshiro"

  override def isArbitrarilyJumpable(): Boolean = false

  override def isDeprecated(): Boolean = false

  override def isHardware(): Boolean = false

  override def isJumpable(): Boolean = true

  override def isLeapable(): Boolean = true

  override def isSplittable(): Boolean = false

  override def isStatistical(): Boolean = true

  override def isStochastic(): Boolean = false

  override def isStreamable(): Boolean = true

  override def name(): String = "Xoroshiro128PlusPlus"

  override def period(): BigInteger =
    BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE)

  override def stateBits(): Int = 128
}

private object Xoshiro256PlusPlusRandomFactory
    extends RandomGeneratorFactory[RandomGenerator.LeapableGenerator] {

  type T = RandomGenerator.LeapableGenerator

  override def create(): T =
    new Xoshiro256PlusPlus()

  override def create(seed: Array[scala.Byte]): T = {
    val requiredLength = 32
    val seedLength = seed.length

    if (seed.length != requiredLength)
      throw new jl.IllegalArgumentException(
        RandomGenFSupport.unsupportedSeedLengthMsg(requiredLength, seedLength)
      )

    new Xoshiro256PlusPlus(seed)
  }

  override def create(seed: scala.Long): T = {
    val expandedSeed = RandomSupport.getVignaInitialState4x64(seed)
    new Xoshiro256PlusPlus(expandedSeed)
  }

  override def equidistribution(): Int = 3

  override def group(): String = "Xoshiro"

  override def isArbitrarilyJumpable(): Boolean = false

  override def isDeprecated(): Boolean = false

  override def isHardware(): Boolean = false

  override def isJumpable(): Boolean = true

  override def isLeapable(): Boolean = true

  override def isSplittable(): Boolean = false

  override def isStatistical(): Boolean = true

  override def isStochastic(): Boolean = false

  override def isStreamable(): Boolean = true

  override def name(): String = "Xoshiro256PlusPlus"

  override def period(): BigInteger =
    BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE)

  override def stateBits(): Int = 256
}

class RandomGeneratorFactory[T <: RandomGenerator] protected () extends Object {

  /** Java defines this class as abstract, as would a proper SPI. The class is
   *  implemented as concrete to allow progress to be made but the
   *  implementation may change to be closer to the JDK in future Scala Native
   *  versions.
   *
   *  Define the base class in such a way that it is likely to break, and break
   *  both obviously and quickly, if someone tries to instantiate it without
   *  sub-classing it.
   */

  def create(): T = null.asInstanceOf[T]

  def create(seed: Array[scala.Byte]): T = null.asInstanceOf[T]

  def create(seed: scala.Long): T = null.asInstanceOf[T]

  def equidistribution(): Int = -1

  def group(): String = "Invalid"

  def isArbitrarilyJumpable(): Boolean = false

  def isDeprecated(): Boolean = false

  def isHardware(): Boolean = false

  def isJumpable(): Boolean = false

  def isLeapable(): Boolean = false

  def isSplittable(): Boolean = false

  def isStatistical(): Boolean = false

  def isStochastic(): Boolean = false

  def isStreamable(): Boolean = false

  def name(): String = "Invalid"

  def period(): BigInteger = BigInteger.ZERO

  def stateBits(): Int = -1
}

object RandomGeneratorFactory {
  /* The Legacy group is: "Random", "SecureRandom", "SplittableRandom".
   * Scala Native does not support "SecureRandom" in "java.security",
   * so also not here.
   *
   * JEP 356 ThreadLocalRandom is, as before, available only through
   * 'ThreadLocal.current()'. Per JDK, it is not available via
   * RandomGeneratorFactory.
   *
   * The two L*MixRandomFactory entries are Splittable.
   * The two Xo*PlusPlusRandomFactory entires are Leapable, hence Jumpable.
   *
   * Other missing algorithms are works-in-progress.
   */

  private def initializeBuiltinFactories(
      map: HashMap[String, RandomGeneratorFactory[RandomGenerator]]
  ): Unit = {

    val builtinFactories = ju.List.of(
      L32X64MixRandomFactory,
      L64X128MixRandomFactory,
      JuRandomFactory,
      SplittableRandomFactory,
      Xoroshiro128PlusPlusRandomFactory,
      Xoshiro256PlusPlusRandomFactory
    )

    builtinFactories.forEach(factory => {
      map.put(
        factory.name(),
        factory.asInstanceOf[RandomGeneratorFactory[RandomGenerator]]
      )
    })
  }

  private lazy val knownFactories = {
    val hm = new HashMap[String, RandomGeneratorFactory[RandomGenerator]]()
    initializeBuiltinFactories(hm)
    hm
  }

  def all(): Stream[RandomGeneratorFactory[RandomGenerator]] = {
    knownFactories
      .values()
      .stream()
      .filter(impl => !impl.isDeprecated())
  }

  def getDefault(): RandomGeneratorFactory[RandomGenerator] = {
    /* per JVM, August 2024:
     *  "The default implementation selects L32X64MixRandom."
     *  Be aware default is subject to change with Java versions.
     *
     * Java 23 documentation does not name the default algorithm.
     * RandomGeneratorFactory.getDefault().name() still returns
     * L32X64MixRandom.
     */
    RandomGeneratorFactory.of("L32X64MixRandom")
  }

  def of[T <: RandomGenerator](name: String): RandomGeneratorFactory[T] = {
    ju.Objects.requireNonNull(name)

    val impl = knownFactories.get(name)

    if (impl == null)
      throw new IllegalArgumentException(
        RandomGenFSupport.unsupportedAlgorithmMsg(name)
      )

    impl.asInstanceOf[RandomGeneratorFactory[T]]
  }
}

private[util] object RandomGenFSupport {

  private val doubleQuote = '\u0022' // Scala 2 awkwardness

  def unsupportedAlgorithmMsg(algName: String): String = {
    "No implementation of the random number generator algorithm " +
      s"${doubleQuote}${algName}${doubleQuote}" +
      " is available"
  }

  def unsupportedArrayByteSeedMsg(algName: String): String = {
    "Random algorithm" +
      s"${doubleQuote}${algName}${doubleQuote}" +
      " does not support an Array[Byte] seed"
  }

  def unsupportedSeedLengthMsg(
      requiredLength: Int,
      seedLength: Int
  ): String =
    s"seed Array length expected: ${requiredLength} got: ${seedLength}"

}
