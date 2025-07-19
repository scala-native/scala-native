package java.nio.file

import java.io._

import java.{lang => jl}
import java.lang.Iterable

import java.nio.charset.{Charset, StandardCharsets}
import java.nio.channels.{FileChannel, SeekableByteChannel}

import java.nio.file.attribute._
import java.nio.file.attribute.PosixFilePermission._

import java.nio.file.StandardCopyOption.{COPY_ATTRIBUTES, REPLACE_EXISTING}

import java.util._
import java.util.function.{BiPredicate, Consumer, Supplier}
import java.util.stream.{Stream, StreamSupport}

import scalanative.unsigned._
import scalanative.unsafe._
import scalanative.libc._

import scalanative.posix.dirent._
import scalanative.posix.direntOps._

import scalanative.posix.errno.errno // avoid libc conflict in import errno._
import scalanative.posix.errno._

import scalanative.posix.{fcntl, limits, unistd}
import scalanative.posix.sys.stat

import scalanative.meta.LinktimeInfo.isWindows

import scalanative.nio.fs.FileHelpers
import scalanative.nio.fs.unix.UnixException

import scalanative.windows._
import scalanative.windows.WinBaseApi._
import scalanative.windows.WinBaseApiExt._
import scalanative.windows.FileApiExt._
import scalanative.windows.ErrorHandlingApi._
import scalanative.windows.winnt.AccessRights._
import java.util.WindowsHelperMethods._

object Files {
  private final val emptyPath = Paths.get("", Array.empty)

  // def getFileStore(path: Path): FileStore
  // def probeContentType(path: Path): String

  def copy(in: InputStream, target: Path, options: Array[CopyOption]): Long = {
    val replaceExisting =
      if (options.isEmpty) false
      else if (options.length == 1 && options(0) == REPLACE_EXISTING)
        true
      else throw new UnsupportedOperationException()

    val noFollowOpts = Array(LinkOption.NOFOLLOW_LINKS)

    val out = {
      if (Files.exists(target, noFollowOpts) && !replaceExisting) {
        throw new FileAlreadyExistsException(target.toAbsolutePath().toString())
      } else if (Files.isRegularFile(target, noFollowOpts)) {
        /* Deleting and recreating a file in order to replace it is expensive
         * but also more certain than adjusting permissions when umask
         * is unknowable.
         */
        Files.delete(target)
      } else if (Files.isDirectory(target, noFollowOpts)) {
        if (!target.toFile().list().isEmpty)
          throw new DirectoryNotEmptyException(
            target.toAbsolutePath().toString()
          )
        else
          Files.delete(target)
      } else if (Files.isSymbolicLink(target)) {
        Files.delete(target)
      }

      Files.newOutputStream(target, Array.empty)
    }

    try {
      val copyResult = in.transferTo(out)
      if (isWindows) {
        // This block is unnecessary on unix. Is this really necessary on
        // Windows?
        // Make sure that created file has correct permissions
        val targetFile = target.toFile()
        val targetExists = targetFile.exists()

        if (!targetExists) {
          targetFile.setReadable(true, ownerOnly = false)
          targetFile.setWritable(true, ownerOnly = true)
        }
      }
      copyResult
    } finally out.close()
  }

  def copy(source: Path, out: OutputStream): Long = {
    val in = newInputStream(source, Array.empty)
    try {
      in.transferTo(out)
    } finally in.close()
  }

  /* Precondition:
   *   - an ancestor caller has ensured that cTarget is neither a directory
   *     nor a symbolic link. Let fcntl.open() determine how other non-regular
   *     existing files are handled.
   */

  private def unixCopyFile(
      source: Path,
      target: Path,
      copyOptions: Array[CopyOption],
      permissions: Set[PosixFilePermission]
  ): Unit = {
    if (isWindows)
      throw new IOException("Unix specific method; should never get here")
    else
      Zone.acquire { implicit z =>

        /* Requirement:
         * 
         *   Files.copy(Path, Path, Options) on the JVM ensures that, on
         *   success, the PosixPermissions of the source, limited by the
         *   process umask, have been copied to the target.
         *   This is similar to bash & zsh behavior.
         *
         *   Using the usual 022 umask, copying a source file with permissions
         *   r-xrwxrwx results in a target file with permissions
         *   r-xr-xr-x.
         */

        /* Design Notes:
         * 
         *   - Use POSIX I/O to handle the corner case where a file exists but
         *     the user does not have write access: r--x------ & kin.
         * 
         *     JVM handles this case, Scala Native must also.
         * 
         *     Most of Scala Native javalib Files.scala, File_Helpers.scala,
         *     java.nio.*, and java.io.* use a non-atomic sequence of steps:
         *     create the file, then set indicated attributes. Any subsequent
         *     write to the file fails because the file permissions have been
         *     set user no-write.
         * 
         *     POSIX fcntl.open() is defined so that it can open and create
         *     a new file for write if the indicated directory permissions
         *     allow. Code can use the fd returned to write to the file as long
         *     as that fd is open. The mode argument to open() is applied
         *     only to future accesses.
         *
         *   - This method is optimized for success paths. That is
         *     condition checking is delegated to the operating system
         *     under the expectation that in most cases the operation will
         *     succeed.
         * 
         *   - Some, but probably not all, rare and somewhat astonishing corner
         *     conditions exist when the REPLACE_EXISTING option is present:
         * 
         *     - Any kind of IOException, including but not limited to:
         *           - source file can not be read
         *       Action: target file is deleted.
         * 
         *     - target file exists but does not have write permission,
         *       e.g. r-xr-xr-x.
         *       Action: copy proceeds but inode number changes.
         *
         *   - This method follows the practice from early Scala Native
         *     development days of modifying files in-place.  This
         *     leaves a pretty wide window for misadventure, particularly
         *     if more that one thread or process is accessing the file.
         * 
         *     Many contemporary applications create a temporary intermediate
         *     file, copy the source contents to the temporary,
         *     set permissions on the temporary, and then, finally, if the
         *     replace conditions still hold, rename the temporary to the
         *     provided target path. A second application will never
         *     see an incompletely copied target file.
         *
         *     The major difficulty with that approach is reliably creating
         *     a file with a temporary name. The obvious library calls
         *     each have their own drawbacks. A "create-until-success" loop
         *     also has its own pain points: more than an afternoon's work.
         * 
         *     Oh, give me a good ship, a fair wind, and a few clever
         *     secondary school students!
         */

        def permissionsToUnixModeType(
            perms: Set[PosixFilePermission]
        ): UInt = {
          var mode = 0.toUInt

          if (perms.contains(OWNER_READ))
            mode |= stat.S_IRUSR

          if (perms.contains(OWNER_WRITE))
            mode |= stat.S_IWUSR

          if (perms.contains(OWNER_EXECUTE))
            mode |= stat.S_IXUSR

          if (perms.contains(GROUP_READ))
            mode |= stat.S_IRGRP

          if (perms.contains(GROUP_WRITE))
            mode |= stat.S_IWGRP

          if (perms.contains(GROUP_EXECUTE))
            mode |= stat.S_IXGRP

          if (perms.contains(OTHERS_READ))
            mode |= stat.S_IROTH

          if (perms.contains(OTHERS_WRITE))
            mode |= stat.S_IWOTH

          if (perms.contains(OTHERS_EXECUTE))
            mode |= stat.S_IXOTH

          mode
        }

        def openTarget(
            cTarget: CString,
            replaceExisting: Boolean,
            cPerms: UInt
        ): Int = {
          val cOpenExtraFlags =
            if (replaceExisting) fcntl.O_TRUNC
            else fcntl.O_EXCL

          val createFd = fcntl.open(
            cTarget,
            fcntl.O_WRONLY | fcntl.O_CREAT | cOpenExtraFlags,
            cPerms
          )

          if (createFd != -1) {
            createFd
          } else if (replaceExisting && (errno == EACCES)) {
            /* Handle what should be an vanishingly rare but possible
             * corner case where cTarget exists but is not user writable;
             * r-xr-xr-x, --xr-xr-x, and kin. O_TRUNC will fail in those cases.
             * 
             * unlink() is a directory operation. If the permissions on that
             * directory permit, the operation should succeed.
             * 
             * Of course, if two or more threads/processes are accessing the
             * same file without explicit synchronization, there are always
             * timing issues, since the file unlink & subsequent creation
             * are not atomic.
             */
            unistd.unlink(cTarget) // Handle error later.
            openTarget(cTarget, replaceExisting = false, cPerms)
          } else {
            val msg = fromCString(string.strerror(errno))
            throw new IOException(
              s"error opening target path '${cTarget}': ${msg}"
            )
          }
        }

        def transferTo(inFd: Int, outFd: Int): Unit = {
          /* If measurement and/or experience shows this method to be a
           * performance bottleneck, a future Evolution could explore
           * calling methods operating systems have developed just for this
           * purpose. Linux has 'copy_file_range()' while macOS has
           * 'copyfile()'. The picture on FreeBSD is more complicated.
           */

          val limit = 8192 // a guess of appropriate size, 2 * usual page size
          val buffer = new Array[Byte](limit).at(0)

          errno = 0 // clearing is probably redundant but be defensive

          var doneRead = false

          while (!doneRead) {
            val nRead = unistd.read(inFd, buffer, limit.toCSize)

            if (nRead < 0) {
              val msg = fromCString(string.strerror(errno))
              throw new IOException(
                s"error reading copy source file: ${msg}"
              )
            } else if (nRead == 0) {
              doneRead = true // EOF
            } else {
              // Be robust to partial writes.
              var nRemaining = nRead
              while ((nRemaining > 0) && errno == 0) {
                val nWritten = unistd.write(outFd, buffer, nRemaining.toCSize)
                if (nWritten < 0) {
                  val msg = fromCString(string.strerror(errno))
                  throw new IOException(
                    s"error writing copy target file: ${msg}"
                  )
                }
                nRemaining -= nWritten
              }
            }
          }
        }

        val absSource = source.toAbsolutePath().toString()
        val cSource = toCString(absSource)

        val absTarget = target.toAbsolutePath().toString()
        val cTarget = toCString(absTarget)

        val cPerms = permissionsToUnixModeType(permissions)

        errno = 0

        /* Hold the 'source' read-lock the shortest amount of time.
         * Do the potentially time consuming open of 'target' first.
         *
         * Other Java I/O options probably do not make sense for uxix I/O.
         * Time & experience will tell.
         */
        val outFd =
          openTarget(cTarget, copyOptions.contains(REPLACE_EXISTING), cPerms)

        try {
          val inFd = fcntl.open(cSource, fcntl.O_RDONLY, 0.toUInt)

          if (inFd == -1) {
            val msg = fromCString(string.strerror(errno))
            throw new IOException(
              s"error opening source path '${absSource}': ${msg}"
            )
          }

          try
            transferTo(inFd, outFd)
          finally
            unistd.close(inFd)

        } catch {
          case _: IOException => unistd.unlink(cTarget) // leave no garbage
        } finally {
          unistd.close(outFd)
        }
      }
  }

  def copy(source: Path, target: Path, options: Array[CopyOption]): Path = {
    val linkOpts = Array(LinkOption.NOFOLLOW_LINKS)
    val attrsCls =
      if (isWindows) classOf[DosFileAttributes]
      else classOf[PosixFileAttributes]

    val attrs = Files.readAttributes(source, attrsCls, linkOpts)
    val targetExists = exists(target, linkOpts)
    if (targetExists && !options.contains(REPLACE_EXISTING))
      throw new FileAlreadyExistsException(target.toString)

    if (attrs.isSymbolicLink() &&
        options.contains(LinkOption.NOFOLLOW_LINKS)) {
      if (targetExists) Files.delete(target)
      createSymbolicLink(target, readSymbolicLink(source), Array.empty)
    } else if (isDirectory(source, Array.empty)) {
      createDirectory(target, Array.empty)
    } else if (!isWindows) {
      // Scala Native Issue #4382
      // Devos, ensure preconditions described at top of method endure.
      unixCopyFile(
        source,
        target,
        options,
        attrs.asInstanceOf[PosixFileAttributes].permissions()
      )
    } else { // Windows
      val in = newInputStream(source, Array.empty)
      try copy(in, target, options.filter(_ == REPLACE_EXISTING))
      finally in.close()
    }

    /* Bug Alert! The following block appears to not copy Access Control Lists.
     * See Scala Native Issue #4381.
     */
    if (options.contains(COPY_ATTRIBUTES)) {
      val attrViewCls =
        if (isWindows) classOf[DosFileAttributeView]
        else classOf[PosixFileAttributeView]
      val newAttrView = getFileAttributeView(target, attrViewCls, linkOpts)

      (attrs, newAttrView) match {
        case (
              attrs: PosixFileAttributes,
              newAttrView: PosixFileAttributeView
            ) =>
          newAttrView.setGroup(attrs.group())
          newAttrView.setOwner(attrs.owner())
          newAttrView.setPermissions(attrs.permissions())
        case (attrs: DosFileAttributes, newAttrView: DosFileAttributeView) =>
          newAttrView.setArchive(attrs.isArchive())
          newAttrView.setHidden(attrs.isHidden())
          newAttrView.setReadOnly(attrs.isReadOnly())
          newAttrView.setSystem(attrs.isSystem())
        case _ => ??? // ignore
      }
      newAttrView.setTimes(
        attrs.lastModifiedTime(),
        attrs.lastAccessTime(),
        attrs.creationTime()
      )
    }
    target
  }

  def createDirectories(dir: Path, attrs: Array[FileAttribute[_]]): Path =
    if (exists(dir, Array.empty) && !isDirectory(dir, Array.empty))
      throw new FileAlreadyExistsException(dir.toString)
    else if (exists(dir, Array.empty)) dir
    else {
      val parent = dir.getParent()
      if (parent != null) createDirectories(parent, attrs)
      createDirectory(dir, attrs)
      dir
    }

  def createDirectory(dir: Path, attrs: Array[FileAttribute[_]]): Path =
    if (exists(dir, Array.empty)) {
      if (!isDirectory(dir, Array.empty)) {
        throw new FileAlreadyExistsException(dir.toString)
      } else if (list(dir).iterator().hasNext()) {
        throw new DirectoryNotEmptyException(dir.toString)
      }
      dir
    } else if (dir.toFile().mkdir()) {
      setAttributes(dir, attrs)
      dir
    } else {
      throw new IOException()
    }

  def createFile(path: Path, attrs: Array[FileAttribute[_]]): Path = {
    if (exists(path, Array.empty))
      throw new FileAlreadyExistsException(path.toString)
    else if (FileHelpers.createNewFile(path.toString, throwOnError = true)) {
      setAttributes(path, attrs)
    }
    path
  }

  def createLink(link: Path, existing: Path): Path = Zone.acquire {
    implicit z =>
      if (isWindows) {
        if (exists(link, Array.empty)) {
          throw new FileAlreadyExistsException(link.toString)
        } else {
          val created = CreateHardLinkW(
            toCWideStringUTF16LE(link.toString),
            toCWideStringUTF16LE(existing.toString),
            securityAttributes = null
          )
          if (created) {
            link
          } else {
            throw new IOException("Cannot create link")
          }
        }

      } else {
        val rtn = unistd.link(
          toCString(existing.toString()),
          toCString(link.toString())
        )

        if (rtn == 0) {
          link
        } else {
          val e = errno
          if (e == EEXIST)
            throw new FileAlreadyExistsException(link.toString)
          else if (e == ENOENT)
            throw new NoSuchFileException(
              link.toString,
              existing.toString,
              null
            )
          else
            throw new IOException(fromCString(string.strerror(e)))
        }

      }
  }

  def createSymbolicLink(
      link: Path,
      target: Path,
      attrs: Array[FileAttribute[_]]
  ): Path = {

    def tryCreateLink() = Zone.acquire { implicit z =>
      if (isWindows) {
        import WinBaseApiExt._
        val targetFilename = toCWideStringUTF16LE(target.toString())
        val linkFilename = toCWideStringUTF16LE(link.toString())
        val flags =
          if (isDirectory(target, Array(LinkOption.NOFOLLOW_LINKS)))
            SYMBOLIC_LINK_FLAG_DIRECTORY
          else SYMBOLIC_LINK_FLAG_FILE
        val created =
          CreateSymbolicLinkW(
            symlinkFileName = linkFilename,
            targetFileName = targetFilename,
            flags = flags
          )
        val ERROR_PRIVILEGE_NOT_HELD = 1314.toUInt
        // On Windows creating soft link is possible only with admin privileges
        if (!created &&
            GetLastError() == ERROR_PRIVILEGE_NOT_HELD) {
          // If develop mode is enabled we can create unprivileged symlinks
          CreateSymbolicLinkW(
            symlinkFileName = linkFilename,
            targetFileName = targetFilename,
            flags = flags | SYMBOLIC_LINK_FLAG_ALLOW_UNPRIVILEGED_CREATE
          ) || {
            throw new SecurityException(
              "Creating symbolic link requires admin privileges"
            )
          }
        } else created
      } else {
        val targetFilename = toCString(target.toString())
        val linkFilename = toCString(link.toString())
        unistd.symlink(targetFilename, linkFilename) == 0
      }
    }

    if (exists(link, Array.empty)) {
      throw new FileAlreadyExistsException(target.toString)
    } else if (tryCreateLink()) {
      setAttributes(link, attrs)
      link
    } else {
      throw new IOException("Cannot create symbolic link")
    }
  }

  private def createTempDirectory(
      dir: File,
      prefix: String,
      attrs: Array[FileAttribute[_]]
  ): Path = {
    val p = if (prefix == null) "" else prefix
    val temp = FileHelpers.createTempFile(
      p,
      "",
      dir,
      minLength = false,
      throwOnError = true
    )
    if (temp.delete() && temp.mkdir()) {
      val tempPath = temp.toPath()

      if (!isWindows) {
        /* A better fix would be to have a unix path which uses
         * 'mkdtemp()' rather than 'File.mkdir()'. The former gives a
         * close to an atomic
         * generate_temporary_name-create_directory_with_restricted_protections
         * sequence.  Unfortunately, it requires more time than the day
         * provides.
         */

        /* FIXUP unix protections to match JVM practice and security.
         * Take the bet that only rarely will atters be non-empty and
         * even more rarely will permissions be set there,
         * cause, the permissions to be set twice.
         */
        val jvmCreateTempDirDefaultPermissions =
          PosixFilePermissions.fromString("rwx------")
        Files.setPosixFilePermissions(
          tempPath,
          jvmCreateTempDirDefaultPermissions
        )
      }

      if (attrs.length > 0)
        setAttributes(tempPath, attrs)

      tempPath
    } else {
      throw new IOException()
    }
  }

  def createTempDirectory(
      dir: Path,
      prefix: String,
      attrs: Array[FileAttribute[_]]
  ): Path =
    createTempDirectory(dir.toFile(), prefix, attrs)

  def createTempDirectory(
      prefix: String,
      attrs: Array[FileAttribute[_]]
  ): Path =
    createTempDirectory(null: File, prefix, attrs)

  private def createTempFile(
      dir: File,
      prefix: String,
      suffix: String,
      attrs: Array[FileAttribute[_]]
  ): Path = {
    val p = if (prefix == null) "" else prefix
    val temp = FileHelpers.createTempFile(
      p,
      suffix,
      dir,
      minLength = false,
      throwOnError = true
    )
    val tempPath = temp.toPath()
    setAttributes(tempPath, attrs)
    tempPath
  }

  def createTempFile(
      dir: Path,
      prefix: String,
      suffix: String,
      attrs: Array[FileAttribute[_]]
  ): Path =
    createTempFile(dir.toFile(), prefix, suffix, attrs)

  def createTempFile(
      prefix: String,
      suffix: String,
      attrs: Array[FileAttribute[_]]
  ): Path =
    createTempFile(null: File, prefix, suffix, attrs)

  private def windowsDeletePath(path: Path): Unit = {
    // Optimize for delete() success. Spend cycles fixing up only on failure.
    if (!path.toFile().delete()) {
      val targetFile = path.toFile()
      if (targetFile.isDirectory() && !targetFile.list().isEmpty) {
        throw new DirectoryNotEmptyException(targetFile.getAbsolutePath())
      } else {
        throw new IOException(s"Failed to remove $path")
      }
    }
  }

  private def unixDeletePath(path: Path): Unit = Zone.acquire { implicit z =>
    val ps = path.toString
    if (stdio.remove(toCString(ps)) == -1) {
      // For historical reasons, some systems report ENOTEMPTY as EEXIST
      val fixedErrno = if (errno == EEXIST) ENOTEMPTY else errno
      throw PosixException(ps, fixedErrno)
    }
  }

  def delete(path: Path): Unit = {
    if (!exists(path, Array(LinkOption.NOFOLLOW_LINKS))) {
      throw new NoSuchFileException(path.toString)
    } else if (isWindows) {
      windowsDeletePath(path)
    } else {
      unixDeletePath(path) // give more information on unanticipated failure
    }
  }

  def deleteIfExists(path: Path): Boolean =
    try {
      delete(path); true
    } catch { case _: NoSuchFileException => false }

  def exists(path: Path, options: Array[LinkOption]): Boolean = {
    def fileExists = path.toFile().exists()
    def noFollowLinks = options.contains(LinkOption.NOFOLLOW_LINKS)

    fileExists || (noFollowLinks && isSymbolicLink(path))
  }

  def find(
      start: Path,
      maxDepth: Int,
      matcher: BiPredicate[Path, BasicFileAttributes],
      options: Array[FileVisitOption]
  ): Stream[Path] = {
    Objects.requireNonNull(start, "start is null")
    if (maxDepth < 0)
      throw new IllegalArgumentException("'maxDepth' is negative")
    Objects.requireNonNull(matcher, "matcher is null")

    val followLinks = options.contains(FileVisitOption.FOLLOW_LINKS)

    FileTreeWalker(start, maxDepth, followLinks, matcher).stream()
  }

  def getAttribute(
      path: Path,
      attribute: String,
      options: Array[LinkOption]
  ): Object = {
    val sepIndex = attribute.indexOf(":")
    val (viewName, attrName) =
      if (sepIndex == -1) ("basic", attribute)
      else
        (
          attribute.substring(0, sepIndex),
          attribute.substring(sepIndex + 1, attribute.length)
        )
    val viewClass = {
      if (!viewNamesToClasses.containsKey(viewName))
        throw new UnsupportedOperationException()
      viewNamesToClasses.get(viewName)
    }
    val view = getFileAttributeView(path, viewClass, options)
    view.getAttribute(attrName)
  }

  def getFileAttributeView[V <: FileAttributeView](
      path: Path,
      tpe: Class[V],
      options: Array[LinkOption]
  ): V =
    path.getFileSystem().provider().getFileAttributeView(path, tpe, options)

  def getLastModifiedTime(path: Path, options: Array[LinkOption]): FileTime = {
    val attributes =
      getFileAttributeView(path, classOf[BasicFileAttributeView], options)
        .readAttributes()
    attributes.lastModifiedTime()
  }

  def getOwner(path: Path, options: Array[LinkOption]): UserPrincipal = {
    val view =
      getFileAttributeView(path, classOf[FileOwnerAttributeView], options)
    view.getOwner()
  }

  def getPosixFilePermissions(
      path: Path,
      options: Array[LinkOption]
  ): Set[PosixFilePermission] =
    getAttribute(path, "posix:permissions", options)
      .asInstanceOf[Set[PosixFilePermission]]

  def isDirectory(path: Path, options: Array[LinkOption]): Boolean =
    try {
      val attrs = readAttributes(path, classOf[BasicFileAttributes], options)
      attrs != null && attrs.isDirectory()
    } catch { case _: IOException => false }

  def isExecutable(path: Path): Boolean =
    path.toFile().canExecute()

  def isHidden(path: Path): Boolean =
    path.toFile().isHidden()

  def isReadable(path: Path): Boolean =
    path.toFile().canRead()

  def isRegularFile(path: Path, options: Array[LinkOption]): Boolean = {
    try {
      val attrs = readAttributes(path, classOf[BasicFileAttributes], options)
      attrs != null && attrs.isRegularFile()
    } catch { case _: IOException => false }
  }

  def isSameFile(path: Path, path2: Path): Boolean =
    path.toFile().getCanonicalPath() == path2.toFile().getCanonicalPath()

  def isSymbolicLink(path: Path): Boolean =
    try {
      val attrs = readAttributes(
        path,
        classOf[BasicFileAttributes],
        Array(LinkOption.NOFOLLOW_LINKS)
      )
      attrs != null && attrs.isSymbolicLink()
    } catch { case _: IOException => false }

  def isWritable(path: Path): Boolean =
    path.toFile().canWrite()

  def lines(path: Path): Stream[String] =
    lines(path, StandardCharsets.UTF_8)

  def lines(path: Path, cs: Charset): Stream[String] =
    newBufferedReader(path, cs).lines(true)

  private final val nul = 0.toByte // ASCII NUL
  private final val dot = 46.toByte // ASCII period or dot

  private def posixList(dir: Path, dirString: String): Stream[Path] = {
    /* The JVM specification describes the returned stream as lazy.
     *
     * FileHelpers.unixList() is not used here because it eagerly creates an
     * Array of all the entries in the directory.
     */

    class PosixDir(dir: Path, dirString: String)
        extends Supplier[Spliterator[Path]] {
      /* Older operating system provided no guarantee that the
       * dirent data returned by readdir() would not be overwritten.
       *
       * Open Group 2018 says "They shall not be affected by a call to
       * readdir() on a different directory stream.". Most if not all current
       * (circa 2024) operating systems supported by Scala Native describe
       * the same guarantee.
       *
       * The calls to readdir() here are two uses of result of the same
       * one opendir() call. Exactly the kind of access pattern which
       * "must be externally synchronized".
       *
       * That "external synchronization" exists in this method but is
       * not obvious on a quick reading.
       *
       * The __essential__ concept that the "trySplit()" method of the
       * Spliterator for the returned stream is overriden to _never_ split.
       * No split, no parallel execution of this stream, no conflict.
       *
       * The Stream "spliterator" and "iterator" methods are described in
       * JVM as terminal operations. That is, one should not be able to
       * obtain more than one. Subsequent attempts will fail.
       * Both are also described as non-thread safe, only the returned
       * stream needs to be thread-safe.
       *
       * Belt, suspenders, duct tape, and instant glue
       * "external synchronization" by design rather than runtime
       * "synchronized" blocks.
       */

      private var posixDirClosed = true

      private lazy val posixDir: Ptr[DIR] = Zone.acquire { implicit z =>
        val ptr = opendir(toCString(dirString))
        if (ptr == null)
          throw new UncheckedIOException(PosixException(dirString, errno))

        posixDirClosed = false
        ptr
      }

      private type T = Path

      def get(): Spliterator[T] = {

        new Spliterators.AbstractSpliterator[T](Long.MaxValue, 0) {
          private def appendToStream(
              cName: CString,
              action: Consumer[_ >: T]
          ): Boolean = {
            val entryPath = dir.resolve(fromCString(cName))

            action.accept(entryPath)
            true
          }

          /* See Design Note at top of method about serializing access
           * to C's readdir buffer.
           *
           * _Never_ splitting this Spliterator is __critical__.
           */
          override def trySplit(): Spliterator[T] = null

          def tryAdvance(action: Consumer[_ >: T]): Boolean = {
            /* Reduce execution cost by relying upon readdir() to detect
             * a closed directory rather than checking posixDirclosed.
             */

            val entry = { errno = 0; readdir(posixDir) }

            if (entry == null) {
              if (errno != 0) {
                throw new UncheckedIOException(PosixException(dirString, errno))
              } else { // End of OS directory stream
                closeImpl()
                false
              }
            } else {
              /* Consume "." and "..", Java does not want to see them.
               *
               * Those two entries are usually the first two, but that
               * is not guaranteed. There are obscure scenarios where
               * at least '.' can come later.
               *
               * This is conceptually a 'stream.filter()' operation but
               * with less overhead.
               */

              val entryName = entry.d_name // A CString, so byte comparisons

              if (entryName(0) != dot)
                appendToStream(entryName, action)
              else if (entryName(1) == nul)
                tryAdvance(action) // past "."
              else if ((entryName(1) == dot) && (entryName(2) == nul))
                tryAdvance(action) // past ".."
              else
                appendToStream(entryName, action) // ".git" or such
            }
          }
        }
      }

      /* Call only while holding dirLock.
       * When closedir() is called more than once, some operating systems
       * set errno to EBADF. Others are not so robust and fail (signal? exit?).
       */

      private def closeImpl(): Unit = {
        if (!posixDirClosed) {
          val err = closedir(posixDir)
          if (err != 0)
            throw new UncheckedIOException(PosixException(dirString, errno))

          posixDirClosed = true
        }
      }

      def close(): Unit =
        closeImpl()
    }

    val posixDir = new PosixDir(dir, dirString)
    StreamSupport.stream(posixDir, 0, false).onClose(() => posixDir.close())
  }

  def list(dir: Path): Stream[Path] = {
    /* Fix Issue 3165 - From Java "Path" documentation URL:
     * https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html
     *
     * "Accessing a file using an empty path is equivalent to accessing the
     * default directory of the file system."
     *
     * Operating Systems can not opendir() an empty string, so expand "" to
     * "./".
     */

    val dirString =
      if (dir.equals(emptyPath)) "./"
      else dir.toString()

    if (!isWindows)
      posixList(dir, dirString) // see comment re: args at top of that method
    else {
      Arrays.stream[Path](FileHelpers.list(dirString, (n, _) => dir.resolve(n)))
    }
  }

  private def mismatchCore(is1: InputStream, is2: InputStream): Long = {
    // A guess, trade off memory use vs speed, many machine page sizes are 4K.
    val bufMaxSize = 4 * 1024 // buf1 & buf1 use 8K total
    val buf1 = new Array[Byte](bufMaxSize)
    val buf2 = new Array[Byte](bufMaxSize)

    var mismatchedAt = jl.Long.MIN_VALUE // not _, as that is a valid value 0
    var runningTotal = 0L // cumulative count of bytes read & matched to date

    var done = false

    while (!done) {
      val nReadBuf1 = is1.readNBytes(buf1, 0, bufMaxSize)
      val nReadBuf2 = is2.readNBytes(buf2, 0, Math.max(1, nReadBuf1))

      if ((nReadBuf1 == 0) && (nReadBuf2 == 0)) { // both EOF
        mismatchedAt = -1
        done = true
      } else {
        val mmPos = Arrays.mismatch(buf1, 0, nReadBuf1, buf2, 0, nReadBuf2)

        if (mmPos < 0) {
          runningTotal += nReadBuf1 // no mismatch, continue looping
        } else {
          mismatchedAt = runningTotal + mmPos
          done = true
        }
      }
    }

    mismatchedAt
  }

  def mismatch(path: Path, path2: Path): Long = {
    if (path.equals(path2)) -1
    else {
      val is1 = Files.newInputStream(path, Array.empty)
      try {
        val is2 = Files.newInputStream(path2, Array.empty)
        try {
          mismatchCore(is1, is2)
        } finally
          is2.close()
      } finally is1.close()
    }
  }

  def move(source: Path, target: Path, options: Array[CopyOption]): Path = {
    lazy val replaceExisting = options.contains(REPLACE_EXISTING)

    if (!exists(source.toAbsolutePath(), Array(LinkOption.NOFOLLOW_LINKS))) {
      throw new NoSuchFileException(source.toString)
    } else if (!exists(
          target.toAbsolutePath(),
          Array.empty
        ) || replaceExisting) {
      moveImpl(source, target, replaceExisting)
    } else {
      throw new FileAlreadyExistsException(target.toString)
    }
    target
  }

  private def moveImpl(
      source: Path,
      target: Path,
      replaceExisting: => Boolean
  ) =
    Zone.acquire { implicit z =>
      val sourceAbs = source.toAbsolutePath().toString
      val targetAbs = target.toAbsolutePath().toString

      if (replaceExisting && target.toFile().isDirectory()) {
        val mustDeleteTarget =
          if (isWindows) {
            // We can not replace directory at all, it must be removed first.
            true
          } else {
            // We can not replace a directory with a file on unix-like.
            (!source.toFile().isDirectory())
          }

        if (mustDeleteTarget)
          Files.delete(target) // will detect & throw if target is not empty.
      }

      if (isWindows) {
        val sourceCString = toCWideStringUTF16LE(sourceAbs)
        val targetCString = toCWideStringUTF16LE(targetAbs)

        // stdio.rename on Windows does not replace existing file
        if (replaceExisting && target.toFile().isDirectory())
          Files.delete(target)

        val flags = {
          val replace =
            if (replaceExisting) MOVEFILE_REPLACE_EXISTING else 0.toUInt
          MOVEFILE_COPY_ALLOWED | // Allow coping betwen volumes
            MOVEFILE_WRITE_THROUGH | // Block until actually moved
            replace
        }
        if (!MoveFileExW(sourceCString, targetCString, flags)) {
          GetLastError() match {
            case ErrorCodes.ERROR_SUCCESS => ()
            case _ => throw WindowsException.onPath(target.toString())
          }
        }
      } else {
        val sourceCString = toCString(sourceAbs)
        val targetCString = toCString(targetAbs)
        if (stdio.rename(sourceCString, targetCString) != 0) {
          throw UnixException(target.toString, errno)
        }
      }
    }

  def newBufferedReader(path: Path): BufferedReader =
    newBufferedReader(path, StandardCharsets.UTF_8)

  def newBufferedReader(path: Path, cs: Charset): BufferedReader =
    new BufferedReader(
      new InputStreamReader(newInputStream(path, Array.empty), cs)
    )

  def newBufferedWriter(
      path: Path,
      cs: Charset,
      options: Array[OpenOption]
  ): BufferedWriter = {
    new BufferedWriter(
      new OutputStreamWriter(newOutputStream(path, options), cs)
    )
  }

  def newBufferedWriter(
      path: Path,
      options: Array[OpenOption]
  ): BufferedWriter =
    newBufferedWriter(path, StandardCharsets.UTF_8, options)

  def newByteChannel(
      path: Path,
      _options: Array[OpenOption]
  ): SeekableByteChannel = {
    val options = new HashSet[OpenOption]()
    _options.foreach(options.add _)
    newByteChannel(path, options, Array.empty)
  }

  def newByteChannel(
      path: Path,
      options: Set[_ <: OpenOption],
      attrs: Array[FileAttribute[_]]
  ): SeekableByteChannel =
    path.getFileSystem().provider().newByteChannel(path, options, attrs)

  def newDirectoryStream(dir: Path): DirectoryStream[Path] = {
    val filter = new DirectoryStream.Filter[Path] {
      override def accept(p: Path): Boolean = true
    }
    newDirectoryStream(dir, filter)
  }

  def newDirectoryStream(
      dir: Path,
      filter: DirectoryStream.Filter[_ >: Path]
  ): DirectoryStream[Path] =
    dir.getFileSystem().provider().newDirectoryStream(dir, filter)

  def newDirectoryStream(dir: Path, glob: String): DirectoryStream[Path] = {
    val filter = new DirectoryStream.Filter[Path] {
      private val matcher =
        FileSystems.getDefault().getPathMatcher("glob:" + glob)

      /* Fix Issue 2937 - Java considers "" & "./" to be the same: current
       * default directory. To ease comparison here and follow JDK practice,
       * change "./" to "" on candidate path. See related "" to "./ "
       * comment in "def list()" above.
       */
      override def accept(p: Path): Boolean = matcher.matches(p.normalize())
    }
    newDirectoryStream(dir, filter)
  }

  def newInputStream(path: Path, options: Array[OpenOption]): InputStream =
    path.getFileSystem().provider().newInputStream(path, options)

  def newOutputStream(path: Path, options: Array[OpenOption]): OutputStream =
    path.getFileSystem().provider().newOutputStream(path, options)

  def notExists(path: Path, options: Array[LinkOption]): Boolean =
    !exists(path, options)

  def readAllBytes(path: Path): Array[Byte] = Zone.acquire { implicit z =>
    /* if 'path' does not exist at all, should get
     * java.nio.file.NoSuchFileException here.
     */
    val pathSize: Long = size(path)
    if (!pathSize.isValidInt) {
      throw new OutOfMemoryError("Required array size too large")
    }

    if (pathSize == 0L) { // SN Issue #I4384, part 1.
      Array.empty[Byte]
    } else {
      val len = pathSize.toInt
      val bytes = scala.scalanative.runtime.ByteArray.alloc(len)

      if (isWindows) {
        val bytesRead = stackalloc[DWord]()

        withFileOpen(
          path.toString,
          access = FILE_GENERIC_READ,
          shareMode = FILE_SHARE_READ
        ) { handle =>
          if (!FileApi.ReadFile(
                handle,
                bytes.at(0),
                pathSize.toUInt,
                bytesRead,
                null
              )) {
            throw WindowsException.onPath(path.toString())
          }
        }
      } else {
        errno = 0
        val pathCString = toCString(path.toString)
        val fd = fcntl.open(pathCString, fcntl.O_RDONLY, 0.toUInt)

        if (fd == -1) {
          val msg = fromCString(string.strerror(errno))
          throw new IOException(s"error opening path '${path}': ${msg}")
        }

        try {
          var offset = 0
          var read = 0
          while ({
            read = unistd.read(fd, bytes.at(offset), (len - offset).toUInt);
            read != -1 && (offset + read) < len
          }) {
            offset += read
          }

          if (read == -1)
            throw UnixException(path.toString, errno)
        } finally {
          unistd.close(fd)
        }
      }

      bytes.asInstanceOf[Array[Byte]]
    }
  }

  def readAllLines(path: Path): List[String] =
    readAllLines(path, StandardCharsets.UTF_8)

  def readAllLines(path: Path, cs: Charset): List[String] = {
    val list = new LinkedList[String]()
    val reader = newBufferedReader(path, cs)
    try {
      val lines = reader.lines().iterator()
      while (lines.hasNext()) {
        list.add(lines.next())
      }
    } finally {
      reader.close()
    }
    list
  }

  def readAttributes[A <: BasicFileAttributes](
      path: Path,
      tpe: Class[A],
      options: Array[LinkOption]
  ): A = {
    val viewClass = {
      if (!attributesClassesToViews.containsKey(tpe))
        throw new UnsupportedOperationException()
      attributesClassesToViews.get(tpe)
    }
    val view = getFileAttributeView(path, viewClass, options)
    view.readAttributes().asInstanceOf[A]
  }

  def readAttributes(
      path: Path,
      attributes: String,
      options: Array[LinkOption]
  ): Map[String, Object] = {
    val parts = attributes.split(":")
    val (viewName, atts) =
      if (parts.length == 1) ("basic", parts(0))
      else (parts(0), parts(1))

    if (atts == "*") {
      val viewClass = {
        if (!viewNamesToClasses.containsKey(viewName))
          throw new UnsupportedOperationException()
        viewNamesToClasses.get(viewName)
      }
      getFileAttributeView(path, viewClass, options).asMap
    } else {
      val attrs = atts.split(",")
      val map = new HashMap[String, Object]()
      attrs.foreach { att =>
        val value = getAttribute(path, viewName + ":" + att, options)
        if (value != null)
          map.put(att, value)
      }
      map
    }
  }

  // Since: Java 11
  def readString(path: Path): String = {
    readString(path, StandardCharsets.UTF_8)
  }

  // Since: Java 11
  def readString(path: Path, cs: Charset): String = {
    val reader = newBufferedReader(path, cs)
    try {
      // Guess an cost-effective amortized size.
      val writer = new StringWriter(2 * 1024)
      reader.transferTo(writer)
      writer.toString()
      // No need to close() StringWriter, so no inner try/finally.
    } finally reader.close()
  }

  def readSymbolicLink(link: Path): Path =
    if (!isSymbolicLink(link)) {
      throw new NotLinkException(link.toString)
    } else
      Zone.acquire { implicit z =>
        val name = if (isWindows) {
          withFileOpen(
            link.toString,
            access = FILE_GENERIC_READ
          ) { handle =>
            val bufferSize = FileApiExt.MAX_PATH
            val buffer: Ptr[WChar] = alloc[WChar](bufferSize)
            val pathSize =
              FileApi.GetFinalPathNameByHandleW(
                handle,
                buffer,
                bufferSize,
                FileApiExt.FILE_NAME_NORMALIZED
              )
            if (pathSize > bufferSize) {
              throw WindowsException(
                "Target path size of link was greater then max allowed size"
              )
            }
            fromCWideString(buffer, StandardCharsets.UTF_16LE)
          }
        } else {
          val buf: CString = alloc[Byte](limits.PATH_MAX)
          if (unistd.readlink(
                toCString(link.toString),
                buf,
                limits.PATH_MAX - 1.toUInt
              ) == -1) {
            throw UnixException(link.toString, errno)
          }
          fromCString(buf)
        }
        Paths.get(name, Array.empty)
      }

  def setAttribute(
      path: Path,
      attribute: String,
      value: AnyRef,
      options: Array[LinkOption]
  ): Path = {
    val sepIndex = attribute.indexOf(":")
    val (viewName, attrName) =
      if (sepIndex == -1) ("basic", attribute)
      else
        (
          attribute.substring(0, sepIndex),
          attribute.substring(sepIndex + 1, attribute.length)
        )
    val viewClass = {
      if (!viewNamesToClasses.containsKey(viewName))
        throw new UnsupportedOperationException()
      viewNamesToClasses.get(viewName)
    }
    val view = getFileAttributeView(path, viewClass, options)
    view.setAttribute(attrName, value)
    path
  }

  def setLastModifiedTime(path: Path, time: FileTime): Path = {
    val view =
      getFileAttributeView(path, classOf[BasicFileAttributeView], Array.empty)
    view.setTimes(time, null, null)
    path
  }

  def setOwner(path: Path, owner: UserPrincipal): Path = {
    val view =
      getFileAttributeView(path, classOf[FileOwnerAttributeView], Array.empty)
    view.setOwner(owner)
    path
  }

  def setPosixFilePermissions(
      path: Path,
      perms: Set[PosixFilePermission]
  ): Path = {
    val view =
      getFileAttributeView(path, classOf[PosixFileAttributeView], Array.empty)
    view.setPermissions(perms)
    path
  }

  def size(path: Path): Long =
    getAttribute(path, "basic:size", Array.empty).asInstanceOf[Long]

  def walk(start: Path, options: Array[FileVisitOption]): Stream[Path] =
    walk(start, Int.MaxValue, options)

  def walk(
      start: Path,
      maxDepth: Int,
      options: Array[FileVisitOption]
  ): Stream[Path] = {
    Objects.requireNonNull(start, "start is null")
    if (maxDepth < 0)
      throw new IllegalArgumentException("'maxDepth' is negative")

    val followLinks = options.contains(FileVisitOption.FOLLOW_LINKS)

    FileTreeWalker(start, maxDepth, followLinks).stream()
  }

  def walkFileTree(start: Path, visitor: FileVisitor[_ >: Path]): Path =
    walkFileTree(
      start,
      EnumSet.noneOf(classOf[FileVisitOption]),
      Int.MaxValue,
      visitor
    )

  def walkFileTree(
      start: Path,
      options: Set[FileVisitOption],
      maxDepth: Int,
      visitor: FileVisitor[_ >: Path]
  ): Path = {
    Objects.requireNonNull(start, "start is null")
    if (maxDepth < 0)
      throw new IllegalArgumentException("'maxDepth' is negative")
    Objects.requireNonNull(visitor, "visitor is null")

    val followLinks = options.contains(FileVisitOption.FOLLOW_LINKS)

    FileTreeWalker(start, maxDepth, followLinks, visitor).walk()

    start
  }

  def write(
      path: Path,
      bytes: Array[Byte],
      _options: Array[OpenOption]
  ): Path = {
    val options =
      if (_options.isEmpty)
        Array[OpenOption](
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE
        )
      else _options

    val out = newOutputStream(path, options)
    out.write(bytes)
    out.close()
    path
  }

  def write(
      path: Path,
      lines: Iterable[_ <: CharSequence],
      cs: Charset,
      _options: Array[OpenOption]
  ): Path = {
    val options =
      if (_options.isEmpty)
        Array[OpenOption](
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE
        )
      else _options
    val writer = newBufferedWriter(path, cs, options)
    val it = lines.iterator()
    while (it.hasNext()) {
      writer.append(it.next())
      writer.newLine()
    }
    writer.close()
    path
  }

  def write(
      path: Path,
      lines: Iterable[_ <: CharSequence],
      options: Array[OpenOption]
  ): Path =
    write(path, lines, StandardCharsets.UTF_8, options)

  private def setAttributes(path: Path, attrs: Array[FileAttribute[_]]): Unit =
    attrs.map(a => (a.name(), a.value())).toMap.foreach {
      case (name, value) =>
        setAttribute(path, name, value.asInstanceOf[AnyRef], Array.empty)
    }

  private val attributesClassesToViews: Map[Class[
    _ <: BasicFileAttributes
  ], Class[_ <: BasicFileAttributeView]] = {
    type HMK = Class[_ <: BasicFileAttributes]
    type HMV = Class[_ <: BasicFileAttributeView]

    val map = new HashMap[HMK, HMV]()
    map.put(classOf[BasicFileAttributes], classOf[BasicFileAttributeView])
    map.put(classOf[DosFileAttributes], classOf[DosFileAttributeView])
    map.put(classOf[PosixFileAttributes], classOf[PosixFileAttributeView])

    map
  }

  private val viewNamesToClasses: Map[String, Class[_ <: FileAttributeView]] = {
    val map = new HashMap[String, Class[_ <: FileAttributeView]]()

    map.put("acl", classOf[AclFileAttributeView])
    map.put("basic", classOf[BasicFileAttributeView])
    map.put("dos", classOf[DosFileAttributeView])
    map.put("owner", classOf[FileOwnerAttributeView])
    map.put("user", classOf[UserDefinedFileAttributeView])
    map.put("posix", classOf[PosixFileAttributeView])

    map
  }

  // Since: Java 11
  def writeString(
      path: Path,
      csq: java.lang.CharSequence,
      cs: Charset,
      options: Array[OpenOption]
  ): Path = {
    import java.io.Reader

    // Java API has no CharSequenceReader, but the concept is useful here.
    class CharSequenceReader(csq: CharSequence) extends Reader {
      private var closed = false
      private var pos = 0

      override def close(): Unit = closed = true

      override def read(cbuf: Array[Char], off: Int, len: Int): Int = {
        if (closed)
          throw new IOException("Operation on closed stream")

        if (off < 0 || len < 0 || len > cbuf.length - off)
          throw new IndexOutOfBoundsException

        if (len == 0) 0
        else {
          val count = Math.min(len, csq.length() - pos)
          var i = 0
          while (i < count) {
            cbuf(off + i) = csq.charAt(pos + i)
            i += 1
          }
          pos += count
          if (count == 0) -1 else count
        }
      }
    }

    val reader = new CharSequenceReader(csq)
    val writer = newBufferedWriter(path, cs, options)
    try {
      reader.transferTo(writer)
      // No need to close() CharSequenceReader, so no inner try/finally.
    } finally
      writer.close()

    path
  }

  // Since: Java 11
  def writeString(
      path: Path,
      csq: java.lang.CharSequence,
      options: Array[OpenOption]
  ): Path = {
    writeString(path, csq, StandardCharsets.UTF_8, options)
  }

}
