# Make the pagefile on $DiskRoot at least $MinimumSize, and fail if that does
# not work. It replaces al-cheb/configure-pagefile-action, which reports errors
# as "The operation completed successfully" and still exits 0.
param(
    [uint64]$MinimumSize = 4GB,
    [uint64]$MaximumSize = 12GB,
    [string]$DiskRoot = "C:"
)

Set-StrictMode -Version 3.0
$ErrorActionPreference = "Stop"

if (-not ("ScalaNative.Ci.PageFile" -as [type])) {
    Add-Type -TypeDefinition @"
using System;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Text;
using Microsoft.Win32.SafeHandles;

namespace ScalaNative.Ci
{
    class NativeMethods
    {
        [StructLayout(LayoutKind.Sequential)]
        internal struct LUID
        {
            internal uint LowPart;
            internal uint HighPart;
        }

        [StructLayout(LayoutKind.Sequential)]
        internal struct LUID_AND_ATTRIBUTES
        {
            internal LUID Luid;
            internal uint Attributes;
        }

        [StructLayout(LayoutKind.Sequential)]
        internal struct TOKEN_PRIVILEGE
        {
            internal uint PrivilegeCount;
            internal LUID_AND_ATTRIBUTES Privilege;
            internal static readonly uint Size = (uint)Marshal.SizeOf(typeof(TOKEN_PRIVILEGE));
        }

        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
        internal struct UNICODE_STRING
        {
            internal ushort length;
            internal ushort maximumLength;
            internal string buffer;
        }

        [DllImport("advapi32.dll", ExactSpelling = true, CharSet = CharSet.Unicode, SetLastError = true)]
        internal static extern bool LookupPrivilegeValueW(string lpSystemName, string lpName, out LUID luid);

        [DllImport("advapi32.dll", SetLastError = true)]
        internal static extern bool AdjustTokenPrivileges(
            SafeCloseHandle tokenHandle,
            bool disableAllPrivileges,
            ref TOKEN_PRIVILEGE newState,
            uint bufferLength,
            out TOKEN_PRIVILEGE previousState,
            out uint returnLength);

        [DllImport("advapi32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        internal static extern bool OpenProcessToken(IntPtr processToken, int desiredAccess, out SafeCloseHandle tokenHandle);

        [DllImport("ntdll.dll", CharSet = CharSet.Unicode)]
        internal static extern int NtCreatePagingFile(ref UNICODE_STRING pageFileName, ref long minimumSize, ref long maximumSize, uint flags);

        [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
        internal static extern uint QueryDosDeviceW(string lpDeviceName, StringBuilder lpTargetPath, int ucchMax);
    }

    public sealed class SafeCloseHandle : SafeHandleZeroOrMinusOneIsInvalid
    {
        [DllImport("kernel32.dll", ExactSpelling = true, SetLastError = true)]
        static extern bool CloseHandle(IntPtr handle);
        private SafeCloseHandle() : base(true) {}
        public SafeCloseHandle(IntPtr preexistingHandle, bool ownsHandle) : base(ownsHandle) { SetHandle(preexistingHandle); }
        protected override bool ReleaseHandle() { return CloseHandle(handle); }
    }

    public class PageFile
    {
        public static int TrySet(long minimumValue, long maximumValue, string dosDevice)
        {
            const int SE_PRIVILEGE_ENABLED = 0x00000002;
            const int AdjustPrivileges = 0x00000020;
            const int Query = 0x00000008;
            const int ERROR_NOT_ALL_ASSIGNED = 1300;

            NativeMethods.LUID luid;
            if (!NativeMethods.LookupPrivilegeValueW(null, "SeCreatePagefilePrivilege", out luid))
            {
                Console.WriteLine("LookupPrivilegeValueW failed win32={0}", Marshal.GetLastWin32Error());
                return -1;
            }

            SafeCloseHandle token;
            if (!NativeMethods.OpenProcessToken(Process.GetCurrentProcess().Handle, AdjustPrivileges | Query, out token))
            {
                Console.WriteLine("OpenProcessToken failed win32={0}", Marshal.GetLastWin32Error());
                return -1;
            }

            NativeMethods.TOKEN_PRIVILEGE previousState;
            NativeMethods.TOKEN_PRIVILEGE newState;
            uint previousSize = 0;
            newState.PrivilegeCount = 1;
            newState.Privilege.Luid = luid;
            newState.Privilege.Attributes = SE_PRIVILEGE_ENABLED;
            bool adjusted = NativeMethods.AdjustTokenPrivileges(token, false, ref newState, NativeMethods.TOKEN_PRIVILEGE.Size, out previousState, out previousSize);
            int adjustError = Marshal.GetLastWin32Error();
            Console.WriteLine("AdjustTokenPrivileges returned {0} win32={1}", adjusted, adjustError);
            if (!adjusted || adjustError == ERROR_NOT_ALL_ASSIGNED)
            {
                return -1;
            }

            StringBuilder target = new StringBuilder(260);
            if (NativeMethods.QueryDosDeviceW(dosDevice, target, target.Capacity) == 0)
            {
                Console.WriteLine("QueryDosDeviceW({0}) failed win32={1}", dosDevice, Marshal.GetLastWin32Error());
                return -1;
            }

            string pageFilePath = target.ToString() + "\\pagefile.sys";
            Console.WriteLine("NtCreatePagingFile {0} min={1} max={2}", pageFilePath, minimumValue, maximumValue);
            NativeMethods.UNICODE_STRING name = new NativeMethods.UNICODE_STRING
            {
                length = (ushort)(pageFilePath.Length * 2),
                maximumLength = (ushort)(2 * (pageFilePath.Length + 1)),
                buffer = pageFilePath
            };
            int status = NativeMethods.NtCreatePagingFile(ref name, ref minimumValue, ref maximumValue, 0);
            Console.WriteLine("NTSTATUS=0x{0:X8}", status);
            return status;
        }
    }
}
"@
}

function Get-PagefileAllocatedMb {
    param([string]$Root)
    $usage = Get-CimInstance Win32_PageFileUsage | Where-Object { $_.Name -like "$Root\*" }
    if (-not $usage) {
        return 0
    }
    return [int](@($usage)[0].AllocatedBaseSize)
}

function Write-PagefileState {
    Write-Host "--- pagefile usage ---"
    Get-CimInstance Win32_PageFileUsage |
        Select-Object Name, AllocatedBaseSize, CurrentUsage, TempPageFile |
        Format-List |
        Out-String |
        Write-Host
    $os = Get-CimInstance Win32_OperatingSystem
    $commitMb = [math]::Round($os.TotalVirtualMemorySize / 1KB, 1)
    $ramMb = [math]::Round($os.TotalVisibleMemorySize / 1KB, 1)
    Write-Host "RAM_MB=$ramMb CommitLimit_MB=$commitMb"
}

$minimumMb = [int]($MinimumSize / 1MB)
Write-Host "Requested pagefile on ${DiskRoot}: minimum $minimumMb MB, maximum $([int]($MaximumSize / 1MB)) MB"
Write-PagefileState

# Windows cannot make the maximum smaller than the current one. In that case
# it returns STATUS_INVALID_PARAMETER_3 (0xC00000F1), so try larger maximums.
# A system-managed pagefile has a maximum of up to 3x RAM: 48GB on a 16GB runner.
$candidateMax = @($MaximumSize, 24GB, 48GB, 64GB) | Select-Object -Unique
$configured = $false
foreach ($max in $candidateMax) {
    if ($max -lt $MinimumSize) {
        continue
    }
    $status = [ScalaNative.Ci.PageFile]::TrySet([int64]$MinimumSize, [int64]$max, $DiskRoot)
    if ($status -eq 0) {
        $configured = $true
        break
    }
    if ($status -ne -1073741583) { # not 0xC00000F1
        break
    }
    Write-Host "Maximum $([int]($max / 1MB)) MB was rejected, trying a larger one."
}

# Win32_PageFileUsage can lag a little behind the change.
Start-Sleep -Seconds 2
$allocatedMb = Get-PagefileAllocatedMb -Root $DiskRoot
Write-Host "Allocated on ${DiskRoot}: $allocatedMb MB (need >= $minimumMb MB)"
Write-PagefileState

if ($allocatedMb -lt $minimumMb) {
    throw "Pagefile on $DiskRoot is $allocatedMb MB, need $minimumMb MB. See the log above for the error."
}
if (-not $configured) {
    Write-Host "::warning::Pagefile change failed, but $DiskRoot already has $allocatedMb MB."
}
Write-Host "Pagefile on $DiskRoot is $allocatedMb MB."
exit 0
