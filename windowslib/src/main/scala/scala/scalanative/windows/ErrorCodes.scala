package scala.scalanative.windows

import scala.scalanative.unsigned.*

// Subset of error codes 0-499 parsed from https://docs.microsoft.com/en-us/windows/win32/debug/system-error-codes--0-499-
object ErrorCodes {

  /** The operation completed successfully. */
  final val ERROR_SUCCESS = 0x0.toUInt

  /** Incorrect function. */
  final val ERROR_INVALID_FUNCTION = 0x1.toUInt

  /** The system cannot find the file specified. */
  final val ERROR_FILE_NOT_FOUND = 0x2.toUInt

  /** The system cannot find the path specified. */
  final val ERROR_PATH_NOT_FOUND = 0x3.toUInt

  /** The system cannot open the file. */
  final val ERROR_TOO_MANY_OPEN_FILES = 0x4.toUInt

  /** Access is denied. */
  final val ERROR_ACCESS_DENIED = 0x5.toUInt

  /** The handle is invalid. */
  final val ERROR_INVALID_HANDLE = 0x6.toUInt

  /** The storage control blocks were destroyed. */
  final val ERROR_ARENA_TRASHED = 0x7.toUInt

  /** Not enough memory resources are available to process this command. */
  final val ERROR_NOT_ENOUGH_MEMORY = 0x8.toUInt

  /** The storage control block address is invalid. */
  final val ERROR_INVALID_BLOCK = 0x9.toUInt

  /** The environment is incorrect. */
  final val ERROR_BAD_ENVIRONMENT = 0xa.toUInt

  /** An attempt was made to load a program with an incorrect format. */
  final val ERROR_BAD_FORMAT = 0xb.toUInt

  /** The access code is invalid. */
  final val ERROR_INVALID_ACCESS = 0xc.toUInt

  /** The data is invalid. */
  final val ERROR_INVALID_DATA = 0xd.toUInt

  /** Not enough storage is available to complete this operation. */
  final val ERROR_OUTOFMEMORY = 0xe.toUInt

  /** The system cannot find the drive specified. */
  final val ERROR_INVALID_DRIVE = 0xf.toUInt

  /** The directory cannot be removed. */
  final val ERROR_CURRENT_DIRECTORY = 0x10.toUInt

  /** The system cannot move the file to a different disk drive. */
  final val ERROR_NOT_SAME_DEVICE = 0x11.toUInt

  /** There are no more files. */
  final val ERROR_NO_MORE_FILES = 0x12.toUInt

  /** The media is write protected. */
  final val ERROR_WRITE_PROTECT = 0x13.toUInt

  /** The system cannot find the device specified. */
  final val ERROR_BAD_UNIT = 0x14.toUInt

  /** The device is not ready. */
  final val ERROR_NOT_READY = 0x15.toUInt

  /** The device does not recognize the command. */
  final val ERROR_BAD_COMMAND = 0x16.toUInt

  /** Data error (cyclic redundancy check). */
  final val ERROR_CRC = 0x17.toUInt

  /** The program issued a command but the command length is incorrect. */
  final val ERROR_BAD_LENGTH = 0x18.toUInt

  /** The drive cannot locate a specific area or track on the disk. */
  final val ERROR_SEEK = 0x19.toUInt

  /** The specified disk or diskette cannot be accessed. */
  final val ERROR_NOT_DOS_DISK = 0x1a.toUInt

  /** The drive cannot find the sector requested. */
  final val ERROR_SECTOR_NOT_FOUND = 0x1b.toUInt

  /** The printer is out of paper. */
  final val ERROR_OUT_OF_PAPER = 0x1c.toUInt

  /** The system cannot write to the specified device. */
  final val ERROR_WRITE_FAULT = 0x1d.toUInt

  /** The system cannot read from the specified device. */
  final val ERROR_READ_FAULT = 0x1e.toUInt

  /** A device attached to the system is not functioning. */
  final val ERROR_GEN_FAILURE = 0x1f.toUInt

  /** The process cannot access the file because it is being used by another
   *  process.
   */
  final val ERROR_SHARING_VIOLATION = 0x20.toUInt

  /** The process cannot access the file because another process has locked a
   *  portion of the file.
   */
  final val ERROR_LOCK_VIOLATION = 0x21.toUInt

  /** The wrong diskette is in the drive. Insert %2 (Volume Serial Number: %3)
   *  into drive %1.
   */
  final val ERROR_WRONG_DISK = 0x22.toUInt

  /** Too many files opened for sharing. */
  final val ERROR_SHARING_BUFFER_EXCEEDED = 0x24.toUInt

  /** Reached the end of the file. */
  final val ERROR_HANDLE_EOF = 0x26.toUInt

  /** The disk is full. */
  final val ERROR_HANDLE_DISK_FULL = 0x27.toUInt

  /** The request is not supported. */
  final val ERROR_NOT_SUPPORTED = 0x32.toUInt

  /** Windows cannot find the network path. Verify that the network path is
   *  correct and the destination computer is not busy or turned off. If Windows
   *  still cannot find the network path, contact your network administrator.
   */
  final val ERROR_REM_NOT_LIST = 0x33.toUInt

  /** You were not connected because a duplicate name exists on the network. If
   *  joining a domain, go to System in Control Panel to change the computer
   *  name and try again. If joining a workgroup, choose another workgroup name.
   */
  final val ERROR_DUP_NAME = 0x34.toUInt

  /** The network path was not found. */
  final val ERROR_BAD_NETPATH = 0x35.toUInt

  /** The network is busy. */
  final val ERROR_NETWORK_BUSY = 0x36.toUInt

  /** The specified network resource or device is no longer available. */
  final val ERROR_DEV_NOT_EXIST = 0x37.toUInt

  /** The network BIOS command limit has been reached. */
  final val ERROR_TOO_MANY_CMDS = 0x38.toUInt

  /** A network adapter hardware error occurred. */
  final val ERROR_ADAP_HDW_ERR = 0x39.toUInt

  /** The specified server cannot perform the requested operation. */
  final val ERROR_BAD_NET_RESP = 0x3a.toUInt

  /** An unexpected network error occurred. */
  final val ERROR_UNEXP_NET_ERR = 0x3b.toUInt

  /** The remote adapter is not compatible. */
  final val ERROR_BAD_REM_ADAP = 0x3c.toUInt

  /** The printer queue is full. */
  final val ERROR_PRINTQ_FULL = 0x3d.toUInt

  /** Space to store the file waiting to be printed is not available on the
   *  server.
   */
  final val ERROR_NO_SPOOL_SPACE = 0x3e.toUInt

  /** Your file waiting to be printed was deleted. */
  final val ERROR_PRINT_CANCELLED = 0x3f.toUInt

  /** The specified network name is no longer available. */
  final val ERROR_NETNAME_DELETED = 0x40.toUInt

  /** Network access is denied. */
  final val ERROR_NETWORK_ACCESS_DENIED = 0x41.toUInt

  /** The network resource type is not correct. */
  final val ERROR_BAD_DEV_TYPE = 0x42.toUInt

  /** The network name cannot be found. */
  final val ERROR_BAD_NET_NAME = 0x43.toUInt

  /** The name limit for the local computer network adapter card was exceeded.
   */
  final val ERROR_TOO_MANY_NAMES = 0x44.toUInt

  /** The network BIOS session limit was exceeded. */
  final val ERROR_TOO_MANY_SESS = 0x45.toUInt

  /** The remote server has been paused or is in the process of being started.
   */
  final val ERROR_SHARING_PAUSED = 0x46.toUInt

  /** No more connections can be made to this remote computer at this time
   *  because there are already as many connections as the computer can accept.
   */
  final val ERROR_REQ_NOT_ACCEP = 0x47.toUInt

  /** The specified printer or disk device has been paused. */
  final val ERROR_REDIR_PAUSED = 0x48.toUInt

  /** The file exists. */
  final val ERROR_FILE_EXISTS = 0x50.toUInt

  /** The directory or file cannot be created. */
  final val ERROR_CANNOT_MAKE = 0x52.toUInt

  /** Fail on INT 24. */
  final val ERROR_FAIL_I24 = 0x53.toUInt

  /** Storage to process this request is not available. */
  final val ERROR_OUT_OF_STRUCTURES = 0x54.toUInt

  /** The local device name is already in use. */
  final val ERROR_ALREADY_ASSIGNED = 0x55.toUInt

  /** The specified network password is not correct. */
  final val ERROR_INVALID_PASSWORD = 0x56.toUInt

  /** The parameter is incorrect. */
  final val ERROR_INVALID_PARAMETER = 0x57.toUInt

  /** A write fault occurred on the network. */
  final val ERROR_NET_WRITE_FAULT = 0x58.toUInt

  /** The system cannot start another process at this time. */
  final val ERROR_NO_PROC_SLOTS = 0x59.toUInt

  /** Cannot create another system semaphore. */
  final val ERROR_TOO_MANY_SEMAPHORES = 0x64.toUInt

  /** The exclusive semaphore is owned by another process. */
  final val ERROR_EXCL_SEM_ALREADY_OWNED = 0x65.toUInt

  /** The semaphore is set and cannot be closed. */
  final val ERROR_SEM_IS_SET = 0x66.toUInt

  /** The semaphore cannot be set again. */
  final val ERROR_TOO_MANY_SEM_REQUESTS = 0x67.toUInt

  /** Cannot request exclusive semaphores at interrupt time. */
  final val ERROR_INVALID_AT_INTERRUPT_TIME = 0x68.toUInt

  /** The previous ownership of this semaphore has ended. */
  final val ERROR_SEM_OWNER_DIED = 0x69.toUInt

  /** Insert the diskette for drive %1. */
  final val ERROR_SEM_USER_LIMIT = 0x6a.toUInt

  /** The program stopped because an alternate diskette was not inserted. */
  final val ERROR_DISK_CHANGE = 0x6b.toUInt

  /** The disk is in use or locked by another process. */
  final val ERROR_DRIVE_LOCKED = 0x6c.toUInt

  /** The pipe has been ended. */
  final val ERROR_BROKEN_PIPE = 0x6d.toUInt

  /** The system cannot open the device or file specified. */
  final val ERROR_OPEN_FAILED = 0x6e.toUInt

  /** The file name is too long. */
  final val ERROR_BUFFER_OVERFLOW = 0x6f.toUInt

  /** There is not enough space on the disk. */
  final val ERROR_DISK_FULL = 0x70.toUInt

  /** No more internal file identifiers available. */
  final val ERROR_NO_MORE_SEARCH_HANDLES = 0x71.toUInt

  /** The target internal file identifier is incorrect. */
  final val ERROR_INVALID_TARGET_HANDLE = 0x72.toUInt

  /** The IOCTL call made by the application program is not correct. */
  final val ERROR_INVALID_CATEGORY = 0x75.toUInt

  /** The verify-on-write switch parameter value is not correct. */
  final val ERROR_INVALID_VERIFY_SWITCH = 0x76.toUInt

  /** The system does not support the command requested. */
  final val ERROR_BAD_DRIVER_LEVEL = 0x77.toUInt

  /** This function is not supported on this system. */
  final val ERROR_CALL_NOT_IMPLEMENTED = 0x78.toUInt

  /** The semaphore timeout period has expired. */
  final val ERROR_SEM_TIMEOUT = 0x79.toUInt

  /** The data area passed to a system call is too small. */
  final val ERROR_INSUFFICIENT_BUFFER = 0x7a.toUInt

  /** The filename, directory name, or volume label syntax is incorrect. */
  final val ERROR_INVALID_NAME = 0x7b.toUInt

  /** The system call level is not correct. */
  final val ERROR_INVALID_LEVEL = 0x7c.toUInt

  /** The disk has no volume label. */
  final val ERROR_NO_VOLUME_LABEL = 0x7d.toUInt

  /** The specified module could not be found. */
  final val ERROR_MOD_NOT_FOUND = 0x7e.toUInt

  /** The specified procedure could not be found. */
  final val ERROR_PROC_NOT_FOUND = 0x7f.toUInt

  /** There are no child processes to wait for. */
  final val ERROR_WAIT_NO_CHILDREN = 0x80.toUInt

  /** The %1 application cannot be run in Win32 mode. */
  final val ERROR_CHILD_NOT_COMPLETE = 0x81.toUInt

  /** Attempt to use a file handle to an open disk partition for an operation
   *  other than raw disk I/O.
   */
  final val ERROR_DIRECT_ACCESS_HANDLE = 0x82.toUInt

  /** An attempt was made to move the file pointer before the beginning of the
   *  file.
   */
  final val ERROR_NEGATIVE_SEEK = 0x83.toUInt

  /** The file pointer cannot be set on the specified device or file. */
  final val ERROR_SEEK_ON_DEVICE = 0x84.toUInt

  /** A JOIN or SUBST command cannot be used for a drive that contains
   *  previously joined drives.
   */
  final val ERROR_IS_JOIN_TARGET = 0x85.toUInt

  /** An attempt was made to use a JOIN or SUBST command on a drive that has
   *  already been joined.
   */
  final val ERROR_IS_JOINED = 0x86.toUInt

  /** An attempt was made to use a JOIN or SUBST command on a drive that has
   *  already been substituted.
   */
  final val ERROR_IS_SUBSTED = 0x87.toUInt

  /** The system tried to delete the JOIN of a drive that is not joined. */
  final val ERROR_NOT_JOINED = 0x88.toUInt

  /** The system tried to delete the substitution of a drive that is not
   *  substituted.
   */
  final val ERROR_NOT_SUBSTED = 0x89.toUInt

  /** The system tried to join a drive to a directory on a joined drive. */
  final val ERROR_JOIN_TO_JOIN = 0x8a.toUInt

  /** The system tried to substitute a drive to a directory on a substituted
   *  drive.
   */
  final val ERROR_SUBST_TO_SUBST = 0x8b.toUInt

  /** The system tried to join a drive to a directory on a substituted drive. */
  final val ERROR_JOIN_TO_SUBST = 0x8c.toUInt

  /** The system tried to SUBST a drive to a directory on a joined drive. */
  final val ERROR_SUBST_TO_JOIN = 0x8d.toUInt

  /** The system cannot perform a JOIN or SUBST at this time. */
  final val ERROR_BUSY_DRIVE = 0x8e.toUInt

  /** The system cannot join or substitute a drive to or for a directory on the
   *  same drive.
   */
  final val ERROR_SAME_DRIVE = 0x8f.toUInt

  /** The directory is not a subdirectory of the root directory. */
  final val ERROR_DIR_NOT_ROOT = 0x90.toUInt

  /** The directory is not empty. */
  final val ERROR_DIR_NOT_EMPTY = 0x91.toUInt

  /** The path specified is being used in a substitute. */
  final val ERROR_IS_SUBST_PATH = 0x92.toUInt

  /** Not enough resources are available to process this command. */
  final val ERROR_IS_JOIN_PATH = 0x93.toUInt

  /** The path specified cannot be used at this time. */
  final val ERROR_PATH_BUSY = 0x94.toUInt

  /** An attempt was made to join or substitute a drive for which a directory on
   *  the drive is the target of a previous substitute.
   */
  final val ERROR_IS_SUBST_TARGET = 0x95.toUInt

  /** System trace information was not specified in your CONFIG.SYS file, or
   *  tracing is disallowed.
   */
  final val ERROR_SYSTEM_TRACE = 0x96.toUInt

  /** The number of specified semaphore events for DosMuxSemWait is not correct.
   */
  final val ERROR_INVALID_EVENT_COUNT = 0x97.toUInt

  /** DosMuxSemWait did not execute; too many semaphores are already set. */
  final val ERROR_TOO_MANY_MUXWAITERS = 0x98.toUInt

  /** The DosMuxSemWait list is not correct. */
  final val ERROR_INVALID_LIST_FORMAT = 0x99.toUInt

  /** The volume label you entered exceeds the label character limit of the
   *  target file system.
   */
  final val ERROR_LABEL_TOO_LONG = 0x9a.toUInt

  /** Cannot create another thread. */
  final val ERROR_TOO_MANY_TCBS = 0x9b.toUInt

  /** The recipient process has refused the signal. */
  final val ERROR_SIGNAL_REFUSED = 0x9c.toUInt

  /** The segment is already discarded and cannot be locked. */
  final val ERROR_DISCARDED = 0x9d.toUInt

  /** The segment is already unlocked. */
  final val ERROR_NOT_LOCKED = 0x9e.toUInt

  /** The address for the thread ID is not correct. */
  final val ERROR_BAD_THREADID_ADDR = 0x9f.toUInt

  /** One or more arguments are not correct. */
  final val ERROR_BAD_ARGUMENTS = 0xa0.toUInt

  /** The specified path is invalid. */
  final val ERROR_BAD_PATHNAME = 0xa1.toUInt

  /** A signal is already pending. */
  final val ERROR_SIGNAL_PENDING = 0xa2.toUInt

  /** No more threads can be created in the system. */
  final val ERROR_MAX_THRDS_REACHED = 0xa4.toUInt

  /** Unable to lock a region of a file. */
  final val ERROR_LOCK_FAILED = 0xa7.toUInt

  /** The requested resource is in use. */
  final val ERROR_BUSY = 0xaa.toUInt

  /** Device's command support detection is in progress. */
  final val ERROR_DEVICE_SUPPORT_IN_PROGRESS = 0xab.toUInt

  /** A lock request was not outstanding for the supplied cancel region. */
  final val ERROR_CANCEL_VIOLATION = 0xad.toUInt

  /** The file system does not support atomic changes to the lock type. */
  final val ERROR_ATOMIC_LOCKS_NOT_SUPPORTED = 0xae.toUInt

  /** The system detected a segment number that was not correct. */
  final val ERROR_INVALID_SEGMENT_NUMBER = 0xb4.toUInt

  /** The operating system cannot run %1. */
  final val ERROR_INVALID_ORDINAL = 0xb6.toUInt

  /** Cannot create a file when that file already exists. */
  final val ERROR_ALREADY_EXISTS = 0xb7.toUInt

  /** The flag passed is not correct. */
  final val ERROR_INVALID_FLAG_NUMBER = 0xba.toUInt

  /** The specified system semaphore name was not found. */
  final val ERROR_SEM_NOT_FOUND = 0xbb.toUInt

  /** The operating system cannot run %1. */
  final val ERROR_INVALID_STARTING_CODESEG = 0xbc.toUInt

  /** The operating system cannot run %1. */
  final val ERROR_INVALID_STACKSEG = 0xbd.toUInt

  /** The operating system cannot run %1. */
  final val ERROR_INVALID_MODULETYPE = 0xbe.toUInt

  /** Cannot run %1 in Win32 mode. */
  final val ERROR_INVALID_EXE_SIGNATURE = 0xbf.toUInt

  /** The operating system cannot run %1. */
  final val ERROR_EXE_MARKED_INVALID = 0xc0.toUInt

  /** %1 is not a valid Win32 application. */
  final val ERROR_BAD_EXE_FORMAT = 0xc1.toUInt

  /** The operating system cannot run %1. */
  final val ERROR_ITERATED_DATA_EXCEEDS_64k = 0xc2.toUInt

  /** The operating system cannot run %1. */
  final val ERROR_INVALID_MINALLOCSIZE = 0xc3.toUInt

  /** The operating system cannot run this application program. */
  final val ERROR_DYNLINK_FROM_INVALID_RING = 0xc4.toUInt

  /** The operating system is not presently configured to run this application.
   */
  final val ERROR_IOPL_NOT_ENABLED = 0xc5.toUInt

  /** The operating system cannot run %1. */
  final val ERROR_INVALID_SEGDPL = 0xc6.toUInt

  /** The operating system cannot run this application program. */
  final val ERROR_AUTODATASEG_EXCEEDS_64k = 0xc7.toUInt

  /** The code segment cannot be greater than or equal to 64K. */
  final val ERROR_RING2SEG_MUST_BE_MOVABLE = 0xc8.toUInt

  /** The operating system cannot run %1. */
  final val ERROR_RELOC_CHAIN_XEEDS_SEGLIM = 0xc9.toUInt

  /** The operating system cannot run %1. */
  final val ERROR_INFLOOP_IN_RELOC_CHAIN = 0xca.toUInt

  /** The system could not find the environment option that was entered. */
  final val ERROR_ENVVAR_NOT_FOUND = 0xcb.toUInt

  /** No process in the command subtree has a signal handler. */
  final val ERROR_NO_SIGNAL_SENT = 0xcd.toUInt

  /** The filename or extension is too long. */
  final val ERROR_FILENAME_EXCED_RANGE = 0xce.toUInt

  /** The ring 2 stack is in use. */
  final val ERROR_RING2_STACK_IN_USE = 0xcf.toUInt

  /** The global filename characters, * or ?, are entered incorrectly or too
   *  many global filename characters are specified.
   */
  final val ERROR_META_EXPANSION_TOO_LONG = 0xd0.toUInt

  /** The signal being posted is not correct. */
  final val ERROR_INVALID_SIGNAL_NUMBER = 0xd1.toUInt

  /** The signal handler cannot be set. */
  final val ERROR_THREAD_1_INACTIVE = 0xd2.toUInt

  /** The segment is locked and cannot be reallocated. */
  final val ERROR_LOCKED = 0xd4.toUInt

  /** Too many dynamic-link modules are attached to this program or dynamic-link
   *  module.
   */
  final val ERROR_TOO_MANY_MODULES = 0xd6.toUInt

  /** Cannot nest calls to LoadModule. */
  final val ERROR_NESTING_NOT_ALLOWED = 0xd7.toUInt

  /** This version of %1 is not compatible with the version of Windows you're
   *  running. Check your computer's system information and then contact the
   *  software publisher.
   */
  final val ERROR_EXE_MACHINE_TYPE_MISMATCH = 0xd8.toUInt

  /** The image file %1 is signed, unable to modify. */
  final val ERROR_EXE_CANNOT_MODIFY_SIGNED_BINARY = 0xd9.toUInt

  /** The image file %1 is strong signed, unable to modify. */
  final val ERROR_EXE_CANNOT_MODIFY_STRONG_SIGNED_BINARY = 0xda.toUInt

  /** This file is checked out or locked for editing by another user. */
  final val ERROR_FILE_CHECKED_OUT = 0xdc.toUInt

  /** The file must be checked out before saving changes. */
  final val ERROR_CHECKOUT_REQUIRED = 0xdd.toUInt

  /** The file type being saved or retrieved has been blocked. */
  final val ERROR_BAD_FILE_TYPE = 0xde.toUInt

  /** The file size exceeds the limit allowed and cannot be saved. */
  final val ERROR_FILE_TOO_LARGE = 0xdf.toUInt

  /** Access Denied. Before opening files in this location, you must first add
   *  the website to your trusted sites list, browse to the website, andselect
   *  the option to login automatically.
   */
  final val ERROR_FORMS_AUTH_REQUIRED = 0xe0.toUInt

  /** Operation did not complete successfully because the file contains a virus
   *  or potentially unwanted software.
   */
  final val ERROR_VIRUS_INFECTED = 0xe1.toUInt

  /** This file contains a virus or potentially unwanted software and cannot be
   *  opened. Due to the nature of this virus or potentially unwanted software,
   *  the file has been removed from this location.
   */
  final val ERROR_VIRUS_DELETED = 0xe2.toUInt

  /** The pipe is local. */
  final val ERROR_PIPE_LOCAL = 0xe5.toUInt

  /** The pipe state is invalid. */
  final val ERROR_BAD_PIPE = 0xe6.toUInt

  /** All pipe instances are busy. */
  final val ERROR_PIPE_BUSY = 0xe7.toUInt

  /** The pipe is being closed. */
  final val ERROR_NO_DATA = 0xe8.toUInt

  /** No process is on the other end of the pipe. */
  final val ERROR_PIPE_NOT_CONNECTED = 0xe9.toUInt

  /** More data is available. */
  final val ERROR_MORE_DATA = 0xea.toUInt

  /** The session was canceled. */
  final val ERROR_VC_DISCONNECTED = 0xf0.toUInt

  /** The specified extended attribute name was invalid. */
  final val ERROR_INVALID_EA_NAME = 0xfe.toUInt

  /** The extended attributes are inconsistent. */
  final val ERROR_EA_LIST_INCONSISTENT = 0xff.toUInt

  /** The wait operation timed out. */
  final val WAIT_TIMEOUT = 0x102.toUInt

  /** No more data is available. */
  final val ERROR_NO_MORE_ITEMS = 0x103.toUInt

  /** The copy functions cannot be used. */
  final val ERROR_CANNOT_COPY = 0x10a.toUInt

  /** The directory name is invalid. */
  final val ERROR_DIRECTORY = 0x10b.toUInt

  /** The extended attributes did not fit in the buffer. */
  final val ERROR_EAS_DIDNT_FIT = 0x113.toUInt

  /** The extended attribute file on the mounted file system is corrupt. */
  final val ERROR_EA_FILE_CORRUPT = 0x114.toUInt

  /** The extended attribute table file is full. */
  final val ERROR_EA_TABLE_FULL = 0x115.toUInt

  /** The specified extended attribute handle is invalid. */
  final val ERROR_INVALID_EA_HANDLE = 0x116.toUInt

  /** The mounted file system does not support extended attributes. */
  final val ERROR_EAS_NOT_SUPPORTED = 0x11a.toUInt

  /** Attempt to release mutex not owned by caller. */
  final val ERROR_NOT_OWNER = 0x120.toUInt

  /** Too many posts were made to a semaphore. */
  final val ERROR_TOO_MANY_POSTS = 0x12a.toUInt

  /** Only part of a ReadProcessMemory or WriteProcessMemory request was
   *  completed.
   */
  final val ERROR_PARTIAL_COPY = 0x12b.toUInt

  /** The oplock request is denied. */
  final val ERROR_OPLOCK_NOT_GRANTED = 0x12c.toUInt

  /** An invalid oplock acknowledgment was received by the system. */
  final val ERROR_INVALID_OPLOCK_PROTOCOL = 0x12d.toUInt

  /** The volume is too fragmented to complete this operation. */
  final val ERROR_DISK_TOO_FRAGMENTED = 0x12e.toUInt

  /** The file cannot be opened because it is in the process of being deleted.
   */
  final val ERROR_DELETE_PENDING = 0x12f.toUInt

  /** Short name settings may not be changed on this volume due to the global
   *  registry setting.
   */
  final val ERROR_INCOMPATIBLE_WITH_GLOBAL_SHORT_NAME_REGISTRY_SETTING =
    0x130.toUInt

  /** Short names are not enabled on this volume. */
  final val ERROR_SHORT_NAMES_NOT_ENABLED_ON_VOLUME = 0x131.toUInt

  /** The security stream for the given volume is in an inconsistent state.
   *  Please run CHKDSK on the volume.
   */
  final val ERROR_SECURITY_STREAM_IS_INCONSISTENT = 0x132.toUInt

  /** A requested file lock operation cannot be processed due to an invalid byte
   *  range.
   */
  final val ERROR_INVALID_LOCK_RANGE = 0x133.toUInt

  /** The subsystem needed to support the image type is not present. */
  final val ERROR_IMAGE_SUBSYSTEM_NOT_PRESENT = 0x134.toUInt

  /** The specified file already has a notification GUID associated with it. */
  final val ERROR_NOTIFICATION_GUID_ALREADY_DEFINED = 0x135.toUInt

  /** An invalid exception handler routine has been detected. */
  final val ERROR_INVALID_EXCEPTION_HANDLER = 0x136.toUInt

  /** Duplicate privileges were specified for the token. */
  final val ERROR_DUPLICATE_PRIVILEGES = 0x137.toUInt

  /** No ranges for the specified operation were able to be processed. */
  final val ERROR_NO_RANGES_PROCESSED = 0x138.toUInt

  /** Operation is not allowed on a file system internal file. */
  final val ERROR_NOT_ALLOWED_ON_SYSTEM_FILE = 0x139.toUInt

  /** The physical resources of this disk have been exhausted. */
  final val ERROR_DISK_RESOURCES_EXHAUSTED = 0x13a.toUInt

  /** The token representing the data is invalid. */
  final val ERROR_INVALID_TOKEN = 0x13b.toUInt

  /** The device does not support the command feature. */
  final val ERROR_DEVICE_FEATURE_NOT_SUPPORTED = 0x13c.toUInt
    /** The system cannot find message text for message number 0x%1 in the
     *  message file for %2.
     */
    .toUInt
  final val ERROR_MR_MID_NOT_FOUND = 0x13d.toUInt

  /** The scope specified was not found. */
  final val ERROR_SCOPE_NOT_FOUND = 0x13e.toUInt

  /** The Central Access Policy specified is not defined on the target machine.
   */
  final val ERROR_UNDEFINED_SCOPE = 0x13f.toUInt

  /** The Central Access Policy obtained from Active Directory is invalid. */
  final val ERROR_INVALID_CAP = 0x140.toUInt

  /** The device is unreachable. */
  final val ERROR_DEVICE_UNREACHABLE = 0x141.toUInt

  /** The target device has insufficient resources to complete the operation. */
  final val ERROR_DEVICE_NO_RESOURCES = 0x142.toUInt

  /** A data integrity checksum error occurred. Data in the file stream is
   *  corrupt.
   */
  final val ERROR_DATA_CHECKSUM_ERROR = 0x143.toUInt

  /** An attempt was made to modify both a KERNEL and normal Extended Attribute
   *  (EA) in the same operation.
   */
  final val ERROR_INTERMIXED_KERNEL_EA_OPERATION = 0x144.toUInt

  /** Device does not support file-level TRIM. */
  final val ERROR_FILE_LEVEL_TRIM_NOT_SUPPORTED = 0x146.toUInt

  /** The command specified a data offset that does not align to the device's
   *  granularity/alignment.
   */
  final val ERROR_OFFSET_ALIGNMENT_VIOLATION = 0x147.toUInt

  /** The command specified an invalid field in its parameter list. */
  final val ERROR_INVALID_FIELD_IN_PARAMETER_LIST = 0x148.toUInt

  /** An operation is currently in progress with the device. */
  final val ERROR_OPERATION_IN_PROGRESS = 0x149.toUInt

  /** An attempt was made to send down the command via an invalid path to the
   *  target device.
   */
  final val ERROR_BAD_DEVICE_PATH = 0x14a.toUInt

  /** The command specified a number of descriptors that exceeded the maximum
   *  supported by the device.
   */
  final val ERROR_TOO_MANY_DESCRIPTORS = 0x14b.toUInt

  /** Scrub is disabled on the specified file. */
  final val ERROR_SCRUB_DATA_DISABLED = 0x14c.toUInt

  /** The storage device does not provide redundancy. */
  final val ERROR_NOT_REDUNDANT_STORAGE = 0x14d.toUInt

  /** An operation is not supported on a resident file. */
  final val ERROR_RESIDENT_FILE_NOT_SUPPORTED = 0x14e.toUInt

  /** An operation is not supported on a compressed file. */
  final val ERROR_COMPRESSED_FILE_NOT_SUPPORTED = 0x14f.toUInt

  /** An operation is not supported on a directory. */
  final val ERROR_DIRECTORY_NOT_SUPPORTED = 0x150.toUInt

  /** The specified copy of the requested data could not be read. */
  final val ERROR_NOT_READ_FROM_COPY = 0x151.toUInt

  /** No action was taken as a system reboot is required. */
  final val ERROR_FAIL_NOACTION_REBOOT = 0x15e.toUInt

  /** The shutdown operation failed. */
  final val ERROR_FAIL_SHUTDOWN = 0x15f.toUInt

  /** The restart operation failed. */
  final val ERROR_FAIL_RESTART = 0x160.toUInt

  /** The maximum number of sessions has been reached. */
  final val ERROR_MAX_SESSIONS_REACHED = 0x161.toUInt

  /** The thread is already in background processing mode. */
  final val ERROR_THREAD_MODE_ALREADY_BACKGROUND = 0x190.toUInt

  /** The thread is not in background processing mode. */
  final val ERROR_THREAD_MODE_NOT_BACKGROUND = 0x191.toUInt

  /** The process is already in background processing mode. */
  final val ERROR_PROCESS_MODE_ALREADY_BACKGROUND = 0x192.toUInt

  /** The process is not in background processing mode. */
  final val ERROR_PROCESS_MODE_NOT_BACKGROUND = 0x193.toUInt

  /** Attempt to access invalid address. */
  final val ERROR_INVALID_ADDRESS = 0x1e7.toUInt
}
