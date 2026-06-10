package scala.scalanative.nio.fs.zipfs

import java.io.{ByteArrayInputStream, IOException, InputStream}
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.DirectoryStream.Filter
import java.nio.file._
import java.nio.file.attribute.{
  BasicFileAttributeView, BasicFileAttributes, FileAttribute, FileAttributeView,
  FileTime
}
import java.nio.file.spi.FileSystemProvider
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.{ZipEntry, ZipException, ZipFile, ZipFileSystemSupport}
import java.util.{ArrayList, LinkedHashMap, LinkedHashSet, Map}

/** `jar:`-scheme provider over zip/jar archives on the default file system.
 *
 *  Mounts are writable by default, matching jdk.zipfs: mutations are buffered
 *  in an in-memory inode map and the archive is rewritten atomically when the
 *  file system is closed. `accessMode=readOnly` opts into a read-only mount;
 *  `create=true` creates a missing archive.
 *
 *  On Scala Native, providers are discovered through `ServiceLoader`, which
 *  resolves implementations at link time: user builds must register
 *  `scala.scalanative.nio.fs.zipfs.ZipFileSystemProvider` for
 *  `java.nio.file.spi.FileSystemProvider` via `NativeConfig.serviceProviders`.
 */
class ZipFileSystemProvider extends FileSystemProvider {
  import ZipFileSystemProvider.{AccessModeOpt, OpenMode, ParsedCopyOptions}

  override def getScheme(): String = "jar"

  override def newFileSystem(uri: URI, env: Map[String, _]): FileSystem = {
    val (archive, _) = ZipUtils.parseJarUri(uri)
    // URI overload: the caller explicitly asked for a `jar:` mount. Wrong
    // magic is a real "this isn't a zip" error, NOT the "provider declines
    // this path" signal — so raise ZipException instead of the UOE the path
    // overload uses for probing.
    openZipFS(
      archive,
      env,
      onWrongMagic = p => throw new ZipException(s"Not a zip archive: $p")
    )
  }

  override def newFileSystem(path: Path, env: Map[String, _]): FileSystem =
    // Path overload: used by FileSystems.newFileSystem(Path, ClassLoader)
    // probing across providers. Wrong magic must be UOE so the dispatcher
    // advances to the next provider instead of treating it as fatal.
    openZipFS(
      path,
      env,
      onWrongMagic =
        p => throw new UnsupportedOperationException(s"Not a zip archive: $p")
    )

  private def openZipFS(
      path: Path,
      env: Map[String, _],
      onWrongMagic: Path => Nothing
  ): FileSystem = {
    val canonical = ZipUtils.canonicalize(path)

    // env-key parsing. Matches jdk.zipfs: mounts are writable by default;
    // `accessMode=readOnly` opts the caller into read-only. `create=true`
    // (string or boolean) skips the existence/magic checks for a missing
    // archive and never downgrades to read-only.
    val createIfMissing = ZipUtils.envFlag(env, "create")
    val accessMode = parseAccessMode(env)
    val defaultCompressionMethod = parseCompressionMethod(env)

    val exists = Files.exists(canonical, Array.empty[LinkOption])

    if (!exists && !createIfMissing)
      throw new java.nio.file.NoSuchFileException(canonical.toString)

    if (exists && !ZipUtils.looksLikeZip(canonical)) onWrongMagic(canonical)

    val isWritable = accessMode match {
      case AccessModeOpt.ReadOnly  => false
      case AccessModeOpt.ReadWrite => true
      case AccessModeOpt.Default   => true
    }

    // Magic ok — any ZipFile parse failure beyond this point is a corrupt
    // archive and propagates as ZipException (subclass of IOException).
    val source: Option[ZipFile] =
      if (exists) Some(new ZipFile(canonical.toFile()))
      else None

    val fs = new ZipFileSystem(
      this,
      canonical,
      source,
      isWritable,
      defaultCompressionMethod
    )

    if (isWritable) {
      // Populate inode map from source archive entries (insertion-order
      // preserved by LinkedHashMap). Reject unsupported gp-flag bits or
      // compression methods at mount time, before any mutation happens.
      source.foreach { zf =>
        val it = zf.entries()
        while (it.hasMoreElements()) {
          val e = it.nextElement()
          validateForWritable(e)
          // Bit 11 (UTF-8 names) is fine; bit 3 (data descriptor) is
          // stripped — sizes/crc are populated on the cached entry by
          // ZipFile when reading from the central directory, so the
          // rewrite path can emit LFHs without bit 3.
          val gpFlags = ZipFileSystemSupport.getGpFlags(e) & ~0x0008
          fs.inodes.put(
            e.getName(),
            Inode.Original(e.getName(), e, gpFlags)
          )
        }
      }
      if (!exists) {
        // Empty mount — force close-rewrite to emit a valid empty EOCD
        // even if the caller never mutates anything.
        fs.dirty = true
      }
    }

    val prev = ZipFileSystemProvider.registry.putIfAbsent(canonical, fs)
    if (prev != null) {
      fs.closeQuietly()
      throw new FileSystemAlreadyExistsException(canonical.toString)
    }
    fs
  }

  // Reject entries we can't safely re-emit. Read-only mounts skip this
  // because they never rewrite.
  private def validateForWritable(e: ZipEntry): Unit = {
    val flags = ZipFileSystemSupport.getGpFlags(e)
    // Only bit 3 (data descriptor, stripped on rewrite) and bit 11
    // (UTF-8 names) are supported for writable mounts.
    val mask = 0x0800 | 0x0008
    if ((flags & ~mask) != 0) {
      throw new java.io.IOException(
        s"Unsupported entry flags 0x${Integer.toHexString(flags)}: ${e.getName()}"
      )
    }
    val m = e.getMethod()
    if (m != ZipEntry.STORED && m != ZipEntry.DEFLATED) {
      throw new java.io.IOException(
        s"Unsupported compression method $m for entry ${e.getName()}"
      )
    }
  }

  private def parseAccessMode(env: Map[String, _]): AccessModeOpt = {
    if (env == null) AccessModeOpt.Default
    else {
      val v = env.get("accessMode")
      if (v == null) AccessModeOpt.Default
      else {
        val s = String.valueOf(v)
        // jdk.zipfs (JDK 23+) spelling: "readOnly" / "readWrite".
        if ("readOnly" == s) AccessModeOpt.ReadOnly
        else if ("readWrite" == s) AccessModeOpt.ReadWrite
        else
          throw new IllegalArgumentException(
            s"Unsupported ZipFS accessMode: $s (expected readOnly|readWrite)"
          )
      }
    }
  }

  private def parseCompressionMethod(env: Map[String, _]): Int = {
    if (env == null) ZipEntry.DEFLATED
    else {
      val v = env.get("compressionMethod")
      if (v == null) ZipEntry.DEFLATED
      else {
        val s = String.valueOf(v)
        if ("STORED" == s) ZipEntry.STORED
        else if ("DEFLATED" == s) ZipEntry.DEFLATED
        else
          throw new IllegalArgumentException(
            s"Unsupported ZipFS compressionMethod: $s"
          )
      }
    }
  }

  override def getFileSystem(uri: URI): FileSystem = {
    val (archive, _) = ZipUtils.parseJarUri(uri)
    val canonical = ZipUtils.canonicalize(archive)
    val fs = ZipFileSystemProvider.registry.get(canonical)
    if (fs == null)
      throw new FileSystemNotFoundException(canonical.toString)
    fs
  }

  // Called by ZipFileSystem.close() to unregister.
  private[zipfs] def unregister(canonical: Path, fs: ZipFileSystem): Unit = {
    ZipFileSystemProvider.registry.remove(canonical, fs)
  }

  override def getPath(uri: URI): Path = {
    // `jar:file:/foo.jar!/inner/path` — split into archive + entry, look
    // up the already-open ZipFileSystem in the registry, and ask it for
    // the entry path. The underlying fs must already be open (via
    // newFileSystem) — we don't auto-open here.
    //
    // jdk.zipfs requires the `!/` separator and rejects a bare archive
    // URI ("URI ... does not contain path info"). Mirror that to stay
    // compatible with jar-URI consumers.
    val ssp = if (uri == null) null else uri.getRawSchemeSpecificPart()
    val (archive, entry) = ZipUtils.parseJarUri(uri)
    if (ssp != null && ssp.lastIndexOf("!/") < 0)
      throw new IllegalArgumentException(
        s"URI does not contain entry path (expected jar:<uri>!/<entry>): $uri"
      )
    val canonical = ZipUtils.canonicalize(archive)
    val fs = ZipFileSystemProvider.registry.get(canonical)
    if (fs == null)
      throw new FileSystemNotFoundException(canonical.toString)
    // `entry` is the raw (still percent-encoded) tail of the SSP after
    // `!/`. ZipPath stores entry names as decoded `String`s, so decode
    // here. We must use URI path-decoding (percent only) rather than
    // URLDecoder (form encoding, which also turns `+` into ` `) — entry
    // names may legitimately contain `+`.
    val decoded =
      if (entry.isEmpty) "/"
      else ZipUtils.decodeUriPath(entry)
    fs.getPath(decoded, Array.empty[String])
  }

  override def newByteChannel(
      path: Path,
      options: java.util.Set[_ <: OpenOption],
      attrs: Array[FileAttribute[_]]
  ): SeekableByteChannel = {
    val zp = asZipPath(path)
    val fs = zp.getFileSystem().asInstanceOf[ZipFileSystem]
    fs.ensureOpen()
    val mode = parseOpenOptions(options)
    if (!mode.writeMode) {
      val data = readEntryBytes(zp)
      new ZipSeekableByteChannel(data)
    } else {
      if (!fs.isWritable) throw new ReadOnlyFileSystemException()
      val entryName = toEntryName(zp)
      if (entryName.isEmpty)
        throw new IOException(s"Is a directory: ${zp}")
      if (isDirectoryPath(zp))
        throw new IOException(s"Is a directory: ${zp}")
      validateWritableParent(zp)

      val exists = existsFileEntry(zp)
      if (mode.createNew && exists)
        throw new FileAlreadyExistsException(zp.toString)
      if (!exists && !mode.create && !mode.createNew)
        throw new NoSuchFileException(zp.toString)

      val truncatesExisting =
        mode.write && !mode.readable && !mode.append ||
          mode.truncate && !mode.append
      val initial =
        if (!exists || truncatesExisting) new Array[Byte](0)
        else readEntryBytes(zp)
      val existingMeta = lookupLiveInode(fs, entryName) match {
        case Some(Inode.Original(_, e, _)) => e
        case Some(Inode.Modified(_, e, _)) => e
        case _                             => null
      }
      new ZipWritableByteChannel(
        initial,
        readable = mode.readable,
        writable = true,
        appendOnly = mode.append,
        deleteOnClose = mode.deleteOnClose,
        onClose = (bytes, deleteOnClose) => {
          if (deleteOnClose) {
            // Tombstone only if there is something to delete; a
            // CREATE_NEW + DELETE_ON_CLOSE channel whose entry never
            // existed must not dirty the mount with a no-op rewrite.
            if (lookupLiveInode(fs, entryName).exists(_ != Inode.Deleted)) {
              fs.inodes.put(entryName, Inode.Deleted)
              fs.dirty = true
            }
          } else {
            val meta =
              if (existingMeta != null) new ZipEntry(existingMeta)
              else {
                val e = new ZipEntry(entryName)
                e.setMethod(fs.defaultCompressionMethod)
                e.setTime(System.currentTimeMillis())
                e
              }
            fs.inodes.put(entryName, Inode.Modified(bytes, meta, 0))
            fs.dirty = true
          }
        }
      )
    }
  }

  override def newInputStream(
      path: Path,
      options: Array[OpenOption]
  ): InputStream = {
    val zp = asZipPath(path)
    val fs = zp.getFileSystem().asInstanceOf[ZipFileSystem]
    fs.ensureOpen()
    var i = 0
    while (i < options.length) { validateReadOption(options(i)); i += 1 }
    new ByteArrayInputStream(readEntryBytes(zp))
  }

  private def parseOpenOptions(
      options: java.util.Set[_ <: OpenOption]
  ): OpenMode = {
    if (options == null) throw new NullPointerException("options")
    var read = false
    var write = false
    var append = false
    var create = false
    var createNew = false
    var truncate = false
    var deleteOnClose = false
    val it = options.iterator()
    while (it.hasNext()) {
      it.next() match {
        case null                    => throw new NullPointerException("option")
        case StandardOpenOption.READ => read = true
        case StandardOpenOption.WRITE             => write = true
        case StandardOpenOption.APPEND            => append = true
        case StandardOpenOption.CREATE            => create = true
        case StandardOpenOption.CREATE_NEW        => createNew = true
        case StandardOpenOption.TRUNCATE_EXISTING => truncate = true
        case StandardOpenOption.DELETE_ON_CLOSE   => deleteOnClose = true
        case StandardOpenOption.SPARSE | StandardOpenOption.SYNC |
            StandardOpenOption.DSYNC =>
          ()
        case o =>
          throw new UnsupportedOperationException(
            s"ZipFS OpenOption not supported: $o"
          )
      }
    }

    val writeMode = write || append
    if (append && truncate)
      throw new IllegalArgumentException("APPEND conflicts with TRUNCATE")
    // jdk.zipfs does NOT enforce JDK FileChannel's
    // "TRUNCATE_EXISTING/CREATE without WRITE → IllegalArgumentException"
    // rule — it routes the call to the read path and surfaces
    // NoSuchFileException when the entry is absent. Match that to keep
    // cross-conformance tests green.

    OpenMode(
      writeMode = writeMode,
      readable = read,
      append = append,
      write = write,
      create = create,
      createNew = createNew,
      truncate = truncate,
      deleteOnClose = deleteOnClose
    )
  }

  // Read-only options are used by newInputStream. newByteChannel has its
  // own parser because it also supports writable modes.
  private def validateReadOption(o: OpenOption): Unit = o match {
    case null =>
      // NIO normally treats null elements in option collections/varargs as
      // programmer error (NPE), not "unsupported option" (UOE).
      throw new NullPointerException("option")
    case StandardOpenOption.READ => ()
    case StandardOpenOption.WRITE | StandardOpenOption.APPEND |
        StandardOpenOption.CREATE | StandardOpenOption.CREATE_NEW |
        StandardOpenOption.DELETE_ON_CLOSE |
        StandardOpenOption.TRUNCATE_EXISTING =>
      throw new UnsupportedOperationException(
        s"ZipFS is read-only; OpenOption not supported: $o"
      )
    case StandardOpenOption.SPARSE | StandardOpenOption.SYNC |
        StandardOpenOption.DSYNC =>
      () // no-ops for read.
    case _ =>
      throw new UnsupportedOperationException(
        s"ZipFS OpenOption not supported: $o"
      )
  }

  private def readEntryBytes(zp: ZipPath): Array[Byte] = {
    val fs = zp.getFileSystem().asInstanceOf[ZipFileSystem]
    val attrs = readZipAttributes(zp)
    if (attrs.isDirectory())
      throw new java.io.IOException(s"Is a directory: ${zp}")
    val entryName = toEntryName(zp)
    lookupLiveInode(fs, entryName) match {
      case Some(Inode.Modified(bytes, _, _))  => bytes.clone()
      case Some(Inode.Original(_, cached, _)) =>
        val in = fs.sourceZip.get.getInputStream(cached)
        try ZipSeekableByteChannel.slurp(in, cached.getSize())
        finally in.close()
      case _ =>
        val zf = fs.zipFile
        val entry = zf.getEntry(entryName)
        if (entry == null)
          throw new NoSuchFileException(zp.toString)
        val in = zf.getInputStream(entry)
        try ZipSeekableByteChannel.slurp(in, entry.getSize())
        finally in.close()
    }
  }

  private def existsFileEntry(zp: ZipPath): Boolean =
    try !readZipAttributes(zp).isDirectory()
    catch { case _: NoSuchFileException => false }

  private def isDirectoryPath(zp: ZipPath): Boolean =
    try readZipAttributes(zp).isDirectory()
    catch { case _: NoSuchFileException => false }

  private def validateWritableParent(zp: ZipPath): Unit = {
    val parent = zp.getParent()
    if (parent == null) return
    val fs = zp.getFileSystem().asInstanceOf[ZipFileSystem]
    val p = parent.asInstanceOf[ZipPath]
    val n = toEntryName(p)
    if (n.isEmpty) return
    lookupLiveInode(fs, n) match {
      case Some(Inode.Original(_, e, _)) if !e.isDirectory() =>
        throw new NoSuchFileException(zp.toString)
      case Some(Inode.Modified(_, e, _)) if !e.isDirectory() =>
        throw new NoSuchFileException(zp.toString)
      case Some(Inode.Deleted) =>
        throw new NoSuchFileException(zp.toString)
      case _ => ()
    }
    lookupLiveInode(fs, n + "/") match {
      case Some(Inode.Deleted) =>
        throw new NoSuchFileException(zp.toString)
      case _ => ()
    }
  }

  override def newDirectoryStream(
      dir: Path,
      filter: DirectoryStream.Filter[_ >: Path]
  ): DirectoryStream[Path] = {
    val zp = asZipPath(dir)
    val fs = zp.getFileSystem().asInstanceOf[ZipFileSystem]
    fs.ensureOpen()
    val a = readZipAttributes(zp) // throws NoSuchFileException
    if (!a.isDirectory())
      throw new NotDirectoryException(dir.toString)

    val parentEntry = toEntryName(zp)
    val prefix = if (parentEntry.isEmpty) "" else parentEntry + "/"

    // Collect unique immediate children (files + synthesised sub-dirs).
    val seen = new LinkedHashSet[String]()
    if (fs.isWritable) {
      val it = fs.inodes.entrySet().iterator()
      while (it.hasNext()) {
        val e = it.next()
        e.getValue() match {
          case Inode.Deleted => ()
          case _             => addDirectoryChild(seen, prefix, e.getKey())
        }
      }
    } else {
      val it = fs.zipFile.entries()
      while (it.hasMoreElements()) {
        addDirectoryChild(seen, prefix, it.nextElement().getName())
      }
    }

    // Resolve children against the *user-supplied* `dir`, not its absolute
    // form. JDK contract: "entries returned by the iterator are obtained
    // as if by resolving the name of the directory entry against dir". So
    // `Files.newDirectoryStream(fs.getPath("dir"))` must yield `dir/a`,
    // not `/dir/a`. Lookup inside the archive still uses the canonical
    // absolute name via `toEntryName` above.
    val raw = zp.toString
    val parentStr =
      if (raw.isEmpty) ""
      else if (raw == "/") "/"
      else raw + "/"
    val acc = new ArrayList[Path](seen.size())
    val sit = seen.iterator()
    while (sit.hasNext()) {
      val child = sit.next()
      val p: Path = fs.getPath(parentStr + child, Array.empty[String])
      if (filter == null || filter.accept(p)) acc.add(p)
    }
    new ZipDirectoryStream(acc)
  }

  override def createDirectory(
      dir: Path,
      attrs: Array[FileAttribute[_]]
  ): Unit = {
    val zp = asZipPath(dir)
    val fs = zp.getFileSystem().asInstanceOf[ZipFileSystem]
    fs.ensureOpen()
    if (!fs.isWritable) throw new ReadOnlyFileSystemException()
    val entryName = toEntryName(zp)
    if (entryName.isEmpty)
      throw new FileAlreadyExistsException(dir.toString)
    try {
      readZipAttributes(zp)
      throw new FileAlreadyExistsException(dir.toString)
    } catch {
      case _: NoSuchFileException => ()
    }
    validateWritableParent(zp)

    val name = entryName + "/"
    val e = new ZipEntry(name)
    e.setMethod(ZipEntry.STORED)
    e.setTime(System.currentTimeMillis())
    e.setSize(0L)
    e.setCompressedSize(0L)
    e.setCrc(0L)
    fs.inodes.put(name, Inode.Modified(new Array[Byte](0), e, 0))
    fs.dirty = true
  }

  override def delete(path: Path): Unit = {
    val zp = asZipPath(path)
    val fs = zp.getFileSystem().asInstanceOf[ZipFileSystem]
    fs.ensureOpen()
    if (!fs.isWritable) throw new ReadOnlyFileSystemException()
    val entryName = toEntryName(zp)
    if (entryName.isEmpty)
      throw new DirectoryNotEmptyException(path.toString)
    val key = existingInodeKey(fs, zp).getOrElse {
      // An implied directory can only exist because it has live descendants.
      if (hasPrefix(fs, entryName + "/"))
        throw new DirectoryNotEmptyException(path.toString)
      throw new NoSuchFileException(path.toString)
    }
    if (key.endsWith("/") && hasLiveChild(fs, key))
      throw new DirectoryNotEmptyException(path.toString)
    fs.inodes.put(key, Inode.Deleted)
    fs.dirty = true
  }

  override def copy(
      source: Path,
      target: Path,
      options: Array[CopyOption]
  ): Unit = {
    val src = asZipPath(source)
    val tgt = asZipPath(target)
    val fs = src.getFileSystem().asInstanceOf[ZipFileSystem]
    if (tgt.getFileSystem() ne fs) throw new ProviderMismatchException()
    fs.ensureOpen()
    if (!fs.isWritable) throw new ReadOnlyFileSystemException()
    val copyOptions = parseCopyOptions(options, isMove = false)
    val srcKey = existingInodeKey(fs, src).getOrElse {
      val srcName = toEntryName(src)
      if (srcName.nonEmpty && hasPrefix(fs, srcName + "/"))
        srcName + "/"
      else throw new NoSuchFileException(source.toString)
    }
    val targetKey = targetKeyForSource(tgt, srcKey.endsWith("/"))
    prepareTargetForWrite(fs, tgt, targetKey, copyOptions.replaceExisting)

    if (srcKey.endsWith("/") && !hasExplicitLiveInode(fs, srcKey)) {
      val e = newDirectoryEntry(targetKey)
      fs.inodes.put(targetKey, Inode.Modified(new Array[Byte](0), e, 0))
    } else {
      val srcInode = liveInode(fs, srcKey).getOrElse(
        throw new NoSuchFileException(source.toString)
      )
      fs.inodes.put(targetKey, cloneInode(srcInode, targetKey))
    }
    fs.dirty = true
  }

  override def move(
      source: Path,
      target: Path,
      options: Array[CopyOption]
  ): Unit = {
    val src = asZipPath(source)
    val tgt = asZipPath(target)
    val fs = src.getFileSystem().asInstanceOf[ZipFileSystem]
    if (tgt.getFileSystem() ne fs) throw new ProviderMismatchException()
    fs.ensureOpen()
    if (!fs.isWritable) throw new ReadOnlyFileSystemException()
    val copyOptions = parseCopyOptions(options, isMove = true)
    val srcName = toEntryName(src)
    if (srcName.isEmpty)
      throw new DirectoryNotEmptyException(source.toString)
    val srcKey = existingInodeKey(fs, src).getOrElse {
      if (hasPrefix(fs, srcName + "/")) srcName + "/"
      else throw new NoSuchFileException(source.toString)
    }
    val targetKey = targetKeyForSource(tgt, srcKey.endsWith("/"))
    if (srcKey == targetKey) return
    prepareTargetForWrite(fs, tgt, targetKey, copyOptions.replaceExisting)

    if (srcKey.endsWith("/")) moveDirectory(fs, srcKey, targetKey)
    else {
      val srcInode = liveInode(fs, srcKey).getOrElse(
        throw new NoSuchFileException(source.toString)
      )
      fs.inodes.put(targetKey, cloneInode(srcInode, targetKey))
      fs.inodes.put(srcKey, Inode.Deleted)
    }
    fs.dirty = true
  }

  override def isSameFile(path: Path, path2: Path): Boolean = {
    // Identity short-circuit.
    if (path eq path2) return true
    (path, path2) match {
      case (zp1: ZipPath, zp2: ZipPath) =>
        // Different ZipFileSystem (different archive) ⇒ never same file.
        if (zp1.getFileSystem() ne zp2.getFileSystem()) false
        else
          zp1.toAbsolutePath().normalize() ==
            zp2.toAbsolutePath().normalize()
      case _ =>
        // Mixed providers: per JDK contract, isSameFile compares files
        // across providers only if at least one side is from the default
        // FS and the other is on the same physical file. For two paths
        // on entirely unrelated providers (e.g. ZipPath vs UnixPath),
        // they are never the same file.
        false
    }
  }

  override def isHidden(path: Path): Boolean = false

  override def checkAccess(path: Path, modes: Array[AccessMode]): Unit = {
    val zp = asZipPath(path)
    val fs = zp.getFileSystem().asInstanceOf[ZipFileSystem]
    // existence check — throws NoSuchFileException if absent
    readZipAttributes(zp)
    var i = 0
    while (i < modes.length) {
      val m = modes(i)
      if (m == AccessMode.EXECUTE)
        throw new AccessDeniedException(path.toString)
      if (m == AccessMode.WRITE && !fs.isWritable)
        throw new AccessDeniedException(path.toString)
      i += 1
    }
  }

  override def getFileAttributeView[V <: FileAttributeView](
      path: Path,
      tpe: Class[V],
      options: Array[LinkOption]
  ): V = {
    val zp = asZipPath(path)
    if (tpe == classOf[BasicFileAttributeView] ||
        tpe == classOf[FileAttributeView])
      new ZipFileAttributeView(this, zp).asInstanceOf[V]
    else null.asInstanceOf[V]
  }

  override def readAttributes[A <: BasicFileAttributes](
      path: Path,
      tpe: Class[A],
      options: Array[LinkOption]
  ): A = {
    val zp = asZipPath(path)
    if (tpe == classOf[BasicFileAttributes] ||
        tpe == classOf[ZipFileAttributes])
      readZipAttributes(zp).asInstanceOf[A]
    else
      throw new UnsupportedOperationException(
        s"Unsupported attribute type for ZipFS: $tpe"
      )
  }

  override def readAttributes(
      path: Path,
      attributes: String,
      options: Array[LinkOption]
  ): Map[String, Object] = {
    val zp = asZipPath(path)
    val (view, attrs) = splitAttrSpec(attributes)
    if (view != "basic")
      throw new UnsupportedOperationException(
        s"View $view is not supported (only 'basic')"
      )
    val a = readZipAttributes(zp)
    val out = new LinkedHashMap[String, Object]()
    // Only "*" expands to the full attribute list; an empty attrs string
    // (e.g. `"basic:"`) yields an empty map, matching jdk.zipfs.
    val names: Array[String] =
      if (attrs == "*")
        Array(
          "lastModifiedTime",
          "lastAccessTime",
          "creationTime",
          "size",
          "isRegularFile",
          "isDirectory",
          "isSymbolicLink",
          "isOther",
          "fileKey"
        )
      else if (attrs.isEmpty) Array.empty[String]
      else attrs.split(",")
    // JDK zipfs silently ignores unrecognised attribute names in a
    // comma-separated list (returns only the recognised ones), so match
    // that — throwing IAE here would diverge from jdk.zipfs on calls like
    // `readAttributes(p, "basic:size,unknown")`.
    var i = 0
    while (i < names.length) {
      val n = names(i).trim
      n match {
        case "lastModifiedTime" => out.put(n, a.lastModifiedTime())
        case "lastAccessTime"   => out.put(n, a.lastAccessTime())
        case "creationTime"     => out.put(n, a.creationTime())
        case "size"             => out.put(n, java.lang.Long.valueOf(a.size()))
        case "isRegularFile"    =>
          out.put(n, java.lang.Boolean.valueOf(a.isRegularFile()))
        case "isDirectory" =>
          out.put(n, java.lang.Boolean.valueOf(a.isDirectory()))
        case "isSymbolicLink" =>
          out.put(n, java.lang.Boolean.valueOf(a.isSymbolicLink()))
        case "isOther" => out.put(n, java.lang.Boolean.valueOf(a.isOther()))
        case "fileKey" => out.put(n, a.fileKey())
        case _         => () // unknown — skip, matches jdk.zipfs
      }
      i += 1
    }
    out
  }

  override def setAttribute(
      path: Path,
      attribute: String,
      value: Object,
      options: Array[LinkOption]
  ): Unit = {
    val zp = asZipPath(path)
    val fs = zp.getFileSystem().asInstanceOf[ZipFileSystem]
    fs.ensureOpen()
    if (!fs.isWritable) throw new ReadOnlyFileSystemException()
    val (view, attrName) = splitAttrSpec(attribute)
    if (view != "basic")
      throw new UnsupportedOperationException(
        s"View $view is not supported (only 'basic')"
      )
    val ft = value match {
      case t: FileTime => t
      case null        => throw new NullPointerException("value")
      case _           =>
        throw new ClassCastException(
          s"Expected FileTime for attribute $attribute"
        )
    }
    val entryName = toEntryName(zp)
    val inode = lookupLiveInode(fs, entryName).getOrElse(
      throw new NoSuchFileException(zp.toString)
    )
    val updated = inode match {
      case Inode.Original(src, e, gp) =>
        val c = new ZipEntry(e)
        setTimeAttribute(c, attrName, ft)
        Inode.Original(src, c, gp)
      case Inode.Modified(bytes, e, gp) =>
        val c = new ZipEntry(e)
        setTimeAttribute(c, attrName, ft)
        Inode.Modified(bytes, c, gp)
      case Inode.Deleted =>
        throw new NoSuchFileException(zp.toString)
    }
    fs.inodes.put(entryName, updated)
    fs.dirty = true
  }

  // --- internals ---

  private def asZipPath(path: Path): ZipPath = path match {
    case zp: ZipPath => zp
    case _           =>
      throw new ProviderMismatchException(
        s"Path is not a ZipPath: ${path.getClass.getName}"
      )
  }

  private def splitAttrSpec(spec: String): (String, String) = {
    val idx = spec.indexOf(':')
    if (idx < 0) ("basic", spec)
    else (spec.substring(0, idx), spec.substring(idx + 1))
  }

  /** Resolve `path` against the file system's `ZipFile` and return its
   *  attributes. Throws `NoSuchFileException` if neither a direct entry, a
   *  `name/` entry, nor an implied-directory prefix match.
   */
  // ZIP entry names never start with "/"; the root path "/" maps to the
  // empty entry name. Used by readZipAttributes / newInputStream /
  // newDirectoryStream.
  private[zipfs] def toEntryName(path: ZipPath): String = {
    val abs = path.toAbsolutePath().normalize().toString
    if (abs == "/") ""
    else if (abs.startsWith("/")) abs.substring(1)
    else abs
  }

  private[zipfs] def readZipAttributes(path: ZipPath): ZipFileAttributes = {
    val fs = path.getFileSystem().asInstanceOf[ZipFileSystem]
    fs.ensureOpen()

    val entryName = toEntryName(path)

    if (entryName.isEmpty)
      return syntheticDirectoryAttrs()

    if (fs.isWritable) {
      lookupLiveInode(fs, entryName) match {
        case Some(Inode.Original(_, e, _)) =>
          return fromEntry(e, e.isDirectory())
        case Some(Inode.Modified(bytes, e, _)) =>
          return fromEntry(e, e.isDirectory(), Some(bytes.length.toLong))
        case Some(Inode.Deleted) => ()
        case None                => ()
      }

      lookupLiveInode(fs, entryName + "/") match {
        case Some(Inode.Original(_, e, _)) =>
          return fromEntry(e, _isDir = true)
        case Some(Inode.Modified(_, e, _)) =>
          return fromEntry(e, _isDir = true, Some(0L))
        case Some(Inode.Deleted) => ()
        case None                => ()
      }

      if (hasPrefix(fs, entryName + "/"))
        return syntheticDirectoryAttrs()

      throw new NoSuchFileException(path.toString)
    }

    val zf = fs.zipFile

    // Try the exact name (covers both files and explicit directories like
    // "META-INF/").
    val direct = zf.getEntry(entryName)
    if (direct != null) return fromEntry(direct, direct.isDirectory())

    // Try as an explicit directory entry.
    val asDir = zf.getEntry(entryName + "/")
    if (asDir != null) return fromEntry(asDir, _isDir = true)

    // Implied directory: any entry has `entryName + "/"` as a prefix.
    if (hasPrefix(zf, entryName + "/"))
      return syntheticDirectoryAttrs()

    throw new java.nio.file.NoSuchFileException(path.toString)
  }

  private def fromEntry(
      e: ZipEntry,
      _isDir: Boolean,
      sizeOverride: Option[Long] = None
  ): ZipFileAttributes = {
    // FileTime triple: prefer the structured accessors (which see UT/NTFS
    // extra fields populated by ZipEntry); fall back to mtime epoch-zero
    // when nothing is available. Matches jdk.zipfs semantics for entries
    // without timestamp metadata.
    val mt = {
      val ft = e.getLastModifiedTime()
      if (ft != null) ft else ZipFileAttributes.EpochZero
    }
    val at = {
      val ft = e.getLastAccessTime()
      if (ft != null) ft else mt
    }
    val ct = {
      val ft = e.getCreationTime()
      if (ft != null) ft else mt
    }
    new ZipFileAttributes(
      _size =
        if (_isDir) 0L else sizeOverride.getOrElse(math.max(0L, e.getSize())),
      _mtime = mt,
      _atime = at,
      _ctime = ct,
      _isDirectory = _isDir
    )
  }

  private def syntheticDirectoryAttrs(): ZipFileAttributes =
    new ZipFileAttributes(
      _size = 0L,
      _mtime = ZipFileAttributes.EpochZero,
      _atime = ZipFileAttributes.EpochZero,
      _ctime = ZipFileAttributes.EpochZero,
      _isDirectory = true
    )

  private def hasPrefix(zf: java.util.zip.ZipFile, prefix: String): Boolean = {
    val it = zf.entries()
    while (it.hasMoreElements()) {
      val e = it.nextElement()
      if (e.getName().startsWith(prefix)) return true
    }
    false
  }

  private def hasPrefix(fs: ZipFileSystem, prefix: String): Boolean = {
    val it = fs.inodes.entrySet().iterator()
    while (it.hasNext()) {
      val e = it.next()
      if (e.getValue() != Inode.Deleted && e.getKey().startsWith(prefix))
        return true
    }
    false
  }

  private def lookupLiveInode(
      fs: ZipFileSystem,
      entryName: String
  ): Option[Inode] =
    if (!fs.isWritable) None
    else {
      val inode = fs.inodes.get(entryName)
      if (inode == null) None else Some(inode)
    }

  private def addDirectoryChild(
      seen: LinkedHashSet[String],
      prefix: String,
      name: String
  ): Unit = {
    if (name.length > prefix.length && name.startsWith(prefix)) {
      val rest = name.substring(prefix.length)
      val slash = rest.indexOf('/')
      val child = if (slash < 0) rest else rest.substring(0, slash)
      if (child.length > 0) seen.add(child)
    }
  }

  private def setTimeAttribute(
      entry: ZipEntry,
      attrName: String,
      value: FileTime
  ): Unit = attrName match {
    case "lastModifiedTime" => entry.setLastModifiedTime(value)
    case "lastAccessTime"   => entry.setLastAccessTime(value)
    case "creationTime"     => entry.setCreationTime(value)
    case _                  =>
      throw new IllegalArgumentException(
        s"Unknown basic attribute: $attrName"
      )
  }

  private def parseCopyOptions(
      options: Array[CopyOption],
      isMove: Boolean
  ): ParsedCopyOptions = {
    if (options == null) throw new NullPointerException("options")
    var replace = false
    var i = 0
    while (i < options.length) {
      options(i) match {
        case null => throw new NullPointerException("option")
        case StandardCopyOption.REPLACE_EXISTING =>
          replace = true
        case StandardCopyOption.COPY_ATTRIBUTES =>
          () // ZipFS copies basic metadata by default.
        case LinkOption.NOFOLLOW_LINKS =>
          () // ZipFS has no symbolic links.
        case StandardCopyOption.ATOMIC_MOVE if isMove =>
          throw new AtomicMoveNotSupportedException(
            "",
            "",
            "ZipFS move is implemented as an inode-map update"
          )
        case o =>
          throw new UnsupportedOperationException(
            s"ZipFS CopyOption not supported: $o"
          )
      }
      i += 1
    }
    ParsedCopyOptions(replace)
  }

  private def targetKeyForSource(
      target: ZipPath,
      isDirectory: Boolean
  ): String = {
    val name = toEntryName(target)
    if (name.isEmpty) ""
    else if (isDirectory) name + "/"
    else name
  }

  private def existingInodeKey(
      fs: ZipFileSystem,
      path: ZipPath
  ): Option[String] = {
    val entryName = toEntryName(path)
    if (entryName.isEmpty) None
    else {
      liveInode(fs, entryName) match {
        case Some(_) => Some(entryName)
        case None    =>
          val dirName = entryName + "/"
          liveInode(fs, dirName) match {
            case Some(_) => Some(dirName)
            case None    => None
          }
      }
    }
  }

  private def liveInode(fs: ZipFileSystem, key: String): Option[Inode] =
    lookupLiveInode(fs, key) match {
      case Some(Inode.Deleted) => None
      case other               => other
    }

  private def hasExplicitLiveInode(fs: ZipFileSystem, key: String): Boolean =
    liveInode(fs, key).isDefined

  private def prepareTargetForWrite(
      fs: ZipFileSystem,
      target: ZipPath,
      targetKey: String,
      replaceExisting: Boolean
  ): Unit = {
    if (targetKey.isEmpty)
      throw new FileAlreadyExistsException(target.toString)
    validateWritableParent(target)
    existingInodeKey(fs, target) match {
      case Some(existing) =>
        if (!replaceExisting)
          throw new FileAlreadyExistsException(target.toString)
        if (existing.endsWith("/") && hasLiveChild(fs, existing))
          throw new DirectoryNotEmptyException(target.toString)
        fs.inodes.put(existing, Inode.Deleted)
      case None =>
        if (hasPrefix(fs, targetKey))
          throw new FileAlreadyExistsException(target.toString)
    }
  }

  private def hasLiveChild(fs: ZipFileSystem, dirKey: String): Boolean = {
    val it = fs.inodes.entrySet().iterator()
    while (it.hasNext()) {
      val e = it.next()
      if (e.getValue() != Inode.Deleted) {
        val key = e.getKey()
        if (key.length > dirKey.length && key.startsWith(dirKey))
          return true
      }
    }
    false
  }

  private def newDirectoryEntry(name: String): ZipEntry = {
    val e = new ZipEntry(name)
    e.setMethod(ZipEntry.STORED)
    e.setTime(System.currentTimeMillis())
    e.setSize(0L)
    e.setCompressedSize(0L)
    e.setCrc(0L)
    e
  }

  private def cloneInode(inode: Inode, targetName: String): Inode =
    inode match {
      case Inode.Original(src, e, gp) =>
        Inode.Original(src, cloneEntryForName(e, targetName), gp)
      case Inode.Modified(bytes, e, gp) =>
        Inode.Modified(bytes.clone(), cloneEntryForName(e, targetName), gp)
      case Inode.Deleted =>
        Inode.Deleted
    }

  private def cloneEntryForName(
      source: ZipEntry,
      targetName: String
  ): ZipEntry = {
    val e = new ZipEntry(targetName)
    if (source.getComment() != null) e.setComment(source.getComment())
    if (source.getMethod() != -1) e.setMethod(source.getMethod())
    if (source.getSize() >= 0L) e.setSize(source.getSize())
    if (source.getCompressedSize() >= 0L)
      e.setCompressedSize(source.getCompressedSize())
    if (source.getCrc() >= 0L) e.setCrc(source.getCrc())
    val extra = source.getExtra()
    if (extra != null) e.setExtra(extra.clone())
    val mt = source.getLastModifiedTime()
    if (mt != null) e.setLastModifiedTime(mt)
    else if (source.getTime() != -1L) e.setTime(source.getTime())
    val at = source.getLastAccessTime()
    if (at != null) e.setLastAccessTime(at)
    val ct = source.getCreationTime()
    if (ct != null) e.setCreationTime(ct)
    e
  }

  private def moveDirectory(
      fs: ZipFileSystem,
      srcKey: String,
      targetKey: String
  ): Unit = {
    val moved = new java.util.ArrayList[(String, Inode)]()
    liveInode(fs, srcKey) match {
      case Some(inode) =>
        moved.add((targetKey, cloneInode(inode, targetKey)))
      case None =>
        moved.add(
          (
            targetKey,
            Inode.Modified(
              new Array[Byte](0),
              newDirectoryEntry(targetKey),
              0
            )
          )
        )
    }

    val it = fs.inodes.entrySet().iterator()
    while (it.hasNext()) {
      val e = it.next()
      val key = e.getKey()
      if (key.length > srcKey.length && key.startsWith(srcKey) &&
          e.getValue() != Inode.Deleted) {
        val movedKey = targetKey + key.substring(srcKey.length)
        moved.add((movedKey, cloneInode(e.getValue(), movedKey)))
      }
    }

    fs.inodes.put(srcKey, Inode.Deleted)
    val dit = fs.inodes.entrySet().iterator()
    while (dit.hasNext()) {
      val e = dit.next()
      val key = e.getKey()
      if (key.length > srcKey.length && key.startsWith(srcKey))
        fs.inodes.put(key, Inode.Deleted)
    }

    var i = 0
    while (i < moved.size()) {
      val (key, inode) = moved.get(i)
      fs.inodes.put(key, inode)
      i += 1
    }
  }
}

object ZipFileSystemProvider {
  // Per-process registry, keyed by canonical archive path. Lives on the
  // companion because FileSystemProvider.installedProviders rebuilds the
  // provider list on every call, so per-instance state on the provider
  // would be lost between lookups.
  private val registry =
    new ConcurrentHashMap[Path, ZipFileSystem]()

  private final case class OpenMode(
      writeMode: Boolean,
      readable: Boolean,
      append: Boolean,
      write: Boolean,
      create: Boolean,
      createNew: Boolean,
      truncate: Boolean,
      deleteOnClose: Boolean
  )

  private final case class ParsedCopyOptions(replaceExisting: Boolean)

  private sealed trait AccessModeOpt
  private object AccessModeOpt {
    case object Default extends AccessModeOpt
    case object ReadOnly extends AccessModeOpt
    case object ReadWrite extends AccessModeOpt
  }
}
