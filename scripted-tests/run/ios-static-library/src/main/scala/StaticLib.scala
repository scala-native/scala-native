// Fixture of scripts/ci/ios-static-lib/run.sh
// Scala Native code built as a static library and used from a foreign host
// program, see https://github.com/scala-native/scala-native/issues/4334
import java.nio.charset.StandardCharsets
import java.{util => ju}

import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

object StaticLib {

  /** Allocation heavy, deterministic transformation of the input. The result is
   *  allocated using malloc and needs to be released using `sn_release`.
   */
  @exported("sn_transform")
  def transform(input: CString, seed: CInt): CString = {
    val in = fromCString(input)
    val random = new ju.Random(seed.toLong)
    val entries = new ju.TreeMap[String, ju.List[String]]()
    var i = 0
    while (i < 256) {
      val key = s"$in-${random.nextInt(64)}"
      val values = entries.computeIfAbsent(key, _ => new ju.ArrayList[String]())
      values.add(
        new String(
          Array.fill(1 + random.nextInt(32))('a' + i % 26).map(_.toChar)
        )
      )
      i += 1
    }
    var checksum = 0L
    entries.forEach { (key, values) =>
      checksum = checksum * 31 + key.hashCode()
      values.forEach(v => checksum = checksum * 31 + v.hashCode())
    }
    val bytes = s"$in:${entries.size()}:$checksum"
      .getBytes(StandardCharsets.UTF_8)
    val result = stdlib.malloc((bytes.length + 1).toCSize)
    i = 0
    while (i < bytes.length) {
      result(i) = bytes(i)
      i += 1
    }
    result(bytes.length) = 0.toByte
    result
  }

  @exported("sn_release")
  def release(result: CString): Unit = stdlib.free(result)
}
