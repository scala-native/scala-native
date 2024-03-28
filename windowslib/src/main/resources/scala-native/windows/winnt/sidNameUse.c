#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <winnt.h>

int scalanative_sidtypeuser() { return SidTypeUser; }
int scalanative_sidtypegroup() { return SidTypeGroup; }
int scalanative_sidtypedomain() { return SidTypeDomain; }
int scalanative_sidtypealias() { return SidTypeAlias; }
int scalanative_sidtypewellknowngroup() { return SidTypeWellKnownGroup; }
int scalanative_sidtypedeletedaccount() { return SidTypeDeletedAccount; }
int scalanative_sidtypeinvalid() { return SidTypeInvalid; }
int scalanative_sidtypeunknown() { return SidTypeUnknown; }
int scalanative_sidtypecomputer() { return SidTypeComputer; }
int scalanative_sidtypelabel() { return SidTypeLabel; }
int scalanative_sidtypelogonsession() { return SidTypeLogonSession; }
#endif // defined(Win32)
