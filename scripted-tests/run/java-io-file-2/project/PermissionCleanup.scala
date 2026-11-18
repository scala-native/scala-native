import java.io.File
import java.nio.file.attribute.{AclEntry, AclFileAttributeView}
import java.nio.file.{Files => NioFiles}

object PermissionCleanup {
  import Files._

  private val files = Seq(
    unreadableFile,
    unreadableDirectory,
    unexecutableFile,
    unexecutableDirectory,
    unwritableFile,
    unwritableDirectory,
    willBeSetReadOnlyFile,
    willBeSetReadOnlyDirectory,
    willBeSetExecutableFile,
    willBeSetExecutableDirectory,
    willBeSetReadableFile,
    willBeSetReadableDirectory,
    willBeSetWritableFile,
    willBeSetWritableDirectory
  )

  private var originalAcls = Map.empty[File, java.util.List[AclEntry]]
  private var saved = false

  def save(): Unit = {
    originalAcls = files.flatMap { file =>
      Option(
        NioFiles.getFileAttributeView(
          file.toPath,
          classOf[AclFileAttributeView]
        )
      ).map { view =>
        file -> new java.util.ArrayList[AclEntry](view.getAcl())
      }
    }.toMap
    saved = true
  }

  def restore(): Unit = {
    require(saved, "setupTests must save permissions before cleanupTests")
    // Native permission tests add Windows deny ACLs; JVM File setters cannot undo them.
    originalAcls.foreach {
      case (file, acl) =>
        NioFiles
          .getFileAttributeView(file.toPath, classOf[AclFileAttributeView])
          .setAcl(acl)
    }
    files.filter(_.exists()).foreach { file =>
      file.setReadable(true, false)
      file.setWritable(true, false)
      file.setExecutable(true, false)
    }
  }
}
