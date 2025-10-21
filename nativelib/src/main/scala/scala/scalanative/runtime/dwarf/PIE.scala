package scala.scalanative.runtime.dwarf

import java.io._
import java.lang.{Long => JLong}

import scala.annotation.tailrec

object PIE {
  private case class MapsRow(lowAddress: Long, highAddress: Long, offset: Long)

  // parses the /proc/self/maps file which contains offsets for virtual memory regions
  private val mapsFile: scala.Array[MapsRow] = {
    val f = new File("/proc/self/maps")
    val builder = scala.Array.newBuilder[MapsRow]

    val reader: BufferedReader = new BufferedReader(new FileReader(f))
    try {
      var line: String = null
      while ({
        line = reader.readLine();
        line != null
      }) {
        val firstSpace = line.indexOf(' ')
        val dashIndex = line.indexOf('-')

        val lowAddress =
          JLong.parseUnsignedLong(line.substring(0, dashIndex), 16)
        val highAddress =
          JLong.parseUnsignedLong(line.substring(dashIndex + 1, firstSpace), 16)

        val secondSpace = line.indexOf(' ', firstSpace + 1)
        val thirdSpace = line.indexOf(' ', secondSpace + 1)
        val offset =
          JLong.parseUnsignedLong(
            line.substring(secondSpace + 1, thirdSpace),
            16
          )

        builder += MapsRow(
          lowAddress = lowAddress,
          highAddress = highAddress,
          offset = offset
        )
      }
    } finally {
      reader.close()
    }

    builder.result()
  }

  def virtualAddress(address: Long): Long = {
    val length = mapsFile.length
    var i = 0
    var found = false
    var result = address
    while (!found && i < length) {
      val row = mapsFile(i)
      if (row.lowAddress <= address && address <= row.highAddress) {
        result = address - row.lowAddress + row.offset
        found = true
      }
      i += 1
    }
    result
  }
}
