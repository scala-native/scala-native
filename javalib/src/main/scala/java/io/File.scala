package java.io

import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.{FileSystems, Path}
import java.nio.file.WindowsException
import java.util.ScalaOps._
import java.util.WindowsHelperMethods._
import scala.annotation.tailrec
import scala.scalanative.annotation.{alwaysinline, stub}
import scala.scalanative.libc._
import scala.scalanative.libc.stdio._
import scala.scalanative.libc.stdlib._
import scala.scalanative.libc.string._
import scala.scalanative.nio.fs.FileHelpers
import scala.scalanative.posix.sys.stat
import scala.scalanative.posix.unistd._
import scala.scalanative.posix.{limits, unistd, utime}
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.runtime.{DeleteOnExit, Platform}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows
import windows._
import windows.MinWinBaseApi.{FileTime => WinFileTime}
import windows.MinWinBaseApiOps.FileTimeOps._
import windows.WinBaseApi._
import windows.WinBaseApiExt._
import windows.SecurityBaseApi._
import windows.SecurityBaseApiOps._
import windows.AclApi._
import windows.HandleApi._
import windows.HandleApiExt._
import windows.FileApi._
import windows.FileApiExt._
import windows.winnt.{HelperMethods => WinNtHelperMethods, _}
import windows.winnt.AccessRights._
import windows.winnt.AccessToken._
import windows.winnt.TokenInformationClass
import windows.accctrl._

class File(_path: String) extends Serializable with Comparable[File] {
  import File._

  if (_path == null) throw new NullPointerException()
  private val path: String = fixSlashes(_path)
  private[io] val properPath: String = File.properPath(path)

  def this(parent: String, child: String) =
    this(
      Option(parent).map(p => p + File.separatorChar + child).getOrElse(child)
    )

  def this(parent: File, child: String) =
    this(Option(parent).map(_.path).orNull, child)

  def this(uri: URI) =
    this(File.checkURI(uri).getPath())

  def compareTo(file: File): Int = {
    if (caseSensitive) getPath().compareTo(file.getPath())
    else getPath().compareToIgnoreCase(file.getPath())
  }

  def canExecute(): Boolean =
    Zone { implicit z =>
      if (isWindows) path.nonEmpty && exists()
      else access(toCString(path), unistd.X_OK) == 0
    }

  def canRead(): Boolean =
    Zone { implicit z =>
      if (isWindows) checkWindowsAccess(FILE_GENERIC_READ)
      else access(toCString(path), unistd.R_OK) == 0
    }

  def canWrite(): Boolean =
    Zone { implicit z =>
      if (isWindows)
        checkWindowsAccess(FILE_GENERIC_WRITE) &&
          fileAttributeIsSet(FILE_ATTRIBUTE_READONLY, checkIsNotSet = true)
      else access(toCString(path), unistd.W_OK) == 0
    }

  def setExecutable(executable: Boolean): Boolean =
    setExecutable(executable, ownerOnly = true)

  def setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean = {
    if (isWindows) {
      // Windows (JVM) does not allow for setting a file as not executable
      if (!executable) false
      else {
        val accessRights = FILE_GENERIC_EXECUTE
        updatePermissionsWindows(accessRights, executable, ownerOnly)
      }
    } else {
      import stat._
      val mask = if (!ownerOnly) S_IXUSR | S_IXGRP | S_IXOTH else S_IXUSR
      updatePermissionsUnix(mask, executable)
    }
  }

  def setReadable(readable: Boolean): Boolean =
    setReadable(readable, ownerOnly = true)

  def setReadable(readable: Boolean, ownerOnly: Boolean): Boolean = {
    if (isWindows) {
      val accessRights = FILE_GENERIC_READ
      updatePermissionsWindows(accessRights, readable, ownerOnly)
    } else {
      import stat._
      val mask = if (!ownerOnly) S_IRUSR | S_IRGRP | S_IROTH else S_IRUSR
      updatePermissionsUnix(mask, readable)
    }
  }

  def setWritable(writable: Boolean): Boolean =
    setWritable(writable, ownerOnly = true)

  def setWritable(writable: Boolean, ownerOnly: Boolean = true): Boolean = {
    if (isWindows) {
      val accessRights = FILE_GENERIC_WRITE
      updatePermissionsWindows(accessRights, writable, ownerOnly)
    } else {
      import stat._
      val mask = if (!ownerOnly) S_IWUSR | S_IWGRP | S_IWOTH else S_IWUSR
      updatePermissionsUnix(mask, writable)
    }
  }

  private def updatePermissionsUnix(
      mask: stat.mode_t,
      grant: Boolean
  ): Boolean =
    Zone { implicit z =>
      if (grant) {
        stat.chmod(toCString(path), accessMode() | mask) == 0
      } else {
        stat.chmod(toCString(path), accessMode() & (~mask)) == 0
      }
    }

  private def updatePermissionsWindows(
      accessRights: windows.DWord,
      grant: Boolean,
      ownerOnly: Boolean
  ): Boolean =
    Zone { implicit z =>
      val filename = toCWideStringUTF16LE(properPath)
      val securityDescriptorPtr = alloc[Ptr[SecurityDescriptor]]()
      val previousDacl, newDacl = alloc[ACLPtr]()
      val usersGroupSid = alloc[SIDPtr]()

      val accessMode: AccessMode =
        if (grant) AccessMode.GRANT_ACCESS
        else AccessMode.DENY_ACCESS

      def getSecurityDescriptor() =
        GetNamedSecurityInfoW(
          objectName = filename,
          objectType = SE_FILE_OBJECT,
          securityInfo = DACL_SECURITY_INFORMATION,
          sidOwner = null,
          sidGroup = null,
          dacl = previousDacl,
          sacl = null,
          securityDescriptor = securityDescriptorPtr
        ) == 0.toUInt

      def setupNewAclEntry() = {
        import accctrl.ops._
        val ea = alloc[ExplicitAccessW]()
        ea.accessPermisions = accessRights
        ea.accessMode = accessMode
        ea.inheritence = NO_PROPAGATE_INHERIT_ACE
        ea.trustee.trusteeForm = TrusteeForm.TRUSTEE_IS_SID

        if (ownerOnly) {
          withUserToken(TOKEN_QUERY) { userToken =>
            withTokenInformation(userToken, TokenInformationClass.TokenUser) {
              (data: Ptr[SidAndAttributes]) =>
                ea.trustee.trusteeType = TrusteeType.TRUSTEE_IS_USER
                ea.trustee.sid = data.sid
            }
          }
        } else {
          WinNtHelperMethods.setupUserGroupSid(usersGroupSid)
          ea.trustee.trusteeType = TrusteeType.TRUSTEE_IS_WELL_KNOWN_GROUP
          ea.trustee.sid = !usersGroupSid
        }
        SetEntriesInAclW(1.toUInt, ea, !previousDacl, newDacl) == 0.toUInt
      }

      def assignNewSecurityInfo() =
        SetNamedSecurityInfoW(
          objectName = filename,
          objectType = SE_FILE_OBJECT,
          securityInfo = DACL_SECURITY_INFORMATION,
          sidOwner = null,
          sidGroup = null,
          dacl = !newDacl,
          sacl = null
        ) == 0.toUInt

      try {
        getSecurityDescriptor() &&
        setupNewAclEntry() &&
        assignNewSecurityInfo()
      } finally {
        FreeSid(!usersGroupSid)
      }
    }

  def exists(): Boolean =
    Zone { implicit z =>
      if (isWindows) {
        val filename = toCWideStringUTF16LE(properPath)
        val attrs = GetFileAttributesW(filename)
        val pathExists = attrs != INVALID_FILE_ATTRIBUTES
        val notSymLink = (attrs & FILE_ATTRIBUTE_REPARSE_POINT) == 0.toUInt
        if (notSymLink) // fast path
          pathExists
        else {
          withFileOpen(
            path,
            access = FILE_READ_ATTRIBUTES,
            allowInvalidHandle = true
          )(_ != INVALID_HANDLE_VALUE)
        }
      } else {
        access(toCString(path), unistd.F_OK) == 0
      }
    }

  def toPath(): Path =
    FileSystems.getDefault().getPath(this.getPath(), Array.empty)

  def getPath(): String = path

  def delete(): Boolean =
    if (path.nonEmpty && isDirectory()) {
      deleteDirImpl()
    } else {
      deleteFileImpl()
    }

  private def deleteDirImpl(): Boolean = Zone { implicit z =>
    if (isWindows) {
      RemoveDirectoryW(toCWideStringUTF16LE(properPath))
    } else
      remove(toCString(path)) == 0
  }

  private def deleteFileImpl(): Boolean = Zone { implicit z =>
    if (isWindows) {
      setReadOnlyWindows(enabled = false)
      DeleteFileW(toCWideStringUTF16LE(properPath))
    } else {
      unlink(toCString(path)) == 0
    }
  }

  override def equals(that: Any): Boolean =
    that match {
      case that: File if caseSensitive =>
        this.path == that.path
      case that: File =>
        this.path.toLowerCase == that.path.toLowerCase
      case _ =>
        false
    }

  def getAbsolutePath(): String = properPath

  def getAbsoluteFile(): File = new File(this.getAbsolutePath())

  def getCanonicalPath(): String =
    Zone { implicit z =>
      if (exists()) {
        simplifyExistingPath(properPath)
      } else {
        simplifyNonExistingPath(fromCString(resolve(toCString(properPath))))
      }
    }

  /** Finds the canonical path for `path`, using `realpath`. The file must
   *  exist, because the result of `realpath` doesn't match that of Java on
   *  non-existing file.
   */
  private def simplifyExistingPath(path: String)(implicit z: Zone): String = {
    if (isWindows) {
      val resolvedName: Ptr[WChar] = alloc[WChar](FileApiExt.MAX_PATH)
      GetFullPathNameW(
        toCWideStringUTF16LE(properPath),
        FileApiExt.MAX_PATH,
        resolvedName,
        null
      )
      fromCWideString(resolvedName, StandardCharsets.UTF_16LE)
    } else {
      val resolvedName: Ptr[Byte] = alloc[Byte](limits.PATH_MAX)
      if (realpath(toCString(path), resolvedName) == null) {
        throw new IOException(
          s"realpath can't resolve: ${fromCString(resolvedName)}"
        )
      }
      fromCString(resolvedName)
    }
  }

  /** Finds the canonical path for `path`.
   */
  private def simplifyNonExistingPath(path: String): String = {
    val rootDirectory =
      if (isWindows) path.take(path.indexOf(s":$separator") + 2)
      else separator

    path
      .stripPrefix(rootDirectory)
      .split(separatorChar)
      .foldLeft(List.empty[String]) {
        case (acc, "..") =>
          if (acc.nonEmpty) acc.tail
          else if (isWindows) Nil
          else List("..")
        case (acc, ".") => acc
        case (acc, "")  => acc
        case (acc, seg) => seg :: acc
      }
      .reverse
      .filterNot(_.isEmpty())
      .mkString(rootDirectory, separator, "")
  }

  @throws(classOf[IOException])
  def getCanonicalFile(): File = new File(getCanonicalPath())

  def getName(): String = {
    val separatorIndex: Int = path.lastIndexOf(separatorChar)
    if (separatorIndex < 0) path
    else path.substring(separatorIndex + 1, path.length())
  }

  def getParent(): String = {
    val isAbsolute = this.isAbsolute()
    val rootDirectory =
      if (!isAbsolute) ""
      else if (isWindows) path.substring(0, path.indexOf(s":$separator") + 2)
      else separator

    path
      .stripPrefix(rootDirectory)
      .split(separatorChar)
      .filterNot(_.isEmpty()) match {
      case Array()                 => null
      case Array(_) if !isAbsolute => null
      case parts => parts.init.mkString(rootDirectory, separator, "")
    }
  }

  def getParentFile(): File = {
    val parent = getParent()
    if (parent == null) null
    else new File(parent)
  }

  override def hashCode(): Int =
    if (caseSensitive) path.hashCode ^ 1234321
    else path.toLowerCase.hashCode ^ 1234321

  def isAbsolute(): Boolean =
    File.isAbsolute(path)

  def isDirectory(): Boolean =
    Zone { implicit z =>
      if (isWindows)
        fileAttributeIsSet(FILE_ATTRIBUTE_DIRECTORY)
      else
        stat.S_ISDIR(accessMode()) != 0
    }

  def isFile(): Boolean =
    Zone { implicit z =>
      if (isWindows)
        fileAttributeIsSet(FILE_ATTRIBUTE_DIRECTORY, checkIsNotSet = true)
      else
        stat.S_ISREG(accessMode()) != 0
    }

  def isHidden(): Boolean = {
    if (isWindows)
      fileAttributeIsSet(FILE_ATTRIBUTE_HIDDEN)
    else getName().startsWith(".")
  }

  def lastModified(): Long =
    Zone { implicit z =>
      if (isWindows) {
        withFileOpen(
          path,
          access = FILE_GENERIC_READ,
          allowInvalidHandle = true
        ) {
          case INVALID_HANDLE_VALUE => 0L
          case handle =>
            val lastModified = stackalloc[WinFileTime]()
            GetFileTime(
              handle,
              creationTime = null,
              lastAccessTime = null,
              lastWriteTime = lastModified
            )
            MinWinBaseApiOps.FileTimeOps.toUnixEpochMillis(!lastModified)
        }
      } else {
        import scala.scalanative.posix.sys.statOps.statOps
        val buf = alloc[stat.stat]()
        if (stat.stat(toCString(path), buf) == 0) {
          buf.st_mtime * 1000L
        } else {
          0L
        }
      }
    }

  private def accessMode()(implicit z: Zone): stat.mode_t = {
    val buf = alloc[stat.stat]()
    if (stat.stat(toCString(path), buf) == 0) {
      buf._13
    } else {
      0.toUInt
    }
  }

  @alwaysinline
  private def fileAttributeIsSet(
      flags: windows.DWord,
      checkIsNotSet: Boolean = false
  ): Boolean = Zone { implicit z =>
    GetFileAttributesW(toCWideStringUTF16LE(properPath)) match {
      case INVALID_FILE_ATTRIBUTES => false // File does not exist
      case attrsSet =>
        if (checkIsNotSet)
          (attrsSet & flags) != flags
        else
          (attrsSet & flags) == flags
    }
  }

  def setLastModified(time: Long): Boolean =
    if (time < 0) {
      throw new IllegalArgumentException("Negative time")
    } else
      Zone { implicit z =>
        if (isWindows) {
          withFileOpen(
            path,
            access = FILE_GENERIC_WRITE,
            allowInvalidHandle = true
          ) {
            case INVALID_HANDLE_VALUE => false
            case handle =>
              val lastModified = stackalloc[WinFileTime]()
              !lastModified =
                MinWinBaseApiOps.FileTimeOps.fromUnixEpochMillis(time)
              SetFileTime(
                handle,
                creationTime = null,
                lastAccessTime = null,
                lastWriteTime = lastModified
              )
          }
        } else {
          val statbuf = alloc[stat.stat]()
          if (stat.stat(toCString(path), statbuf) == 0) {
            val timebuf = alloc[utime.utimbuf]()
            import scala.scalanative.posix.sys.statOps.statOps
            timebuf._1 = statbuf.st_mtime
            timebuf._2 = time.toSize / 1000
            utime.utime(toCString(path), timebuf) == 0
          } else {
            false
          }
        }
      }

  def setReadOnly(): Boolean =
    Zone { implicit z =>
      if (isWindows) setReadOnlyWindows(enabled = true)
      else {
        import stat._
        val mask =
          S_ISUID | S_ISGID | S_ISVTX | S_IRUSR | S_IXUSR | S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH
        val newMode = accessMode() & mask
        chmod(toCString(path), newMode) == 0
      }
    }

  private def setReadOnlyWindows(enabled: Boolean)(implicit z: Zone) = {
    val filename = toCWideStringUTF16LE(properPath)
    val currentAttributes = GetFileAttributesW(filename)

    def newAttributes =
      if (enabled) currentAttributes | FILE_ATTRIBUTE_READONLY
      else currentAttributes & ~FILE_ATTRIBUTE_READONLY

    def setNewAttributes() = SetFileAttributesW(filename, newAttributes)

    currentAttributes != INVALID_FILE_ATTRIBUTES && setNewAttributes()
  }

  def length(): Long = Zone { implicit z =>
    if (isWindows) {
      withFileOpen(
        path,
        access = FILE_READ_ATTRIBUTES,
        allowInvalidHandle = true
      ) {
        case INVALID_HANDLE_VALUE => 0L
        case handle =>
          val size = stackalloc[windows.LargeInteger]()
          if (GetFileSizeEx(handle, size)) (!size).toLong
          else 0L
      }
    } else {
      val buf = alloc[stat.stat]()
      if (stat.stat(toCString(path), buf) == 0) {
        buf._6.toLong
      } else {
        0L
      }
    }
  }

  def list(): Array[String] =
    list(FilenameFilter.allPassFilter)

  def list(filter: FilenameFilter): Array[String] = {
    if (!isDirectory() || !canRead()) {
      null
    } else
      Zone { implicit z =>
        val elements =
          FileHelpers.list(properPath, (n, _) => n, allowEmpty = true)
        if (elements == null)
          Array.empty[String]
        else
          elements.filter(filter.accept(this, _))
      }
  }

  def listFiles(): Array[File] =
    listFiles(FilenameFilter.allPassFilter)

  def listFiles(filter: FilenameFilter): Array[File] =
    list(filter).map(new File(this, _))

  def listFiles(filter: FileFilter): Array[File] = {
    val filenameFilter =
      new FilenameFilter {
        override def accept(dir: File, name: String): Boolean =
          filter.accept(new File(dir, name))
      }
    listFiles(filenameFilter)
  }

  def mkdir(): Boolean =
    Zone { implicit z =>
      if (isWindows)
        CreateDirectoryW(
          toCWideStringUTF16LE(properPath),
          securityAttributes = null
        )
      else {
        val mode = octal("0777")
        stat.mkdir(toCString(path), mode) == 0
      }
    }

  def mkdirs(): Boolean =
    if (exists()) {
      false
    } else if (mkdir()) {
      true
    } else {
      val parent = getParentFile()
      if (parent == null) {
        false
      } else {
        parent.mkdirs() && mkdir()
      }
    }

  def createNewFile(): Boolean =
    FileHelpers.createNewFile(path, throwOnError = true)

  def renameTo(dest: File): Boolean =
    Zone { implicit z =>
      rename(toCString(properPath), toCString(dest.properPath)) == 0
    }

  override def toString(): String = path

  def deleteOnExit(): Unit = DeleteOnExit.addFile(this.getAbsolutePath())

  @stub
  def toURL(): java.net.URL = ???

  // Ported from Apache Harmony
  def toURI(): URI = {
    val path = getAbsolutePath().replace('\\', '/')
    if (!path.startsWith("/")) {
      // start with sep.
      new URI(
        "file",
        null,
        new StringBuilder(path.length + 1).append('/').append(path).toString,
        null,
        null
      )
    } else if (path.startsWith("//")) {
      // UNC path
      new URI("file", "", path, null)
    } else {
      new URI("file", null, path, null, null)
    }
  }

  private[this] def checkWindowsAccess(
      access: windows.DWord
  )(implicit zone: Zone): Boolean = {
    // based on this article https://blog.aaronballman.com/2011/08/how-to-check-access-rights/
    val accessStatus = stackalloc[Boolean]()

    def withFileSecurityDescriptor(
        fn: Ptr[SecurityDescriptor] => Unit
    ): Unit = {
      val securityDescriptorPtr = stackalloc[Ptr[SecurityDescriptor]]()
      val filename = toCWideStringUTF16LE(properPath)
      val securityInfo =
        OWNER_SECURITY_INFORMATION |
          GROUP_SECURITY_INFORMATION |
          DACL_SECURITY_INFORMATION

      val result = GetNamedSecurityInfoW(
        filename,
        SE_FILE_OBJECT,
        securityInfo,
        sidOwner = null,
        sidGroup = null,
        dacl = null,
        sacl = null,
        securityDescriptor = securityDescriptorPtr
      )
      if (result != ErrorCodes.ERROR_SUCCESS)
        throw WindowsException(
          s"Cannot retrive file security descriptor",
          errorCode = result
        )
      try {
        fn(!securityDescriptorPtr)
      } finally {
        WinBaseApi.LocalFree(!securityDescriptorPtr)
      }
    }
    if (path.isEmpty() || !exists()) false // JVM complience
    else {
      withFileSecurityDescriptor { securityDescriptor =>
        withImpersonatedToken { impersonatedToken =>
          val genericMapping = stackalloc[GenericMapping]()
          genericMapping.genericRead = FILE_GENERIC_READ
          genericMapping.genericWrite = FILE_GENERIC_WRITE
          genericMapping.genericExecute = FILE_GENERIC_EXECUTE
          genericMapping.genericAll = FILE_GENERIC_ALL

          val accessMask = stackalloc[windows.DWord]()
          !accessMask = access

          val privilegeSetLength = stackalloc[windows.DWord]()
          !privilegeSetLength = emptyPriviligesSize.toUInt

          val privilegeSet: Ptr[Byte] = stackalloc[Byte](!privilegeSetLength)
          memset(privilegeSet, 0, !privilegeSetLength)

          val grantedAcccess = stackalloc[windows.DWord]()
          !grantedAcccess = 0.toUInt

          MapGenericMask(accessMask, genericMapping)
          AccessCheck(
            securityDescriptor = securityDescriptor,
            clientToken = impersonatedToken,
            desiredAccess = access,
            genericMapping = genericMapping,
            privilegeSet = privilegeSet,
            privilegeSetLength = privilegeSetLength,
            grantedAccess = grantedAcccess,
            accessStatus = accessStatus
          )
        }
      }
      !accessStatus
    }

  }
}

object File {

  private val `1U` = 1.toUInt

  private def octal(v: String): UInt =
    Integer.parseInt(v, 8).toUInt

  private def getUserDir(): String =
    Zone { implicit z =>
      if (isWindows) {
        val buffSize = GetCurrentDirectoryW(0.toUInt, null)
        val buff: Ptr[windows.WChar] = alloc[windows.WChar](buffSize + 1.toUInt)
        if (GetCurrentDirectoryW(buffSize, buff) == 0.toUInt) {
          throw WindowsException("error in trying to get user directory")
        }
        fromCWideString(buff, StandardCharsets.UTF_16LE)
      } else {
        val buff: CString = alloc[CChar](4096)
        if (getcwd(buff, 4095.toUInt) == 0.toUInt) {
          val errMsg = fromCString(string.strerror(errno.errno))
          throw new IOException(
            s"error in trying to get user directory - $errMsg"
          )
        }
        fromCString(buff)
      }
    }

  /** The purpose of this method is to take a path and fix the slashes up. This
   *  includes changing them all to the current platforms fileSeparator and
   *  removing duplicates.
   */
  // Ported from Apache Harmony
  private def fixSlashes(path: String): String = {
    val length = path.length
    var newLength = 0

    var uncIndex =
      if (separatorChar == '/') 0 // UNIX world
      else if (length > 2 && path.charAt(1) == ':')
        2 // Windows, but starts with C:...
      else 1 // Possible UNC path name

    var foundSlash = false
    val newPath = path.toCharArray()
    var i = 0
    while (i < length) {
      val currentChar = newPath(i)

      if ((separatorChar == '\\' && currentChar == '\\') || currentChar == '/') {
        // UNC Name requires 2 leading slashes
        if ((foundSlash && i == uncIndex) || !foundSlash) {
          newPath(newLength) = separatorChar
          newLength += 1
          foundSlash = true
        }
      } else {
        // check for leading slashes before a drive
        if (currentChar == ':'
            && uncIndex > 0
            && (newLength == 2 || (newLength == 3 && newPath(
              1
            ) == separatorChar))
            && newPath(0) == separatorChar) {
          newPath(0) = newPath(newLength - 1)
          newLength = 1
          // allow trailing slash after drive letter
          uncIndex = 2
        }
        newPath(newLength) = currentChar
        newLength += 1
        foundSlash = false
      }

      i += 1
    }

    if (foundSlash && (newLength > (uncIndex + 1) || (newLength == 2 && newPath(
          0
        ) != separatorChar))) {
      newLength -= 1
    }

    new String(newPath, 0, newLength)
  }

  /** Returns a string representing the proper path of this file. If this file
   *  path is absolute, the user.dir property is not prepended, otherwise it is.
   */
  // Ported from Apache Harmony
  private def properPath(path: String): String = {
    if (isAbsolute(path)) path
    else
      getUserDir() match {
        case null =>
          throw new IOException(
            "Failed to resolve proper path, no current directory defined"
          )
        case userdir =>
          if (userdir.endsWith(separator)) userdir + path
          else userdir + separator + path
      }
  }

  def isAbsolute(path: String): Boolean =
    if (separatorChar == '\\') { // Windows. Must start with `\\` or `X:(\|/)`
      (path.length > 1 && path.startsWith(separator + separator)) ||
      (path.length > 2 && path(0).isLetter && path(1) == ':' && (path(
        2
      ) == '/' || path(2) == '\\'))
    } else {
      path.length > 0 && path.startsWith(separator)
    }

  /** Resolve a symbolic link. While the path resolves to an existing path, keep
   *  resolving. If an absolute link is found, resolve the parent directories if
   *  resolveAbsolute is true.
   */
  // Ported from Apache Harmony
  private def resolveLink(
      path: CString,
      resolveAbsolute: Boolean,
      restart: Boolean = false
  )(implicit z: Zone): CString = {
    val resolved =
      readLink(path) match {
        // path is not a symlink
        case null =>
          path

        // found an absolute path. continue from there.
        case link if link(0) == separatorChar =>
          if (isWindows)
            if (strncmp(link, c"\\\\?\\", 4.toUInt) == 0)
              path
            else resolveLink(link, resolveAbsolute, restart = resolveAbsolute)
          else
            resolveLink(link, resolveAbsolute, restart = resolveAbsolute)

        // found a relative path. append to the current path, and continue.
        case link =>
          val linkLength = strlen(link)
          val pathLength = strlen(path)
          val `1US` = 1.toUSize
          var last = pathLength - `1US`
          while (path(last) != separatorChar) last -= `1US`
          last += `1US`

          // previous path up to last /, plus result of resolving the link.
          val newPathLength = last + linkLength + `1US`
          val newPath: Ptr[Byte] = alloc[Byte](newPathLength)
          strncpy(newPath, path, last)
          strncat(newPath, link, linkLength)

          resolveLink(newPath, resolveAbsolute, restart)
      }

    if (restart) resolve(resolved)
    else resolved
  }

  @tailrec private def resolve(path: CString, start: UInt = 0.toUInt)(implicit
      z: Zone
  ): CString = {
    val partSize =
      if (isWindows) windows.FileApiExt.MAX_PATH
      else limits.PATH_MAX.toUInt
    val part: CString = alloc[Byte](partSize)
    val `1U` = 1.toUInt
    // Find the next separator
    var i = start
    while (i < strlen(path) && path(i) != separatorChar) i += `1U`

    if (i == strlen(path).toUInt) resolveLink(path, resolveAbsolute = true)
    else {
      // copy path from start to next separator.
      // and resolve that subpart.
      strncpy(part, path, i + `1U`)

      val resolved = resolveLink(part, resolveAbsolute = true)

      strcpy(part, resolved)
      strcat(part, path + i + `1U`)

      if (strncmp(resolved, path, i + `1U`) == 0) {
        // Nothing changed. Continue from the next segment.
        resolve(part, i + `1U`)
      } else {
        // The path has changed. Start over.
        resolve(part, 0.toUInt)
      }
    }

  }

  /** If `link` is a symlink, follows it and returns the path pointed to.
   *  Otherwise, returns `None`.
   */
  private def readLink(link: CString)(implicit z: Zone): CString = {
    val bufferSize: CSize =
      if (isWindows) FileApiExt.MAX_PATH
      else limits.PATH_MAX - `1U`
    val buffer: CString = alloc[Byte](bufferSize)
    if (isWindows) {
      withFileOpen(
        fromCString(link),
        access = FILE_GENERIC_READ,
        attributes = FILE_FLAG_BACKUP_SEMANTICS | FILE_ATTRIBUTE_DIRECTORY,
        allowInvalidHandle = true
      ) { fileHandle =>
        val finalPathFlags = FileApiExt.FILE_NAME_NORMALIZED
        def pathLength = GetFinalPathNameByHandleA(
          fileHandle,
          buffer = buffer,
          bufferSize = bufferSize.toUInt,
          flags = finalPathFlags
        )

        if (fileHandle == HandleApiExt.INVALID_HANDLE_VALUE || pathLength == 0.toUInt)
          null
        else buffer
      }
    } else {
      readlink(link, buffer, bufferSize) match {
        case -1 =>
          null
        case read =>
          // readlink doesn't null-terminate the result.
          buffer(read) = 0.toByte
          buffer
      }
    }
  }

  val pathSeparatorChar: Char = if (isWindows) ';' else ':'
  val pathSeparator: String = pathSeparatorChar.toString
  val separatorChar: Char = if (isWindows) '\\' else '/'
  val separator: String = separatorChar.toString
  private val caseSensitive: Boolean = !isWindows

  def listRoots(): Array[File] = {
    val list = new java.util.ArrayList[File]()
    FileSystems
      .getDefault()
      .getRootDirectories()
      .scalaOps
      .foreach(p => list.add(p.toFile()))
    list.toArray(new Array[File](0))
  }

  @throws(classOf[IOException])
  def createTempFile(prefix: String, suffix: String): File =
    createTempFile(prefix, suffix, null)

  @throws(classOf[IOException])
  def createTempFile(prefix: String, suffix: String, directory: File): File =
    FileHelpers.createTempFile(
      prefix,
      suffix,
      directory,
      minLength = true,
      throwOnError = true
    )

  // Ported from Apache Harmony
  private def checkURI(uri: URI): URI = {
    def throwExc(msg: String): Unit =
      throw new IllegalArgumentException(s"$msg: $uri")
    def compMsg(comp: String): String =
      s"Found $comp component in URI"

    if (!uri.isAbsolute()) {
      throwExc("URI is not absolute")
    } else if (!uri.getRawSchemeSpecificPart().startsWith("/")) {
      throwExc("URI is not hierarchical")
    } else if (uri.getScheme() == null || !(uri.getScheme() == "file")) {
      throwExc("Expected file scheme in URI")
    } else if (uri.getRawPath() == null || uri.getRawPath().length() == 0) {
      throwExc("Expected non-empty path in URI")
    } else if (uri.getRawAuthority() != null) {
      throwExc(compMsg("authority"))
    } else if (uri.getRawQuery() != null) {
      throwExc(compMsg("query"))
    } else if (uri.getRawFragment() != null) {
      throwExc(compMsg("fragment"))
    }
    uri
    // else URI is ok
  }
}
