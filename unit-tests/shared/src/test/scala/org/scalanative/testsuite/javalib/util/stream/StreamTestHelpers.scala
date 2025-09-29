package org.scalanative.testsuite.javalib.util.stream

import java.util.Spliterator

import org.junit.Assume._

object StreamTestHelpers {

  // spliterator and, presumably, stream characteristic names
  private val maskNames = Map(
    0x00000001 -> "DISTINCT",
    0x00000004 -> "SORTED",
    0x00000010 -> "ORDERED",
    0x00000040 -> "SIZED",
    0x00000100 -> "NONNULL",
    0x00000400 -> "IMMUTABLE",
    0x00001000 -> "CONCURRENT",
    0x00004000 -> "SUBSIZED"
  )

  private def maskToName(mask: Int): String =
    maskNames.getOrElse(mask, s"0x${mask.toHexString.toUpperCase}")

  def verifyCharacteristics[T](
      splItr: Spliterator[T],
      requiredPresent: Seq[Int],
      requiredAbsent: Seq[Int]
  ): Unit = {
    /* The splItr.hasCharacteristics() and splItr.characteristics()
     * sections both seek the same information: Does the spliterator report
     * the required characteristics and no other. They ask the question
     * in slightly different ways to exercise each of the two Spliterator
     * methods. The answers should match, belt & suspenders.
     */

    for (rp <- requiredPresent) {
      assert(
        splItr.hasCharacteristics(rp),
        s"missing requiredPresent characteristic: ${maskToName(rp)}"
      )
    }

    for (rp <- requiredAbsent) {
      assert(
        !splItr.hasCharacteristics(rp),
        s"found requiredAbsent characteristic: ${maskToName(rp)}"
      )
    }

    val sc = splItr.characteristics()
    val requiredPresentMask = requiredPresent.fold(0)((x, y) => x | y)

    val unknownBits = sc & ~requiredPresentMask
    val unknownBitsMsg = s"0X${unknownBits.toHexString}"
    assert(
      0 == unknownBits,
      s"unexpected characteristics, unknown mask: ${unknownBitsMsg}"
    )
  }

  def requireJDK8CompatibleCharacteristics(): Unit = {

    val defaultVersion = 8
    val defaultVersionString = "1.8" // a.k.a. Java 8

    val jvmVersionString =
      System.getProperty("java.version", s"${defaultVersion}")

    /* This parse is lazy in the sense of developer lazy & easier to get right.
     * It is reasonably robust but not fool-proof. Feel free to do better.
     */

    val parseFailMsg = s"Could not parse java.version: ${jvmVersionString}"

    val elements = jvmVersionString.split('.')

    assumeTrue(
      parseFailMsg,
      elements.size >= 1
    )

    val jvmVersion =
      try {
        val selected =
          if (elements(0) == "1") 1 // e.g. "1.8" a.k.a. Java 8
          else 0 // e.g. 17.0.7
        elements(selected).toInt
      } catch {
        case _: NumberFormatException =>
          assumeTrue(parseFailMsg, false)
          defaultVersion // Should never reach here, keep compiler happy.
      }

    /* Java is _almost_ always backward compatible.  It appears that this
     * is not the case for the characteristics returned by the stream
     * limit() methods for parallel ORDERED streams. See Issue #Issue #3309
     * and the code in the *StreamImpl.scala files.
     *
     * Somewhere after Java 8 and before or in Java 17.0.7 the
     * characteristics of parallel ORDERED streams changed to add SIZED.
     * The change is not in Java 11.
     * A complication is that it may or not be in various patch version of a
     * JVM. That is, Java 17.0.7 has it but 17.0.0 might not. Here the
     * assumption is that 17.0.0 has it.
     *
     * A person with too much time on their hands and access to a wide
     * range of JDKs could narrow the bounds.  As long as Scala Native
     * JDK only describes itself as supporting Java 8, this is good enough.
     */
    val inbounds = (jvmVersion >= 8) && (jvmVersion < 17)

    assumeTrue(
      "Tests of stream limit methods require Java >= 8 and < 17",
      inbounds
    )
  }

}
