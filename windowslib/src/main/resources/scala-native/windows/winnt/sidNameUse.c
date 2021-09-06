#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include <WinNT.h>

int scalanative_win32_winnt_sid_name_use_sidtypeuser() { return SidTypeUser; }
int scalanative_win32_winnt_sid_name_use_sidtypegroup() { return SidTypeGroup; }
int scalanative_win32_winnt_sid_name_use_sidtypedomain() {
    return SidTypeDomain;
}
int scalanative_win32_winnt_sid_name_use_sidtypealias() { return SidTypeAlias; }
int scalanative_win32_winnt_sid_name_use_sidtypewellknowngroup() {
    return SidTypeWellKnownGroup;
}
int scalanative_win32_winnt_sid_name_use_sidtypedeletedaccount() {
    return SidTypeDeletedAccount;
}
int scalanative_win32_winnt_sid_name_use_sidtypeinvalid() {
    return SidTypeInvalid;
}
int scalanative_win32_winnt_sid_name_use_sidtypeunknown() {
    return SidTypeUnknown;
}
int scalanative_win32_winnt_sid_name_use_sidtypecomputer() {
    return SidTypeComputer;
}
int scalanative_win32_winnt_sid_name_use_sidtypelabel() { return SidTypeLabel; }
int scalanative_win32_winnt_sid_name_use_sidtypelogonsession() {
    return SidTypeLogonSession;
}
#endif // defined(Win32)
