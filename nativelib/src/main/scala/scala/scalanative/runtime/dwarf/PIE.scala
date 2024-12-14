package scala.scalanative.runtime.dwarf

import java.lang.{Long => JLong}
import java.io._

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

        val lowAddress = JLong.parseLong(line.substring(0, dashIndex), 16)
        val highAddress =
          JLong.parseLong(line.substring(dashIndex + 1, firstSpace), 16)

        val secondSpace = line.indexOf(' ', firstSpace + 1)
        val thirdSpace = line.indexOf(' ', secondSpace + 1)
        val offset =
          JLong.parseLong(line.substring(secondSpace + 1, thirdSpace), 16)

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

  def absoluteAddress(address: Long): Long = {
    val length = mapsFile.length
    @tailrec
    def binarySearch(from: Int, to: Int): Long = {
      if (from < 0) binarySearch(0, to)
      else if (to > length) binarySearch(from, length)
      else if (to <= from)
        // Not found
        address
      else {
        val idx = from + (to - from - 1) / 2
        val row = mapsFile(idx)
        if (row.lowAddress <= address && address <= row.highAddress)
          // Found
          address - row.lowAddress + row.offset
        else if (address < row.lowAddress) binarySearch(from, idx)
        else // row.highAddress < address
          binarySearch(idx + 1, to)
      }
    }
    binarySearch(0, length)
  }
}
