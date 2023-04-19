package scala.scalanative.nio.fs.windows

import java.nio.file.{LinkOption, Path}
import java.nio.file.attribute._

import scalanative.unsigned._
import scalanative.unsafe._
import scalanative.annotation.stub
import scala.scalanative.windows._
import java.nio.file.WindowsException
import java.{util => ju}

class WindowsAclFileAttributeView(path: Path, options: Array[LinkOption])
    extends AclFileAttributeView {
  import SecurityBaseApi._
  import WinBaseApiExt._
  import AclApi._

  def name(): String = "acl"

  def getOwner(): UserPrincipal =
    Zone { implicit z =>
      val filename = toCWideStringUTF16LE(path.toString)
      val ownerSid = stackalloc[SIDPtr]()

      if (AclApi.GetNamedSecurityInfoW(
            filename,
            SE_FILE_OBJECT,
            OWNER_SECURITY_INFORMATION,
            sidOwner = ownerSid,
            sidGroup = null,
            dacl = null,
            sacl = null,
            securityDescriptor = null
          ) != 0.toUInt) {
        throw WindowsException("Failed to get ownership info")
      }
      WindowsUserPrincipal(!ownerSid)
    }

  def setOwner(owner: UserPrincipal): Unit = Zone { implicit z =>
    val filename = toCWideStringUTF16LE(path.toString)

    val sidCString = owner match {
      case WindowsUserPrincipal.User(sidString, _, _) =>
        toCWideStringUTF16LE(sidString)
      case _ =>
        throw WindowsException(
          "Unsupported user principal type " + owner.getClass.getName
        )
    }
    val newOwnerSid = stackalloc[SIDPtr]()

    if (!SddlApi.ConvertStringSidToSidW(sidCString, newOwnerSid)) {
      throw WindowsException("Cannot convert user principal to sid")
    }

    if (AclApi.SetNamedSecurityInfoW(
          filename,
          SE_FILE_OBJECT,
          OWNER_SECURITY_INFORMATION,
          sidOwner = !newOwnerSid,
          sidGroup = null,
          dacl = null,
          sacl = null
        ) != 0.toUInt) {
      throw WindowsException("Failed to set new owner")
    }
  }

  @stub def getAcl(): ju.List[AclEntry] = ???
  @stub def setAcl(x: ju.List[AclEntry]): Unit = ???
}
