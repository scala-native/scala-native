package scala.scalanative.nio.fs.windows

import scalanative.unsigned.*
import scalanative.unsafe.*
import scalanative.windows.*
import scalanative.windows.WinBaseApi.*
import scalanative.windows.winnt.SidNameUse
import scalanative.windows.SecurityBaseApi.*
import java.nio.file.attribute.*
import java.nio.file.WindowsException
import scala.util.*

object WindowsUserPrincipalLookupService extends UserPrincipalLookupService {
  override def lookupPrincipalByName(
      name: String
  ): WindowsUserPrincipal.User = {
    lookupByName(name) match {
      case Success(user: WindowsUserPrincipal.User) => user
      case other                                    =>
        throw new UserPrincipalNotFoundException(name)
    }
  }

  override def lookupPrincipalByGroupName(
      group: String
  ): WindowsUserPrincipal.Group = {
    lookupByName(group) match {
      case Success(group: WindowsUserPrincipal.Group) =>
        group
      case _ => throw new UserPrincipalNotFoundException(group)
    }
  }

  private def lookupByName(name: String): Try[WindowsUserPrincipal] =
    Zone.acquire { implicit z =>
      val cbSid, domainSize = stackalloc[DWord]()
      !cbSid = 0.toUInt
      !domainSize = 0.toUInt

      val useRef = alloc[SidNameUse]()
      val accountName = toCWideStringUTF16LE(name).asInstanceOf[CWString]
      LookupAccountNameW(
        systemName = null,
        accountName = accountName,
        sid = null,
        cbSid = cbSid,
        referencedDomainName = null,
        referencedDomainNameSize = domainSize,
        use = useRef
      )
      if ((!cbSid).toInt <= 0 || (!domainSize).toInt <= 0) {
        Failure(
          WindowsException("Failed to lookup buffer sizes for acount name")
        )
      } else {
        val sidRef: SIDPtr = alloc[Byte](!cbSid)
        val domainName: CWString =
          alloc[CChar16](!domainSize).asInstanceOf[CWString]

        if (!LookupAccountNameW(
              systemName = null,
              accountName = accountName,
              sid = sidRef,
              cbSid = cbSid,
              referencedDomainName = domainName,
              referencedDomainNameSize = domainSize,
              use = useRef
            )) {
          Failure(WindowsException("Failed to lookup sid for account name"))
        } else Try(WindowsUserPrincipal(sidRef))
      }
    }
}
