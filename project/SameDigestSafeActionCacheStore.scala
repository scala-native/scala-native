package build

import java.nio.file.{Files, Path}

import sbt.util.{
  AbstractActionCacheStore,
  Digest,
  DiskActionCacheStore,
  GetActionResultRequest,
  UpdateActionResultRequest
}
import xsbti.{FileConverter, HashedVirtualFileRef, VirtualFile}

/** Disk ActionCache that does not rewrite same-digest outputs.
 *
 * Upstream [[DiskActionCacheStore]] replaces regular files with CAS symlinks when
 * symlink creation works. On Windows that delete+replace fails if the compiler
 * still holds an `exportJars` plugin jar open (`AccessDeniedException` on
 * `packageBin`).
 */
final class SameDigestSafeActionCacheStore(
    disk: DiskActionCacheStore,
    converter: FileConverter
) extends AbstractActionCacheStore {
  override def storeName: String = disk.storeName

  override def get(request: GetActionResultRequest) = disk.get(request)
  override def put(request: UpdateActionResultRequest) = disk.put(request)
  override def putBlobs(blobs: Seq[VirtualFile]) = disk.putBlobs(blobs)
  override def findBlobs(refs: Seq[HashedVirtualFileRef]) = disk.findBlobs(refs)

  override def syncBlobs(
      refs: Seq[HashedVirtualFileRef],
      outputDirectory: Path
  ): Seq[Path] = {
    val (needsSync, upToDate) = refs.partition { ref =>
      val path = resolve(ref, outputDirectory)
      !Files.exists(path) || !Digest.sameDigest(path, Digest(ref))
    }
    upToDate.map(resolve(_, outputDirectory)) ++
      disk.syncBlobs(needsSync, outputDirectory)
  }

  private def resolve(ref: HashedVirtualFileRef, outputDirectory: Path): Path =
    converter.toPath(ref) match {
      case p if p.isAbsolute => p
      case p                 => outputDirectory.resolve(p)
    }
}
