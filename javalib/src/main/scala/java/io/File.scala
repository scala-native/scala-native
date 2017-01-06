package java.io

import scala.transient
import scalanative.native._, stdlib._, stdio._, string._, Nat._
import scalanative.posix.unistd._
import scala.util.control.Breaks._

import java.util.ArrayList

class File private () extends Serializable with Comparable[File] {
  import File._

  private var path: String = null

  def compareTo(file: File): Int = {
    if (caseSensitive) getPath().compareTo(file.getPath())
    else this.getPath().compareToIgnoreCase(file.getPath())
  }

  @transient
  var properPath: Array[Byte] = null

  def this(dir: File, name: String) = {
    this()
    if (name == null) throw new NullPointerException()
    if (dir == null) path = fixSlashes(name)
    else path = calculatePath(dir.getPath(), name)
  }

  def this(dirPath: String) = {
    this()
    path = fixSlashes(dirPath)
  }

  def this(dirPath: String, name: String) = {
    this()
    if (name == null) throw new NullPointerException()
    if (dirPath == null) path = fixSlashes(name)
    else path = calculatePath(dirPath, name)
  }

  def getPath(): String = return path

  private def calculatePath(dirPath: String, dirName: String): String = {
    val path: String = fixSlashes(dirPath)
    if (!dirName.isEmpty || path.isEmpty) {
      var name: String        = fixSlashes(dirName)
      var separatorIndex: Int = 0;

      while (separatorIndex < name.length() &&
             name(separatorIndex) == separatorChar) {
        separatorIndex += 1
      }

      if (separatorIndex > 0) {
        name = name.substring(separatorIndex, name.length())
      }

      val pathLength: Int = path.length()
      if (pathLength > 0 && path(pathLength - 1) == separatorChar) {
        path + name
      } else path + separatorChar + name
    } else path
  }

  def fixSlashes(origPath: String): String = {
    var uncIndex  = 1
    var length    = origPath.length
    var newLength = 0
    if (separatorChar == '/') {
      uncIndex = 0
    } else if (length > 2 && origPath.charAt(1) == ':') {
      uncIndex = 2
    }
    var foundSlash = false
    val newPath    = origPath.toCharArray()
    var i          = 0
    while (i < length) {
      val pathChar = newPath(i)
      if ((separatorChar == '\\' && pathChar == '\\') || pathChar == '/') {
        if ((foundSlash && i == uncIndex) || !foundSlash) {
          newPath(newLength) = separatorChar
          newLength += 1
          foundSlash = true
        }
      } else {
        if (pathChar == ':' && uncIndex > 0 &&
            (newLength == 2 || (newLength == 3 && newPath(1) == separatorChar)) &&
            newPath(0) == separatorChar) {
          newPath(0) = newPath(newLength - 1)
          newLength = 1
          uncIndex = 2
        }
        newPath(newLength) = pathChar

        newLength += 1
        foundSlash = false
      }
      i += 1
    }
    if (foundSlash &&
        (newLength > (uncIndex + 1) || (newLength == 2 && newPath(0) != separatorChar))) {
      newLength -= 1
    }
    new String(newPath, 0, newLength)
  }

  def canRead(): Boolean = {
    if (path.length() == 0) {
      return false
    }
    var pp: Array[Byte] = setProperPath()
    return existsImpl(pp) && !isWriteOnlyImpl(pp)
  }

  def canWrite(): Boolean = {
    // Cannot use exists() since that does an unwanted read-check.
    var exists: Boolean = false
    if (path.length() > 0) {
      exists = existsImpl(setProperPath())
    }
    return exists && !isReadOnlyImpl(setProperPath())
  }

  def delete(): Boolean = {
    var propPath: Array[Byte] = setProperPath()
    if ((path.length() != 0) && isDirectoryImpl(propPath)) {
      return deleteDirImpl(propPath)
    }
    return deleteFileImpl(propPath)
  }

  //native funct.
  @throws(classOf[IOException])
  private def deleteDirImpl(filePath: Array[Byte]): Boolean = {
    var pathCopy: CString = filePathCopy(filePath)
    val result: Int       = remove(pathCopy)
    return result == 0
  }

  //native funct.
  @throws(classOf[IOException])
  private def deleteFileImpl(filePath: Array[Byte]): Boolean = {
    var pathCopy: CString = filePathCopy(filePath)
    var result: Int       = unlink(pathCopy)
    return result == 0
  }

  override def equals(obj: Any): Boolean = {
    if (!(obj.isInstanceOf[File])) {
      return false
    }
    if (!caseSensitive) {
      return toLowerCaseNaive(path) equals toLowerCaseNaive(
        (obj.asInstanceOf[File]).getPath())
    }
    return path.equals((obj.asInstanceOf[File]).getPath())
  }

  def exists(): Boolean = {
    if (path.length() == 0) {
      return false;
    }
    return existsImpl(setProperPath());
  }

  //native funct.
  def existsImpl(filePath: Array[Byte]): Boolean = {
    var pathCopy: CString = filePathCopy(filePath)
    return (CFile.fileAttribute(pathCopy) >= 0)
  }

  def getAbsolutePath(): String = HyUtil.toUTF8String(setProperPath())

  def getAbsoluteFile(): File = new File(this.getAbsolutePath())

  @throws(classOf[IOException])
  def getCanonicalPath(): String = {
    var result: Array[Byte] = setProperPath()
    var absPath: String     = HyUtil.toUTF8String(result)
    if (separatorChar == '/') {
      // resolve the full path first
      result = resolveLink(result, result.length, false)
      // resolve the parent directories
      result = resolve(result)
    }
    var numSeparators: Int = 1;
    var i                  = 0
    while (i < result.length) {
      if (result(i) == separatorChar) {
        numSeparators += 1
      }
      i += 1
    }
    var sepLocations: Array[Int] = new Array[Int](numSeparators)
    var rootLoc: Int             = 0
    if (separatorChar != '/') {
      if (result(0) == '\\') {
        rootLoc = if (result.length > 1 && result(1) == '\\') 1 else 0
      } else {
        rootLoc = 2 // skip drive i.e. c:
      }
    }
    var newResult: Array[Byte] = new Array[Byte](result.length + 1)
    var newLength: Int         = 0
    var lastSlash: Int         = 0
    var foundDots: Int         = 0
    sepLocations(lastSlash) = rootLoc

    breakable {
      var i = 0
      while (i <= result.length) {
        if (i < rootLoc) {
          newResult(newLength) = result(i)
          newLength += 1
        } else {
          if (i == result.length || result(i) == separatorChar) {
            if (i == result.length && foundDots == 0) {
              break
            }
            if (foundDots == 1) {
              /* Don't write anything, just reset and continue */
              foundDots = 0
            } else if (foundDots > 1) {
              /* Go back N levels */
              lastSlash =
                if (lastSlash > (foundDots - 1))
                  lastSlash - (foundDots - 1)
                else 0
              newLength = sepLocations(lastSlash) + 1
              foundDots = 0
            } else {
              lastSlash += 1
              sepLocations(lastSlash) = newLength
              newResult(newLength) = separatorChar.toByte
              newLength += 1
            }
          } else if (result(i) == '.') {
            foundDots += 1
          } else {
            if (foundDots > 0) {
              var j = 0
              while (j < foundDots) {
                newResult(newLength) = '.'.toByte
                newLength += 1
                j += 1
              }
            }
            newResult(newLength) = result(i)
            newLength += 1
            foundDots = 0
          }
        }
        i += 1
      }
    }

    if (newLength > (rootLoc + 1)
        && newResult(newLength - 1) == separatorChar) {
      newLength -= 1
    }
    newResult(newLength) = 0
    //part down here until toUTF8String is equivalent to getCanonImpl from harmony
    val pathCopy  = filePathCopy(newResult)
    val answerlen = strlen(pathCopy).toInt
    val answer    = new Array[Byte](answerlen)
    var k         = 0
    while (k < answerlen) {
      answer(k) = pathCopy(k)
      k += 1
    }
    return HyUtil.toUTF8String(answer, 0, answerlen)
  }

  @throws(classOf[IOException])
  private def resolve(newResult: Array[Byte]): Array[Byte] = {
    var last: Int = 1

    var nextSize: Int         = 0
    var linkSize: Int         = 0
    var linkPath: Array[Byte] = newResult
    var bytes: Array[Byte]    = null
    var done: Boolean         = false
    var inPlace: Boolean      = false
    var i                     = 1
    while (i <= newResult.length) {
      if (i == newResult.length || newResult(i) == separatorChar) {
        done = (i >= (newResult.length - 1))
        // if there is only one segment, do nothing
        if (done && linkPath.length == 1) {
          return newResult
        }
        inPlace = false
        if (linkPath == newResult) {
          bytes = newResult
          // if there are no symbolic links, terminate the C string
          // instead of copying
          if (!done) {
            inPlace = true
            newResult(i) = '\0'
          }
        } else {
          nextSize = i - last + 1
          linkSize = linkPath.length
          if (linkPath(linkSize - 1) == separatorChar) {
            linkSize -= 1
          }
          bytes = new Array[Byte](linkSize + nextSize)
          System.arraycopy(linkPath, 0, bytes, 0, linkSize);
          System.arraycopy(newResult, last - 1, bytes, linkSize, nextSize)
          // the full path has already been resolved
        }
        if (done) {
          return bytes
        }
        linkPath = resolveLink(bytes, if (inPlace) i else bytes.length, true);
        if (inPlace) {
          newResult(i) = '/'
        }
        last = i + 1
      }
      i += 1
    }
    throw new InternalError();
  }

  @throws(classOf[IOException])
  private def resolveLink(pathBytesGiven: Array[Byte],
                          length: Int,
                          resolveAbsolute: Boolean): Array[Byte] = {
    var pathBytes: Array[Byte] = pathBytesGiven
    var restart: Boolean       = false

    //previously uninitialized
    var linkBytes: Array[Byte] = null;

    //previously uninitialized
    var temp: Array[Byte] = null;
    try {
      do {
        linkBytes = getLinkImpl(pathBytes)
        if (linkBytes == pathBytes) {
          throw Break
        }
        if (linkBytes(0) == separatorChar) {
          // link to an absolute path, if resolving absolute paths,
          // resolve the parent dirs again
          restart = resolveAbsolute
          pathBytes = linkBytes
        } else {
          var last: Int = length - 1;
          while (pathBytes(last) != separatorChar) {
            last -= 1
          }
          last += 1
          temp = new Array[Byte](last + linkBytes.length)
          System.arraycopy(pathBytes, 0, temp, 0, last);
          System.arraycopy(linkBytes, 0, temp, last, linkBytes.length);
          pathBytes = temp;
        }
        //can't do that in scala.
        //length = pathBytes.length;
      } while (existsImpl(pathBytes))
    } catch {
      case Break          => endOfFunct
      case e: IOException => throw e
    }
    def endOfFunct: Array[Byte] =
      if (restart) resolve(pathBytes) else pathBytes
    endOfFunct
  }

  @throws(classOf[IOException])
  def getCannonicalFile(): File = new File(getCanonicalPath())

  def getName(): String = {
    val separatorIndex: Int = path.lastIndexOf(separatorChar)
    if (separatorIndex < 0) path
    else path.substring(separatorIndex + 1, path.length())
  }

  def getParent(): String = {
    val length: Int      = path.length()
    var firstInPath: Int = 0
    if (separatorChar == '\\' && length > 2 && path(1) == ':') {
      firstInPath = 2
    }
    var index: Int = path.lastIndexOf(separatorChar)
    if (index == -1 && firstInPath > 0) index = 2
    if (index == -1 || path(length - 1) == separatorChar) return null
    if (path.indexOf(separatorChar) == index
        && path(firstInPath) == separatorChar)
      return path.substring(0, index + 1)
    return path.substring(0, index)
  }

  def getParentFile(): File = {
    var tempParent: String = getParent()
    if (tempParent == null) return null
    return new File(tempParent)
  }

  override def hashCode(): Int =
    if (caseSensitive) path.hashCode ^ 1234321
    else toLowerCaseNaive(path).hashCode ^ 1234321

  def isAbsolute(): Boolean = {
    if (separatorChar == '\\') {
      // for windows
      if (path.length() > 1 && path(0) == separatorChar
          && path(1) == separatorChar) {
        return true
      }
      if (path.length() > 2) {
        if ((path(0).isLetter && (path(1) == ':')
            && (path(2) == '/' || path(2) == '\\'))) {
          return true
        }
      }
      return false
    }

    // for Linux
    return (path.length() > 0 && path(0) == separatorChar);
  }

  def isDirectory(): Boolean = {
    if (path.length() == 0) {
      return false
    }
    return isDirectoryImpl(setProperPath())
  }

  //native funct.
  private def isDirectoryImpl(filePath: Array[Byte]): Boolean = {
    var pathCopy: CString = filePathCopy(filePath)
    return (CFile.fileAttribute(pathCopy) == 0)
  }

  def isFile(): Boolean = {
    if (path.length() == 0) {
      return false
    }
    return isFileImpl(setProperPath())
  }

  //native funct.
  private def isFileImpl(filePath: Array[Byte]): Boolean = {
    var pathCopy: CString = filePathCopy(filePath)
    return (CFile.fileAttribute(pathCopy) == 1)
  }

  def isHidden(): Boolean =
    if (path.length() == 0) false
    else isHiddenImpl(setProperPath())

  //native funct.
  private def isHiddenImpl(filePath: Array[Byte]): Boolean = {
    var pathCopy: CString  = filePathCopy(filePath)
    val length: CSize      = strlen(pathCopy)
    val existsResult: CInt = CFile.fileAttribute(pathCopy)
    if (existsResult < 0) return false
    if (length == 0) return true
    var index: Long = length
    while (index >= 0) {
      if (pathCopy(index) == '.' && (index > 0 && (pathCopy(index - 1) == '/')))
        return true
      index -= 1
    }
    return false
  }

  //native funct.
  private def isReadOnlyImpl(filePath: Array[Byte]): Boolean = {
    val pathCopy: CString = filePathCopy(filePath)
    return (access(pathCopy, fcntl.W_OK) != 0)
  }

  //native funct.
  private def isWriteOnlyImpl(filePath: Array[Byte]): Boolean = {
    val pathCopy: CString = filePathCopy(filePath)
    return (access(pathCopy, fcntl.R_OK) != 0)
  }
  //native funct.
  private def getLinkImpl(filePath: Array[Byte]): Array[Byte] = {
    var answer: Array[Byte] = null
    var pathCopy: CString   = filePathCopy(filePath)
    if (platformReadLink(pathCopy)) {
      //need to transform pathCopy into an Array of byte
      val length: CSize = strlen(pathCopy)
      answer = new Array[Byte](length.toInt)
      var index: Int = 0
      while (index < length) {
        answer(index) = pathCopy(index)
        index += 1
      }
    } else {
      answer = filePath
    }
    return answer;
  }

  //scala way to write the C function platformReadLink(char*) in hyfile.c
  private def platformReadLink(link: CString): Boolean = {
    val size: CInt = readlink(link, link, HyMaxPath - 1);
    if (size <= 0) return false
    link(size) = 0
    return true
  }

  def lastModified(): Long = {
    val result: Long = lastModifiedImpl(setProperPath())
    result match {
      case 0  => 0
      case -1 => 0
      case _  => result
    }
  }

  //native funct.
  private def lastModifiedImpl(filePath: Array[Byte]): Long = {
    var pathCopy: CString = filePathCopy(filePath)
    return CFile.lastModNative(pathCopy)
  }

  def setLastModified(time: Long): Boolean = {
    //message corresponding to luni.B2 from apache messages.properties
    if (time < 0) throw new IllegalArgumentException("time must be positive")
    return (setLastModifiedImpl(setProperPath(), time));
  }

  //native funct.
  private def setLastModifiedImpl(filePath: Array[Byte], time: Long): Boolean = {
    var pathCopy: CString = filePathCopy(filePath)
    CFile.setLastModNative(pathCopy, time) match {
      case 0 => false
      case 1 => true
    }
  }

  def setReadOnly(): Boolean = setReadOnlyImpl(setProperPath())

  //native funct.
  def setReadOnlyImpl(path: Array[Byte]): Boolean = {
    var pathCopy: CString = filePathCopy(path)
    CFile.setReadOnlyNative(pathCopy) match {
      case 0 => false
      case 1 => true
    }

  }

  def length(): Long = lengthImpl(setProperPath())

  //native funct.
  private def lengthImpl(filePath: Array[Byte]): Long = {
    val pathCopy: CString = filePathCopy(filePath)
    val result: Long      = CFile.fileLength(pathCopy)
    if (result < 0) {
      return 0L
    } else return result
  }

  def list(): Array[java.lang.String] = {
    if (path.length() == 0) return null
    var bs: Array[Byte] = setProperPath()
    if (!isDirectoryImpl(bs) || !existsImpl(bs) || isWriteOnlyImpl(bs))
      return null

    var implList: Array[Array[Byte]] = listImpl(bs)
    if (implList == null) return new Array[String](0)

    val result: Array[String] = new Array[String](implList.length)
    var index                 = 0
    while (index < implList.length) {
      result(index) = HyUtil.toUTF8String(implList(index))
      index += 1
    }
    return result
  }

  def listFiles(): Array[File] = {
    val tempNames: Array[String] = list()
    if (tempNames == null) return null
    val resultLength         = tempNames.length
    val results: Array[File] = new Array[File](resultLength)
    var i                    = 0
    while (i < resultLength) {
      results(i) = new File(this, tempNames(i))
      i += 1
    }
    return results
  }

  def listFiles(filter: FilenameFilter): Array[File] = {
    val tempNames: Array[String] = list(filter)
    if (tempNames == null) return null
    val resultLength         = tempNames.length
    val results: Array[File] = new Array[File](resultLength)
    var i                    = 0
    while (i < resultLength) {
      results(i) = new File(this, tempNames(i))
      i += 1
    }
    return results
  }

  def listFiles(filter: FileFilter): Array[File] = {
    if (path.length() == 0) return null
    var bs: Array[Byte] = setProperPath()
    if (!isDirectoryImpl(bs) || !existsImpl(bs) || isWriteOnlyImpl(bs))
      return null

    var implList: Array[Array[Byte]] = listImpl(bs)
    if (implList == null) return new Array[File](0)

    val tempResult: ArrayList[File] = new ArrayList[File]()
    var index                       = 0
    while (index < implList.length) {
      val aName = HyUtil.toString(implList(index))
      val aFile = new File(this, aName)
      if (filter == null || filter.accept(aFile)) {
        tempResult.add(aFile)
      }
      index += 1
    }
    return tempResult.toArray(new Array[File](tempResult.size()))

  }

  def list(filter: FilenameFilter): Array[java.lang.String] = {
    if (path.length() == 0) return null
    var bs: Array[Byte] = setProperPath()
    if (!isDirectoryImpl(bs) || !existsImpl(bs) || isWriteOnlyImpl(bs))
      return null

    var implList: Array[Array[Byte]] = listImpl(bs)
    if (implList == null) return new Array[String](0)

    val tempResult: ArrayList[String] = new ArrayList[String]()
    var index                         = 0
    while (index < implList.length) {
      val aName = HyUtil.toString(implList(index))
      if (filter == null || filter.accept(this, aName)) {
        tempResult.add(aName)
      }
      index += 0
    }

    return tempResult.toArray(new Array[String](tempResult.size()))
  }

  //native funct.
  @throws(classOf[IOException])
  private def listImpl(filePath: Array[Byte]): Array[Array[Byte]] =
    synchronized {

      import java.io.File.DirEntryOps
      import java.io.File.DirEntry

      var dirList: Ptr[DirEntry]      = null
      var currentEntry: Ptr[DirEntry] = null

      var result: Int     = 0
      var index: Int      = 0
      var numEntries: Int = 0

      var pathCopy: CString = stackalloc[CChar](HyMaxPath + 1)
      var filename: CString = stackalloc[CChar](HyMaxPath)
      var length: Int       = filePath.length

      var answer: Array[Array[Byte]] = null

      if (length > (HyMaxPath - 1)) {
        throw new IOException(
          "Path length of " + length + " characters exceeds maximum supported length of " + HyMaxPath)
      }
      pathCopy =
        filePath.asInstanceOf[scala.scalanative.runtime.ByteArray].at(0)

      if ((length >= 1) && (pathCopy(length - 1) != '/')
          && pathCopy(length - 1) != '\\') {
        pathCopy(length) = separatorChar.toByte
        length += 1
      }

      var findhandle = CFile.fileFindFirst(pathCopy, filename)

      if (findhandle.cast[Long] == -1)
        return null

      while (result > -1) {
        if (strcmp(toCString("."), filename) != 0 && (strcmp(toCString(".."),
                                                             filename) != 0)) {

          if (numEntries > 0) {
            currentEntry.next = malloc(sizeof[DirEntry]).cast[Ptr[DirEntry]]
            currentEntry = currentEntry.next
          } else {
            dirList = malloc(sizeof[DirEntry]).cast[Ptr[DirEntry]]
            currentEntry = dirList
          }

          if (currentEntry == null) {
            CFile.fileFindClose(findhandle);
            return cleanup
          }
          strcpy(currentEntry.pathEntry, filename)
          numEntries += 1
        }
        result = CFile.fileFindNext(findhandle, filename);
      }
      CFile.fileFindClose(findhandle);

      if (numEntries == 0) return null
      answer = new Array[Array[Byte]](numEntries)

      def cleanup: Array[Array[Byte]] = {
        var fileindex = 0
        while (fileindex < numEntries) {
          val entrylen: Int = strlen(dirList.pathEntry).toInt;
          currentEntry = dirList
          if (answer != null) {
            answer(fileindex) = fromCString(dirList.pathEntry).getBytes
          }
          dirList = dirList.next

          free(currentEntry.cast[Ptr[Byte]])
          fileindex += 1
        }
        return answer
      }

      return cleanup
    }

  def mkdir(): Boolean = mkdirImpl(setProperPath())

  //native funct.
  private def mkdirImpl(filePath: Array[Byte]): Boolean = {
    val pathCopy: CString = filePathCopy(filePath)
    CFile.fileMkDir(pathCopy) == 0
  }

  def mkdirs(): Boolean = {
    if (exists()) return false
    if (mkdir()) return true
    val parentDir: String = getParent()

    if (parentDir == null) return false
    return (new File(parentDir).mkdirs() && mkdir())
  }

  @throws(classOf[IOException])
  def createNewFile(): Boolean = {
    if (path.length() == 0) {
      //corresponds to the entry "luni.B3" of the
      //internal Messages module from apache.
      throw new IOException("No such file or directory")
    }
    val pathCopy: CString = filePathCopy(setProperPath())
    val flag              = (CFileFlags.openCreateNew | CFileFlags.openWrite | CFileFlags.openTruncate)
    val portFD: CInt      = CFile.nativeFileOpen(pathCopy, flag, 438)
    //even without sbt, this print is decisive...
    print("")
    if (portFD == -1) {
      if (errno.errno == CFile.EEXIST) {
        return false
      } else {
        throw new IOException("Cannot create " + path)
      }
    } else {
      CFile.fileDescriptorClose(portFD)
      return true
    }
  }

  /*private[io]*/
  def setProperPath(): Array[Byte] = {
    if (properPath != null) {
      return properPath;
    }
    if (isAbsolute()) {
      var pathBytes: Array[Byte] = HyUtil.getUTF8Bytes(path);
      properPath = pathBytes;
      return properPath
    }
    // Check security by getting user.dir when the path is not absolute

    var userdir: String = getUserDir()
    if (userdir == null) {
      throw new IOException("getcwd() error in trying to get user directory")
    }

    if (path.length() == 0) {
      properPath = HyUtil.getUTF8Bytes(userdir)
      return properPath
    }
    var length: Int = userdir.length()

    // Handle windows-like path
    if (path(0) == '\\') {
      if (length > 1 && userdir(1) == ':') {
        properPath = HyUtil.getUTF8Bytes(userdir.substring(0, 2) + path)
        return properPath
      }
      path = path.substring(1);
    }

    // Handle separator
    var result: String = userdir
    if (userdir(length - 1) != separatorChar) {
      if (path(0) != separatorChar) {
        result += separator
      }
    } else if (path(0) == separatorChar) {
      result = result.substring(0, length - 2)
    }
    result += path;
    properPath = HyUtil.getUTF8Bytes(result)
    return properPath
  }

  private def getUserDir(): String = {
    var buff: CString = stackalloc[CChar](4096)
    var res: CString  = getcwd(buff, 4095)
    fromCString(res)
  }

  def renameTo(des: java.io.File): Boolean =
    renameToImpl(setProperPath(), des.setProperPath())

  //native funct.
  private def renameToImpl(pathExists: Array[Byte],
                           pathNew: Array[Byte]): Boolean = {
    val oldPathCopy: CString = filePathCopy(pathExists)
    val newPathCopy: CString = filePathCopy(pathNew)
    rename(oldPathCopy, newPathCopy) match {
      case 0  => true
      case -1 => false
    }
  }

  override def toString(): String = path

  def getAbsoluteName(): String = {
    val f: File      = getAbsoluteFile()
    var name: String = f.getPath()

    if (f.isDirectory() && name(name.length() - 1) != separatorChar) {
      // Directories must end with a slash
      name = new StringBuilder(name.length() + 1)
        .append(name)
        .append('/')
        .toString()
    }
    if (separatorChar != '/') { // Must convert slashes.
      name = name.replace(separatorChar, '/')
    }
    return name;
  }

}

object File {

  //value class to access the struct DirEntry in a more readable/C-like way.
  implicit class DirEntryOps(val ptr: Ptr[DirEntry]) extends AnyVal {
    @inline def pathEntry: CString =
      ptr._1.cast[CString]

    @inline def next: Ptr[DirEntry] =
      !(ptr._2.cast[Ptr[Ptr[DirEntry]]])

    @inline def next_=(value: Ptr[DirEntry]): Unit =
      !(ptr._2.cast[Ptr[Ptr[DirEntry]]]) = value
  }

  type _1024 = Digit[_1, Digit[_0, Digit[_2, _4]]]

  type PathEntry = CArray[CChar, _1024]

  //neded in listImpl()
  type DirEntry = CStruct2[
    PathEntry, //_1 pathentry
    Ptr[_] //_2 next
  ]

  //HyMaxPath was chosen from unix MAXPATHLEN.
  val HyMaxPath: Int                 = 1024
  val separatorChar: Char            = CFile.separatorChar()
  val pathSeparatorChar: Char        = CFile.pathSeparatorChar()
  val separator: String              = separatorChar.toString
  val pathSeparator: String          = pathSeparatorChar.toString
  private var counter: Int           = 0;
  private var counterBase: Int       = 0;
  private val caseSensitive: Boolean = (CFile.isCaseSensitiveImpl() == 1);

  //temporary workaround, while waiting on a working implementation of toLowerCase
  private def toLowerCaseNaive(str: String): String = {
    def toLowerChar(c: Char): Char = c match {
      case 'A' => 'a'
      case 'B' => 'b'
      case 'C' => 'c'
      case 'D' => 'd'
      case 'E' => 'e'
      case 'F' => 'f'
      case 'G' => 'g'
      case 'H' => 'h'
      case 'I' => 'i'
      case 'J' => 'j'
      case 'K' => 'k'
      case 'L' => 'l'
      case 'M' => 'm'
      case 'N' => 'n'
      case 'O' => 'o'
      case 'P' => 'p'
      case 'Q' => 'q'
      case 'R' => 'r'
      case 'S' => 's'
      case 'T' => 't'
      case 'U' => 'u'
      case 'V' => 'v'
      case 'W' => 'w'
      case 'X' => 'x'
      case 'Y' => 'y'
      case 'Z' => 'z'
      case x   => x
    }
    var chars = str.toCharArray
    var i     = 0
    while (i < chars.length) {
      val lC = toLowerChar(chars(i))
      chars(i) = lC
      i += 1
    }
    return new String(chars)
  }

  //Unix only implementation
  def listRoots(): Array[File] = {
    var array = new Array[File](1)
    array(0) = new File("/")
    return array
  }

  @throws(classOf[IOException])
  def createTempFile(prefix: String, suffix: String): File =
    createTempFile(prefix, suffix, null)

  @throws(classOf[IOException])
  def createTempFile(prefix: String, suffix: String, directory: File): File = {
    // Force a prefix null check first
    if (prefix.length() < 3) {
      throw new IllegalArgumentException(
        "Prefix must be at least 3 characters")
    }
    var newSuffix: String = if (suffix == null) ".tmp" else suffix
    var tmpDirFile: File  = null
    if (directory == null) {
      var tmpDir: String = fromCString(CFile.getTempDir())
      tmpDirFile = new File(tmpDir)
    } else {
      tmpDirFile = directory;
    }
    var result: File = null
    do {
      result = genTempFile(prefix, newSuffix, tmpDirFile)
    } while (!result.createNewFile())
    return result
  }

  @throws(classOf[IOException])
  private def genTempFile(prefix: String,
                          suffix: String,
                          directory: File): File = {
    var identify: Int = 0
    synchronized {
      if (counter == 0) {
        val newInt: Int = new java.util.Random().nextInt()
        counter = ((newInt / 65535) & 0xFFFF) + 0x2710
        counterBase = counter
      }
      counter += 1
      identify = counter
    }

    val newName: StringBuilder = new StringBuilder();
    newName.append(prefix);
    newName.append(counterBase);
    newName.append(identify);
    newName.append(suffix);
    return new File(directory, newName.toString());
  }

  /*
   *Small utilitary function to achieve modularity.
   *transform an Array of Byte to a CString
   *add the null terminating char at the end.
   * can throw an IO exception if the path is too long
   * Doesn't really make a copy per se.
   */
  @throws(classOf[IOException])
  @inline def filePathCopy(filePath: Array[Byte]): CString = {

    val length: Int = filePath.length
    if (length > (HyMaxPath - 1)) {
      throw new IOException(
        "Path length of " + length + " characters exceeds maximum supported length of " + HyMaxPath)
    }
    val pathCopy =
      filePath.asInstanceOf[scala.scalanative.runtime.ByteArray].at(0)
    pathCopy(length) = '\0'
    return pathCopy
  }
}

object fcntl {
  val W_OK: Int = 2
  val R_OK: Int = 4
  val X_OK: Int = 1
  val F_OK: Int = 0
}

//way to handle break in Scala.
object Break extends Exception {}

//TODO:
//private def checkURI(uri : URI): Unit

//def deleteOnExit(): Unit = ??? /*atexit{ () => delete() }*/

/*@throws(classOf[IOException])
private def writeObject(stream: ObjectOutputStream): Unit */

/*@throws(classOf[IOException])
@throws(classOf[ClassNotFoundException])
private def readObject(stream: ObjectInputStream): Unit */

//def toURI(): URI

/*@throws(classOf[java.net.MalformedURLException])
def toURL(): URL*/

/*def File(uri: URI): File = {
    this()
    checkURI(uri)
    path = fixSlashes(uri.getPath())
}*/
