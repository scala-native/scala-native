package scala.scalanative.nio.fs.zipfs

import java.io.{ByteArrayInputStream, InputStream}
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file._
import java.nio.file.attribute.{
  BasicFileAttributeView, BasicFileAttributes, FileAttribute, FileAttributeView
}
import java.nio.file.spi.FileSystemProvider
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.{ZipEntry, ZipException, ZipFile}
import java.util.{ArrayList, LinkedHashMap, LinkedHashSet, Map}

/** `jar:`-scheme provider over zip/jar archives on the default file system.
 *
 *  Read-only for now: mounting with `accessMode=readWrite` or `create=true`
 *  is rejected with `UnsupportedOperationException`, and every mutating
 *  operation throws [[java.nio.file.ReadOnlyFileSystemException]].
 *
 *  On Scala Native, providers are discovered through `ServiceLoader`, which
 *  resolves implementations at link time: user builds must register
 *  `scala.scalanative.nio.fs.zipfs.ZipFileSystemProvider` for
 *  `java.nio.file.spi.FileSystemProvider` via `NativeConfig.serviceProviders`.
 */
class ZipFileSystemProvider extends FileSystemProvider {

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
    validateEnv(env)

    if (!Files.exists(canonical, Array.empty[LinkOption]))
      throw new java.nio.file.NoSuchFileException(canonical.toString)

    if (!ZipUtils.looksLikeZip(canonical)) onWrongMagic(canonical)

    // Magic ok — any ZipFile parse failure beyond this point is a corrupt
    // archive and propagates as ZipException (subclass of IOException).
    val fs =
      new ZipFileSystem(this, canonical, new ZipFile(canonical.toFile()))

    val prev = ZipFileSystemProvider.registry.putIfAbsent(canonical, fs)
    if (prev != null) {
      fs.closeQuietly()
      throw new FileSystemAlreadyExistsException(canonical.toString)
    }
    fs
  }

  /** Env keys follow jdk.zipfs: `accessMode` may be "readOnly" or
   *  "readWrite", `create` may be true. Only read-only mounts are
   *  implemented, so "readWrite" and `create=true` are rejected with
   *  `UnsupportedOperationException` until write support lands.
   */
  private def validateEnv(env: Map[String, _]): Unit = {
    if (env == null) return
    if (ZipUtils.envFlag(env, "create"))
      throw new UnsupportedOperationException(
        "Creating zip archives is not supported yet (writable ZipFS)"
      )
    val v = env.get("accessMode")
    if (v != null) {
      val s = String.valueOf(v)
      if ("readWrite" == s)
        throw new UnsupportedOperationException(
          "Writable zip file systems are not supported yet"
        )
      else if ("readOnly" != s)
        throw new IllegalArgumentException(
          s"Unsupported ZipFS accessMode: $s (expected readOnly|readWrite)"
        )
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
    validateReadOnlyChannelOptions(options)
    new ZipSeekableByteChannel(readEntryBytes(zp))
  }

  // Channel options legal on a read-only mount. Per the JDK spec CREATE,
  // CREATE_NEW and TRUNCATE_EXISTING are ignored when opening for reading;
  // WRITE / APPEND / DELETE_ON_CLOSE imply mutation and fail.
  private def validateReadOnlyChannelOptions(
      options: java.util.Set[_ <: OpenOption]
  ): Unit = {
    if (options == null) throw new NullPointerException("options")
    val it = options.iterator()
    while (it.hasNext()) {
      it.next() match {
        case null => throw new NullPointerException("option")
        case StandardOpenOption.WRITE | StandardOpenOption.APPEND |
            StandardOpenOption.DELETE_ON_CLOSE =>
          throw new ReadOnlyFileSystemException()
        case StandardOpenOption.READ | StandardOpenOption.CREATE |
            StandardOpenOption.CREATE_NEW |
            StandardOpenOption.TRUNCATE_EXISTING | StandardOpenOption.SPARSE |
            StandardOpenOption.SYNC | StandardOpenOption.DSYNC =>
          ()
        case o =>
          throw new UnsupportedOperationException(
            s"ZipFS OpenOption not supported: $o"
          )
      }
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

  // Options legal for newInputStream. Unlike the channel, write-side
  // options are rejected outright per the Files.newInputStream contract.
  private def validateReadOption(o: OpenOption): Unit = o match {
    case null =>
      // NIO treats null elements in option collections/varargs as
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
    val zf = fs.zipFile
    val entry = zf.getEntry(entryName)
    if (entry == null)
      throw new NoSuchFileException(zp.toString)
    val in = zf.getInputStream(entry)
    try ZipSeekableByteChannel.slurp(in, entry.getSize())
    finally in.close()
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
    val it = fs.zipFile.entries()
    while (it.hasMoreElements()) {
      addDirectoryChild(seen, prefix, it.nextElement().getName())
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
    zp.getFileSystem().asInstanceOf[ZipFileSystem].ensureOpen()
    throw new ReadOnlyFileSystemException()
  }

  override def delete(path: Path): Unit = {
    val zp = asZipPath(path)
    zp.getFileSystem().asInstanceOf[ZipFileSystem].ensureOpen()
    throw new ReadOnlyFileSystemException()
  }

  override def copy(
      source: Path,
      target: Path,
      options: Array[CopyOption]
  ): Unit = {
    val src = asZipPath(source)
    val tgt = asZipPath(target)
    if (tgt.getFileSystem() ne src.getFileSystem())
      throw new ProviderMismatchException()
    src.getFileSystem().asInstanceOf[ZipFileSystem].ensureOpen()
    throw new ReadOnlyFileSystemException()
  }

  override def move(
      source: Path,
      target: Path,
      options: Array[CopyOption]
  ): Unit = {
    val src = asZipPath(source)
    val tgt = asZipPath(target)
    if (tgt.getFileSystem() ne src.getFileSystem())
      throw new ProviderMismatchException()
    src.getFileSystem().asInstanceOf[ZipFileSystem].ensureOpen()
    throw new ReadOnlyFileSystemException()
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
    // existence check — throws NoSuchFileException if absent
    readZipAttributes(zp)
    var i = 0
    while (i < modes.length) {
      val m = modes(i)
      if (m == AccessMode.EXECUTE)
        throw new AccessDeniedException(path.toString)
      if (m == AccessMode.WRITE)
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
    // jdk.zipfs silently ignores unrecognised attribute names in a
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
    zp.getFileSystem().asInstanceOf[ZipFileSystem].ensureOpen()
    throw new ReadOnlyFileSystemException()
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

  // ZIP entry names never start with "/"; the root path "/" maps to the
  // empty entry name.
  private[zipfs] def toEntryName(path: ZipPath): String = {
    val abs = path.toAbsolutePath().normalize().toString
    if (abs == "/") ""
    else if (abs.startsWith("/")) abs.substring(1)
    else abs
  }

  /** Resolve `path` against the file system's `ZipFile` and return its
   *  attributes. Throws `NoSuchFileException` if neither a direct entry, a
   *  `name/` entry, nor an implied-directory prefix match.
   */
  private[zipfs] def readZipAttributes(path: ZipPath): ZipFileAttributes = {
    val fs = path.getFileSystem().asInstanceOf[ZipFileSystem]
    fs.ensureOpen()

    val entryName = toEntryName(path)

    if (entryName.isEmpty)
      return syntheticDirectoryAttrs()

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

  private def fromEntry(e: ZipEntry, _isDir: Boolean): ZipFileAttributes = {
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
      _size = if (_isDir) 0L else math.max(0L, e.getSize()),
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
}

object ZipFileSystemProvider {
  // Per-process registry, keyed by canonical archive path. Lives on the
  // companion because FileSystemProvider.installedProviders rebuilds the
  // provider list on every call, so per-instance state on the provider
  // would be lost between lookups.
  private val registry =
    new ConcurrentHashMap[Path, ZipFileSystem]()
}
