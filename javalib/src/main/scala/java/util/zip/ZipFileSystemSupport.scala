package java.util.zip

import java.io.InputStream

/** Scala Native internal bridge between `java.util.zip` and the zip file
 *  system in `scala.scalanative.nio.fs.zipfs`.
 *
 *  The zip file system rewrites archives by raw-copying unchanged entries
 *  and needs access to a few pieces of low-level state (general-purpose
 *  flag bits, raw MS-DOS time fields, raw compressed payloads, extra-field
 *  helpers) that the JDK keeps module-internal between `java.util.zip` and
 *  `jdk.zipfs`. Scala Native has no module system, so this object exposes
 *  exactly that surface in one place instead of widening the public API of
 *  `ZipEntry`/`ZipFile`.
 *
 *  This is NOT part of the JDK API. User code must not rely on it.
 */
object ZipFileSystemSupport {

  /** General-purpose-flag bits captured when `entry` was read from a central
   *  directory; 0 for entries created via `new ZipEntry(name)`.
   */
  def getGpFlags(entry: ZipEntry): Int = entry.gpFlags

  /** Raw MS-DOS time field as encoded in the LFH/CDH (5 bits hour, 6 bits
   *  minute, 5 bits 2-second). -1 if no time has been set. Pairs with
   *  [[getDosDate]] for re-emission of the encoded pair without re-running
   *  the timezone-sensitive epoch conversion.
   */
  def getDosTime(entry: ZipEntry): Int = entry.time

  /** Raw MS-DOS date field as encoded in the LFH/CDH (7 bits year-1980,
   *  4 bits month, 5 bits day). -1 if no date has been set.
   */
  def getDosDate(entry: ZipEntry): Int = entry.modDate

  /** See `ZipFile.getRawInputStream`: the compressed payload bytes of the
   *  named entry, with the local file header parsed and skipped and no
   *  inflater wrapping; `null` for an unknown name.
   */
  def getRawInputStream(zipFile: ZipFile, entryName: String): InputStream =
    zipFile.getRawInputStream(entryName)

  /** See `ZipEntry.buildUTExtraBlock`. */
  def buildUTExtraBlock(
      entry: ZipEntry,
      includeAtimeCtime: Boolean
  ): Array[Byte] =
    ZipEntry.buildUTExtraBlock(entry, includeAtimeCtime)

  /** See `ZipEntry.mergeExtra`. */
  def mergeExtra(existing: Array[Byte], addition: Array[Byte]): Array[Byte] =
    ZipEntry.mergeExtra(existing, addition)

  /** See `ZipEntry.stripTimestampBlocks`. */
  def stripTimestampBlocks(extra: Array[Byte]): Array[Byte] =
    ZipEntry.stripTimestampBlocks(extra)
}
