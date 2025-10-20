package scala.scalanative.windows

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*
import HandleApi.Handle
import scala.scalanative.windows.winnt.*

@link("advapi32")
@extern()
object WinBaseApi {
  import SecurityBaseApi.*

  type SecurityInformation = DWord
  type SecurityAttributes = CStruct3[DWord, CVoidPtr, Boolean]
  type CallbackContext = CVoidPtr
  type WaitOrTimerCallback = CFuncPtr2[CallbackContext, Boolean, Unit]
  type LocalHandle = CVoidPtr

  def CreateHardLinkW(
      linkFileName: CWString,
      existingFileName: CWString,
      securityAttributes: Ptr[SecurityAttributes]
  ): Boolean = extern

  def CreateSymbolicLinkW(
      symlinkFileName: CWString,
      targetFileName: CWString,
      flags: DWord
  ): Boolean = extern
  def FormatMessageA(
      flags: DWord,
      source: CVoidPtr,
      messageId: DWord,
      languageId: DWord,
      buffer: Ptr[CWString],
      size: DWord,
      arguments: CVarArgList
  ): DWord = extern

  def FormatMessageW(
      flags: DWord,
      source: CVoidPtr,
      messageId: DWord,
      languageId: DWord,
      buffer: Ptr[CWString],
      size: DWord,
      arguments: CVarArgList
  ): DWord = extern
  def GetCurrentDirectoryA(bufferLength: DWord, buffer: CString): DWord = extern
  def GetCurrentDirectoryW(bufferLength: DWord, buffer: CWString): DWord =
    extern
  def GetFileSecurityW(
      filename: CWString,
      requestedInformation: SecurityInformation,
      securityDescriptor: Ptr[SecurityDescriptor],
      length: DWord,
      lengthNeeded: Ptr[DWord]
  ): Boolean =
    extern

  def LocalFree(ref: LocalHandle): LocalHandle = extern
  def LookupAccountNameA(
      systemName: CString,
      accountName: CString,
      sid: SIDPtr,
      cbSid: Ptr[DWord],
      referencedDomainName: CString,
      referencedDomainNameSize: Ptr[DWord],
      use: Ptr[SidNameUse]
  ): Boolean = extern
  def LookupAccountNameW(
      systemName: CWString,
      accountName: CWString,
      sid: SIDPtr,
      cbSid: Ptr[DWord],
      referencedDomainName: CWString,
      referencedDomainNameSize: Ptr[DWord],
      use: Ptr[SidNameUse]
  ): Boolean = extern
  def LookupAccountSidA(
      systemName: Ptr[CString],
      sid: SIDPtr,
      name: CString,
      nameSize: Ptr[DWord],
      referencedDomainName: CString,
      referencedDomainNameSize: Ptr[DWord],
      use: Ptr[SidNameUse]
  ): Boolean = extern
  def LookupAccountSidW(
      systemName: CWString,
      sid: SIDPtr,
      name: CWString,
      nameSize: Ptr[DWord],
      referencedDomainName: CWString,
      referencedDomainNameSize: Ptr[DWord],
      use: Ptr[SidNameUse]
  ): Boolean = extern
  def MoveFileExA(
      existingFileName: CString,
      newFileName: CString,
      flags: DWord
  ): Boolean = extern

  def MoveFileExW(
      existingFileName: CWString,
      newFileName: CWString,
      flags: DWord
  ): Boolean = extern

  def RegisterWaitForSingleObject(
      retHandle: Ptr[Handle],
      ref: Handle,
      callbackFn: WaitOrTimerCallback,
      context: CVoidPtr,
      miliseconds: DWord,
      flags: DWord
  ): Boolean = extern

  def UnregisterWait(handle: Handle): Boolean = extern

  def CreateFileMappingA(
      hFile: Handle,
      lpFileMappingAttributes: Ptr[SecurityAttributes],
      flProtect: DWord,
      dwMaximumSizeHigh: DWord,
      dwMaximumSizeLow: DWord,
      lpName: CString
  ): Handle = extern

  def CreateFileMappingW(
      hFile: Handle,
      lpFileMappingAttributes: Ptr[SecurityAttributes],
      flProtect: DWord,
      dwMaximumSizeHigh: DWord,
      dwMaximumSizeLow: DWord,
      lpName: CWString
  ): Handle = extern

  @name("scalanative_lang_user_default")
  final def DefaultLanguageId: DWord = extern

  @name("scalanative_infinite")
  final def Infinite: DWord = extern
}

object WinBaseApiExt {
  final val WT_EXECUTEDEFAULT = 0x00000000.toUInt
  final val WT_EXECUTEIOTHREAD = 0x00000001.toUInt
  final val WT_EXECUTEINPERSISTANTTHREAD = 0x00000080.toUInt
  final val WT_EXECUTEINWAITTHREAD = 0x00000004.toUInt
  final val WT_EXECUTELONGFUNCTION = 0x00000010.toUInt
  final val WT_EXECUTEONLYONCE = 0x00000008.toUInt
  final val WT_TRANSFER_IMPERSONATION = 0x00000100.toUInt

  final val MOVEFILE_REPLACE_EXISTING = 0x1.toUInt
  final val MOVEFILE_COPY_ALLOWED = 0x2.toUInt
  final val MOVEFILE_DELAY_UNTIL_REBOOT = 0x4.toUInt
  final val MOVEFILE_WRITE_THROUGH = 0x8.toUInt
  final val MOVEFILE_CREATE_HARDLINK = 0x10.toUInt
  final val MOVEFILE_FAIL_IF_NOT_TRACKABLE = 0x20.toUInt

  final val SYMBOLIC_LINK_FLAG_FILE = 0.toUInt
  final val SYMBOLIC_LINK_FLAG_DIRECTORY = 0x01.toUInt
  final val SYMBOLIC_LINK_FLAG_ALLOW_UNPRIVILEGED_CREATE = 0x02.toUInt

  final val FORMAT_MESSAGE_ALLOCATE_BUFFER = 0x00000100.toUInt
  final val FORMAT_MESSAGE_IGNORE_INSERTS = 0x00000200.toUInt
  final val FORMAT_MESSAGE_FROM_STRING = 0x00000400.toUInt
  final val FORMAT_MESSAGE_FROM_HMODULE = 0x00000800.toUInt
  final val FORMAT_MESSAGE_FROM_SYSTEM = 0x00001000.toUInt
  final val FORMAT_MESSAGE_ARGUMENT_ARRAY = 0x00002000.toUInt

  final val OWNER_SECURITY_INFORMATION = 0x00000001L.toUInt
  final val GROUP_SECURITY_INFORMATION = 0x00000002L.toUInt
  final val DACL_SECURITY_INFORMATION = 0x00000004L.toUInt
  final val SACL_SECURITY_INFORMATION = 0x00000008L.toUInt
  final val LABEL_SECURITY_INFORMATION = 0x00000010L.toUInt
  final val ATTRIBUTE_SECURITY_INFORMATION = 0x00000020L.toUInt
  final val SCOPE_SECURITY_INFORMATION = 0x00000040L.toUInt
  final val PROCESS_TRUST_LABEL_SECURITY_INFORMATION = 0x00000080L.toUInt
  final val ACCESS_FILTER_SECURITY_INFORMATION = 0x00000100L.toUInt
  final val BACKUP_SECURITY_INFORMATION = 0x00010000L.toUInt
  final val PROTECTED_DACL_SECURITY_INFORMATION = 0x80000000L.toUInt
  final val PROTECTED_SACL_SECURITY_INFORMATION = 0x40000000L.toUInt
  final val UNPROTECTED_DACL_SECURITY_INFORMATION = 0x20000000L.toUInt
  final val UNPROTECTED_SACL_SECURITY_INFORMATION = 0x10000000L.toUInt

  final val PAGE_READONLY = 0x02.toUInt
  final val PAGE_READWRITE = 0x04.toUInt
  final val PAGE_WRITECOPY = 0x08.toUInt
}

object WinBaseApiOps {
  import WinBaseApi.*
  implicit class SecurityAttributesOps(val ref: Ptr[SecurityAttributes])
      extends AnyVal {
    def length: DWord = ref._1
    def securityDescriptor: CVoidPtr = ref._2
    def inheritHandle: Boolean = ref._3

    def length_=(v: DWord): Unit = ref._1 = v
    def securityDescriptor_=(v: CVoidPtr): Unit = ref._2 = v
    def inheritHandle_=(v: Boolean): Unit = ref._3 = v
  }
}
