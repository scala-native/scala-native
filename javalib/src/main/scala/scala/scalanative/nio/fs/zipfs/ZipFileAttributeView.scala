package scala.scalanative.nio.fs.zipfs

import java.nio.file.attribute.{
  BasicFileAttributeView, BasicFileAttributes, FileTime
}
import java.util.{HashMap, Map}

/** `BasicFileAttributeView` over a `ZipPath`. */
private[zipfs] final class ZipFileAttributeView(
    provider: ZipFileSystemProvider,
    path: ZipPath
) extends BasicFileAttributeView {

  override def name(): String = "basic"

  override def readAttributes(): BasicFileAttributes =
    provider.readZipAttributes(path)

  override def setTimes(
      lastModifiedTime: FileTime,
      lastAccessTime: FileTime,
      createTime: FileTime
  ): Unit = {
    if (lastModifiedTime != null)
      provider.setAttribute(
        path,
        "basic:lastModifiedTime",
        lastModifiedTime,
        Array.empty
      )
    if (lastAccessTime != null)
      provider.setAttribute(
        path,
        "basic:lastAccessTime",
        lastAccessTime,
        Array.empty
      )
    if (createTime != null)
      provider.setAttribute(
        path,
        "basic:creationTime",
        createTime,
        Array.empty
      )
  }

  override def setAttribute(name: String, value: Object): Unit =
    provider.setAttribute(path, name, value, Array.empty)

  // Extended javalib API: `Files.getAttribute(path, "basic:xxx")` and
  // `Files.readAttributes(path, "basic:*")` both go through
  // `FileAttributeView.asMap` on Native. The default impl returns an empty
  // HashMap, which would make `Files.size(zipPath)` etc. silently return
  // null. Populate it with the standard BasicFileAttributes fields.
  override def asMap: Map[String, Object] = {
    val a = readAttributes()
    val m = new HashMap[String, Object]()
    m.put("lastModifiedTime", a.lastModifiedTime())
    m.put("lastAccessTime", a.lastAccessTime())
    m.put("creationTime", a.creationTime())
    m.put("size", java.lang.Long.valueOf(a.size()))
    m.put("isRegularFile", java.lang.Boolean.valueOf(a.isRegularFile()))
    m.put("isDirectory", java.lang.Boolean.valueOf(a.isDirectory()))
    m.put("isSymbolicLink", java.lang.Boolean.valueOf(a.isSymbolicLink()))
    m.put("isOther", java.lang.Boolean.valueOf(a.isOther()))
    m.put("fileKey", a.fileKey())
    m
  }
}
