#ifdef _WIN32

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <windows.h>
#include <stdio.h>
#include <Aclapi.h>

typedef int mode_t;
#include "os_win_dirent.h"
#include <io.h>

#define MS_MODE_MASK (0x0000ffff)

void setAccessFlagHelper(ACCESS_MASK *accessRights, DWORD current, DWORD mode,
                         DWORD testflags, DWORD flags) {
    if ((current & testflags) != (mode & testflags)) {
        if (mode & testflags) {
            *accessRights |= (flags);
        } else {
            *accessRights &= ~(flags);
        }
    }
}

void setChmodFlagHelper(mode_t *chmodmode, ACCESS_MASK accessRights,
                        DWORD testflags, DWORD flags) {
    if ((accessRights & testflags) == testflags) {
        *chmodmode |= (flags);
    } else {
        *chmodmode &= ~(flags);
    }
}

DWORD AddAceToObjectsSecurityDescriptor(
    LPWSTR pszObjName,         // name of object
    SE_OBJECT_TYPE ObjectType, // type of object
    LPWSTR pszTrustee,         // trustee for new ACE
    TRUSTEE_FORM TrusteeForm,  // format of trustee structure
    TRUSTEE_TYPE TrusteeType,  // type of trustee structure
    DWORD dwAccessRights,      // access mask for new ACE
    ACCESS_MODE AccessMode,    // type of ACE
    DWORD dwInheritance        // inheritance flags for new ACE
    ) {
    DWORD dwRes = 0;
    PACL pOldDACL = NULL, pNewDACL = NULL;
    PSECURITY_DESCRIPTOR pSD = NULL;
    EXPLICIT_ACCESSW ea;

    if (NULL == pszObjName)
        return ERROR_INVALID_PARAMETER;

    // Get a pointer to the existing DACL.

    dwRes =
        GetNamedSecurityInfoW(pszObjName, ObjectType, DACL_SECURITY_INFORMATION,
                              NULL, NULL, &pOldDACL, NULL, &pSD);
    if (ERROR_SUCCESS != dwRes) {
        // printf("GetNamedSecurityInfo Error %lu\n", dwRes);
        goto Cleanup;
    }

    // Initialize an EXPLICIT_ACCESS structure for the new ACE.

    ZeroMemory(&ea, sizeof(EXPLICIT_ACCESSW));
    ea.grfAccessPermissions = dwAccessRights;
    ea.grfAccessMode = AccessMode;
    ea.grfInheritance = dwInheritance;
    ea.Trustee.TrusteeForm = TrusteeForm;
    ea.Trustee.TrusteeType = TrusteeType;
    ea.Trustee.ptstrName = pszTrustee;

    // Create a new ACL that merges the new ACE
    // into the existing DACL.

    dwRes = SetEntriesInAclW(1, &ea, pOldDACL, &pNewDACL);
    if (ERROR_SUCCESS != dwRes) {
        // printf("SetEntriesInAcl Error %lu\n", dwRes);
        goto Cleanup;
    }

    // Attach the new ACL as the object's DACL.

    dwRes =
        SetNamedSecurityInfoW(pszObjName, ObjectType, DACL_SECURITY_INFORMATION,
                              NULL, NULL, pNewDACL, NULL);
    if (ERROR_SUCCESS != dwRes) {
        // printf("SetNamedSecurityInfo Error %lu\n", dwRes);
        goto Cleanup;
    }

Cleanup:

    if (pSD != NULL)
        LocalFree((HLOCAL)pSD);
    if (pNewDACL != NULL)
        LocalFree((HLOCAL)pNewDACL);

    return dwRes;
}

mode_t AccessMaskToMode(ACCESS_MASK Mask) {
    mode_t result = 0;

    setChmodFlagHelper(&result, Mask, FILE_READ_DATA,
                       S_ISUID | S_ISGID | S_ISVTX | S_IRUSR | S_IRGRP |
                           S_IROTH);
    setChmodFlagHelper(&result, Mask, FILE_WRITE_DATA,
                       S_ISUID | S_ISGID | S_ISVTX | S_IWUSR | S_IWGRP |
                           S_IWOTH);
    setChmodFlagHelper(&result, Mask, FILE_EXECUTE,
                       S_ISUID | S_ISGID | S_ISVTX | S_IXUSR | S_IXGRP |
                           S_IXOTH);

    return result;
}

DWORD getAccessPermissions(
    LPWSTR pszObjName,         // name of object
    SE_OBJECT_TYPE ObjectType, // type of object
    LPWSTR pszTrustee,         // trustee for new ACE
    TRUSTEE_FORM TrusteeForm,  // format of trustee structure
    TRUSTEE_TYPE TrusteeType,  // type of trustee structure
    ACCESS_MASK *accessRights) {
    DWORD dwRes = 0;
    PACL pDACL = NULL;
    PSECURITY_DESCRIPTOR pSD = NULL;
    PEXPLICIT_ACCESSW ea = NULL;
    ULONG numberOfEa = 0;

    TRUSTEEW trustee;

    if (NULL == accessRights) {
        return ERROR_INVALID_PARAMETER;
    }

    if (NULL == pszObjName)
        return ERROR_INVALID_PARAMETER;

    // Get a pointer to the existing DACL.

    dwRes =
        GetNamedSecurityInfoW(pszObjName, ObjectType, DACL_SECURITY_INFORMATION,
                              NULL, NULL, &pDACL, NULL, &pSD);
    if (ERROR_SUCCESS != dwRes) {
        // printf("GetNamedSecurityInfo Error %lu\n", dwRes);
        goto Cleanup;
    }

    dwRes = GetExplicitEntriesFromAclW(pDACL, &numberOfEa, &ea);

    if (dwRes != 0 || numberOfEa != 1) {
        trustee.ptstrName = pszTrustee;
        trustee.TrusteeForm = TrusteeForm;
        trustee.TrusteeType = TrusteeType;

        dwRes = GetEffectiveRightsFromAclW(pDACL, &trustee, accessRights);
        if (ERROR_SUCCESS != dwRes) {
            // printf("GetEffectiveRightsFromAclW Error %lu\n", dwRes);
            goto Cleanup;
        }
    } else {
        *accessRights = ea->grfAccessPermissions;
    }

Cleanup:

    if (pSD != NULL)
        LocalFree((HLOCAL)pSD);

    return dwRes;
}

extern "C" mode_t getAccessMode(const char *path) {
    ACCESS_MASK accessRights;
    const int cSize = 1024;
    wchar_t pathw[cSize];
    size_t outLength = 0;
    mbstowcs_s(&outLength, pathw, cSize, path, cSize);

    WCHAR userNameBuffer[256];
    DWORD userNameLength = 256;
    GetUserNameW(userNameBuffer, &userNameLength);

    int res =
        getAccessPermissions(pathw, SE_FILE_OBJECT, userNameBuffer,
                             TRUSTEE_IS_NAME, TRUSTEE_IS_USER, &accessRights);
    if (res != 0) {
        return -1;
    }

    return AccessMaskToMode(accessRights);
}

extern "C" mode_t getAccessModeF(const int fileHandle) {
    HANDLE hFile = (HANDLE)(uintptr_t)(fileHandle);
    char existingTarget[MAX_PATH];
    if (INVALID_HANDLE_VALUE != hFile) {
        GetFinalPathNameByHandleA(hFile, existingTarget, MAX_PATH,
                                  FILE_NAME_OPENED);
    }
    return getAccessMode(existingTarget);
}

extern "C" int pchmod(const char *path, mode_t mode) {
    const int cSize = 1024;
    wchar_t pathw[cSize];
    size_t outLength = 0;
    mbstowcs_s(&outLength, pathw, cSize, path, cSize);

    WCHAR userNameBuffer[256];
    DWORD userNameLength = 256;
    GetUserNameW(userNameBuffer, &userNameLength);

    ACCESS_MASK accessRights;

    int res =
        getAccessPermissions(pathw, SE_FILE_OBJECT, userNameBuffer,
                             TRUSTEE_IS_NAME, TRUSTEE_IS_USER, &accessRights);
    if (res != 0) {
        return -1;
    }

    mode_t currentmode = AccessMaskToMode(accessRights);
    DWORD newAcessingRights = accessRights;
    setAccessFlagHelper(&newAcessingRights, currentmode, mode, S_IRUSR,
                        FILE_READ_DATA);
    setAccessFlagHelper(&newAcessingRights, currentmode, mode, S_IWUSR,
                        FILE_WRITE_DATA);
    setAccessFlagHelper(&newAcessingRights, currentmode, mode, S_IXUSR,
                        FILE_EXECUTE);

    res = AddAceToObjectsSecurityDescriptor(
        pathw, SE_FILE_OBJECT, userNameBuffer, TRUSTEE_IS_NAME, TRUSTEE_IS_USER,
        newAcessingRights, SET_ACCESS, NO_INHERITANCE);

    return res != 0 ? 1 : 0;
}

#endif /* !_STAT_H_ */
