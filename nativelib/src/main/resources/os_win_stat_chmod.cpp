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
//#define CHMOD_VERBOSE

void setAccessFlagHelper(ACCESS_MASK *accessRights, DWORD current, DWORD mode,
                         DWORD testflags, DWORD flags) {
    // if ((current & testflags) != (mode & testflags)) {
    if (mode & testflags) {
        *accessRights |= (flags);
    } /* else {
         *accessRights &= ~(flags);
     }*/
    //}
}

void setChmodFlagHelper(mode_t *chmodmode, ACCESS_MASK accessRights,
                        DWORD testflags, DWORD flags) {
    if ((accessRights & testflags) == testflags) {
        *chmodmode |= (flags);
    } /*else {
        *chmodmode &= ~(flags);
    }*/
}

PSID ConvertNameToBinarySid(LPWSTR pAccountName) {
    LPWSTR lpServerName = NULL;
    LPWSTR pDomainName = NULL;
    DWORD dwDomainNameSize = 0;
    PSID pSid = NULL;
    DWORD dwSidSize = 0;
    SID_NAME_USE sidType;
    BOOL fSuccess = FALSE;
    HRESULT hr = S_OK;

    __try {
        LookupAccountNameW(lpServerName, // look up on local system
                           pAccountName,
                           pSid, // buffer to receive name
                           &dwSidSize, pDomainName, &dwDomainNameSize,
                           &sidType);

        //  If the Name cannot be resolved, LookupAccountName will fail with
        //  ERROR_NONE_MAPPED.
        if (GetLastError() == ERROR_NONE_MAPPED) {
            wprintf_s(L"LookupAccountName failed with %d\n", GetLastError());
            __leave;
        } else if (GetLastError() == ERROR_INSUFFICIENT_BUFFER) {
            pSid = (LPWSTR)LocalAlloc(LPTR, dwSidSize * sizeof(TCHAR));
            if (pSid == NULL) {
                wprintf_s(L"LocalAlloc failed with %d\n", GetLastError());
                __leave;
            }

            pDomainName =
                (LPWSTR)LocalAlloc(LPTR, dwDomainNameSize * sizeof(TCHAR));
            if (pDomainName == NULL) {
                wprintf_s(L"LocalAlloc failed with %d\n", GetLastError());
                __leave;
            }

            if (!LookupAccountNameW(lpServerName, // look up on local system
                                    pAccountName,
                                    pSid, // buffer to receive name
                                    &dwSidSize, pDomainName, &dwDomainNameSize,
                                    &sidType)) {
                wprintf_s(L"LookupAccountName failed with %d\n",
                          GetLastError());
                __leave;
            }
        }

        //  Any other error code
        else {
            wprintf_s(L"LookupAccountName failed with %d\n", GetLastError());
            __leave;
        }

        fSuccess = TRUE;
    } __finally {
        if (pDomainName != NULL) {
            LocalFree(pDomainName);
            pDomainName = NULL;
        }

        //  Free pSid only if failed;
        //  otherwise, the caller has to free it after use.
        if (fSuccess == FALSE) {
            if (pSid != NULL) {
                LocalFree(pSid);
                pSid = NULL;
            }
        }
    }

    return pSid;
}

PSID getCurrentUserSID() {
    struct SIDCleaner {
        SIDCleaner() {
            WCHAR userNameBuffer[256];
            DWORD userNameLength = 256;
            GetUserNameW(userNameBuffer, &userNameLength);
            psid = ConvertNameToBinarySid(userNameBuffer);
        }
        ~SIDCleaner() { LocalFree(psid); }
        PSID psid = nullptr;
    };
    static SIDCleaner cache;
    return cache.psid;
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

    dwRes = GetNamedSecurityInfoW(pszObjName, ObjectType,
                                  DACL_SECURITY_INFORMATION |
                                      PROTECTED_DACL_SECURITY_INFORMATION,
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
    ea.Trustee.ptstrName = (LPWSTR)pszTrustee;

    // Create a new ACL that merges the new ACE
    // into the existing DACL.

    dwRes = SetEntriesInAclW(1, &ea, pOldDACL, &pNewDACL);
    if (ERROR_SUCCESS != dwRes) {
        // printf("SetEntriesInAcl Error %lu\n", dwRes);
        goto Cleanup;
    }

    // Attach the new ACL as the object's DACL.

    dwRes = SetNamedSecurityInfoW(pszObjName, ObjectType,
                                  DACL_SECURITY_INFORMATION |
                                      PROTECTED_DACL_SECURITY_INFORMATION,
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
    setChmodFlagHelper(&result, Mask, FILE_WRITE_DATA | FILE_APPEND_DATA,
                       S_ISUID | S_ISGID | S_ISVTX | S_IWUSR | S_IWGRP |
                           S_IWOTH);
    setChmodFlagHelper(&result, Mask, FILE_EXECUTE,
                       // S_IRUSR | S_IRGRP |
                       S_ISUID | S_ISGID | S_ISVTX | S_IXUSR | S_IXGRP |
                           S_IXOTH);

    return result;
}

DWORD getAccessPermissions(
    LPWSTR pszObjName,         // name of object
    SE_OBJECT_TYPE ObjectType, // type of object
    PSID pszTrustee,           // trustee for new ACE
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

    dwRes = GetNamedSecurityInfoW(pszObjName, ObjectType,
                                  DACL_SECURITY_INFORMATION |
                                      PROTECTED_DACL_SECURITY_INFORMATION,
                                  NULL, NULL, &pDACL, NULL, &pSD);
    if (ERROR_SUCCESS != dwRes) {
        // printf("GetNamedSecurityInfo Error %lu\n", dwRes);
        goto Cleanup;
    }

    dwRes = GetExplicitEntriesFromAclW(pDACL, &numberOfEa, &ea);

    *accessRights = 0;
    for (int i = 0; dwRes == 0 && i < numberOfEa; ++i) {
        const auto esid = (PSID)ea[i].Trustee.ptstrName;
        if (EqualSid(esid, pszTrustee)) {
            *accessRights |= ea->grfAccessPermissions;
        }
    }

    if (dwRes != 0 || *accessRights == 0) {
        trustee.ptstrName = (LPWSTR)pszTrustee;
        trustee.TrusteeForm = TrusteeForm;
        trustee.TrusteeType = TrusteeType;

        dwRes = GetEffectiveRightsFromAclW(pDACL, &trustee, accessRights);
        if (ERROR_SUCCESS != dwRes) {
            // printf("GetEffectiveRightsFromAclW Error %lu\n", dwRes);
            goto Cleanup;
        }
    }

Cleanup:

    if (pSD != NULL)
        LocalFree((HLOCAL)pSD);

    return dwRes;
}

BOOL DirectoryExists(LPWSTR szPath) {
    DWORD dwAttrib = GetFileAttributesW(szPath);

    return (dwAttrib != INVALID_FILE_ATTRIBUTES &&
            (dwAttrib & FILE_ATTRIBUTE_DIRECTORY));
}

extern "C" mode_t getAccessMode(const char *path) {
    ACCESS_MASK accessRights;
    const int cSize = 1024;
    wchar_t pathw[cSize];
    size_t outLength = 0;
    mbstowcs_s(&outLength, pathw, cSize, path, cSize);

    int res =
        getAccessPermissions(pathw, SE_FILE_OBJECT, getCurrentUserSID(),
                             TRUSTEE_IS_SID, TRUSTEE_IS_USER, &accessRights);

    if (res != 0) {
        return -1;
    }

    mode_t dirFlags = 0; // DirectoryExists(pathw) ? S_IFDIR : S_IFREG;
    DWORD dwAttrib = GetFileAttributesW(pathw);

    if (dwAttrib != INVALID_FILE_ATTRIBUTES) {
        if (dwAttrib & FILE_ATTRIBUTE_REPARSE_POINT) {
            dirFlags |= S_IFLNK;
        } else {
            dirFlags |=
                (dwAttrib & FILE_ATTRIBUTE_DIRECTORY) ? S_IFDIR : S_IFREG;
        }
    }

    const auto result = AccessMaskToMode(accessRights) | dirFlags;
#ifdef CHMOD_VERBOSE
    printf("path=%s\n", path);
    if ((result & S_IFMT) == S_IFDIR) {
        printf("Directory\n");
    } else if ((result & S_IFMT) == S_IFLNK) {
        printf("Link\n");
    } else if ((result & S_IFMT) == S_IFREG) {
        printf("File\n");
    } else
        printf("Unknown\n");
    printf("(Read)\t\tUser: %x, Group: %x, Other: %x\n", (result & S_IRUSR),
           (result & S_IRGRP), (result & S_IROTH));
    printf("(Write)\t\tUser: %x, Group: %x, Other: %x\n", (result & S_IWUSR),
           (result & S_IWGRP), (result & S_IWOTH));
    printf("(Execute)\tUser: %x, Group: %x, Other: %x\n", (result & S_IXUSR),
           (result & S_IXGRP), (result & S_IXOTH));
    printf("\n");
#endif
    return result;
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

    ACCESS_MASK accessRights;

    int res =
        getAccessPermissions(pathw, SE_FILE_OBJECT, getCurrentUserSID(),
                             TRUSTEE_IS_SID, TRUSTEE_IS_USER, &accessRights);
    if (res != 0) {
        return -1;
    }

    mode_t currentmode = AccessMaskToMode(accessRights);
    DWORD newAcessingRights = 0; // accessRights;
    setAccessFlagHelper(&newAcessingRights, currentmode, mode, S_IRUSR,
                        FILE_GENERIC_READ);
    setAccessFlagHelper(&newAcessingRights, currentmode, mode, S_IWUSR,
                        FILE_GENERIC_WRITE);
    setAccessFlagHelper(&newAcessingRights, currentmode, mode, S_IXUSR,
                        FILE_GENERIC_EXECUTE);

    res = AddAceToObjectsSecurityDescriptor(
        pathw, SE_FILE_OBJECT, (LPWSTR)getCurrentUserSID(), TRUSTEE_IS_SID,
        TRUSTEE_IS_UNKNOWN,
        // pathw, SE_FILE_OBJECT, username, TRUSTEE_IS_NAME, TRUSTEE_IS_USER,
        newAcessingRights, SET_ACCESS,
        OBJECT_INHERIT_ACE | CONTAINER_INHERIT_ACE);

    res = AddAceToObjectsSecurityDescriptor(
        pathw, SE_FILE_OBJECT, (LPWSTR)getCurrentUserSID(), TRUSTEE_IS_SID,
        TRUSTEE_IS_UNKNOWN,
        // pathw, SE_FILE_OBJECT, username, TRUSTEE_IS_NAME, TRUSTEE_IS_USER,
        newAcessingRights, SET_ACCESS,
        OBJECT_INHERIT_ACE | CONTAINER_INHERIT_ACE);

    return res != 0 ? 1 : 0;
}

#endif /* !_STAT_H_ */
