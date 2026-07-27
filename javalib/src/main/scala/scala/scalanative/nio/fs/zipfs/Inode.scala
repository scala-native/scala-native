package scala.scalanative.nio.fs.zipfs

import java.util.zip.ZipEntry

/* Per-entry state held by a writable ZipFileSystem mount. Insertion
 * order is preserved by the LinkedHashMap that owns these.
 *
 * `gpFlags` lives on the inode (not on the cloned ZipEntry) so that
 * copy/move don't need to mutate ZipEntry's flag-bit surface. At
 * rewrite time ZipWriter reads gpFlags from the inode and emits LFH
 * bits accordingly; we always strip bit 3 (data descriptor) since
 * Modified entries pre-deflate before writing the LFH.
 */
private[zipfs] sealed trait Inode

private[zipfs] object Inode {

  /** Entry that has not been rewritten this session. The compressed payload is
   *  streamed at rewrite time via
   *  `ZipFile.getRawInputStream(sourceArchiveName)`. `cachedEntry` holds the
   *  metadata (method/sizes/crc/dos-time/extra/comment) we use to rebuild
   *  LFH+CDH; its name may differ from `sourceArchiveName` after copy/move so
   *  that the rewritten archive carries the target name.
   */
  final case class Original(
      sourceArchiveName: String,
      cachedEntry: ZipEntry,
      gpFlags: Int
  ) extends Inode

  /** Entry created or rewritten this session. `uncompressedBytes` is the user
   *  payload pre-compression; ZipWriter deflates (or stores) it and computes
   *  CRC + sizes at rewrite time.
   */
  final case class Modified(
      uncompressedBytes: Array[Byte],
      cachedEntry: ZipEntry,
      gpFlags: Int
  ) extends Inode

  /** Tombstone — the entry has been deleted this session. Suppresses the inode
   *  from directory streams, attribute reads, and the rewrite loop.
   */
  case object Deleted extends Inode
}
