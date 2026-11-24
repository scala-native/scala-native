import java.io.File
import java.nio.file.attribute.{AclEntry, AclEntryType, AclFileAttributeView}
import java.nio.file.{Files => NioFiles}

// Shared by java-io-file-1 and java-io-file-2, copied in by scriptedDependencies.
// Restores permissions changed by the native tests so that scripted can remove
// its temporary directories.
object JavaIoFilePermissionCleanup {
  def restore(dir: File): Unit = {
    val files = Option(dir.listFiles()).toSeq.flatten
    files.foreach { file =>
      // Native setters on Windows add DENY ACL entries, which JVM setters cannot undo.
      Option(
        NioFiles.getFileAttributeView(
          file.toPath,
          classOf[AclFileAttributeView]
        )
      ).foreach { view =>
        val acl = new java.util.ArrayList[AclEntry](view.getAcl())
        val isDeny: java.util.function.Predicate[AclEntry] =
          _.`type`() == AclEntryType.DENY
        if (acl.removeIf(isDeny)) view.setAcl(acl)
      }
      file.setReadable(true, false)
      file.setWritable(true, false)
      file.setExecutable(true, false)
    }
  }
}
