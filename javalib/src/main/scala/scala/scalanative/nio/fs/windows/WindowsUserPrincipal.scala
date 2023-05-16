package scala.scalanative.nio.fs.windows

import java.nio.charset.StandardCharsets
import java.nio.file.attribute._
import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.windows._
import java.nio.file.WindowsException

sealed trait WindowsUserPrincipal extends UserPrincipal

object WindowsUserPrincipal {
  import SecurityBaseApi._
  import WinBaseApi._
  import winnt.SidNameUse

  case class User(sidString: String, accountName: String, sidType: SidNameUse)
      extends WindowsUserPrincipal {
    def getName(): String = accountName
  }
  class Group(sidString: String, accountName: String, sidType: SidNameUse)
      extends User(sidString, accountName, sidType)
      with GroupPrincipal

  def apply(sidRef: SIDPtr): WindowsUserPrincipal = {
    import SidNameUse._
    val sidString = {
      val sidCString = stackalloc[CWString]()
      if (!SddlApi.ConvertSidToStringSidW(sidRef, sidCString)) {
        throw WindowsException("Unable to convert SID to string")
      }
      fromCWideString(!sidCString, StandardCharsets.UTF_16LE)
    }

    val (accountName, accountType) =
      try {
        val nameSize, domainSize = stackalloc[DWord]()
        !nameSize = 255.toUInt
        !domainSize = 255.toUInt
        val nameRef: Ptr[WChar] = stackalloc[WChar](!nameSize)
        val domainRef: Ptr[WChar] = stackalloc[WChar](!domainSize)
        val useRef = stackalloc[SidNameUse]()
        if (!LookupAccountSidW(
              systemName = null,
              sid = sidRef,
              name = nameRef,
              nameSize = nameSize,
              referencedDomainName = domainRef,
              referencedDomainNameSize = domainSize,
              use = useRef
            )) {
          throw WindowsException("Failed to lookup account info")
        }

        val accountName = {
          val charset = StandardCharsets.UTF_16LE
          fromCWideString(domainRef, charset) +
            "\\" +
            fromCWideString(nameRef, charset)
        }
        (accountName, !useRef)
      } catch {
        case ex: WindowsException => (sidString, SidTypeUnknown)
      }

    val groupTypes = SidTypeGroup | SidTypeWellKnownGroup | SidTypeAlias
    val isGroup = (accountType & groupTypes) != 0
    if (isGroup)
      new Group(sidString, accountName, accountType)
    else
      new User(sidString, accountName, accountType)
  }
}
