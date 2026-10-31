#!/usr/bin/env python3
"""Test production static-root registration in isolated native processes.

Runs on Linux and macOS; CC selects the Clang executable. Each process has a
30-second deadline, so a broken once gate reports a failure instead of hanging CI.
"""
import os
from pathlib import Path
import shlex
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[2]
NATIVE = ROOT / "nativelib/src/main/resources/scala-native"


def main():
    with tempfile.TemporaryDirectory(prefix="static-roots-") as temporary:
        for threaded in (False, True):
            for populated in (False, True):
                for thin_lto in (False, True):
                    name = f"threads={threaded}, populated={populated}, ThinLTO={thin_lto}"
                    executable = Path(temporary) / "registration"
                    command = shlex.split(os.environ.get("CC", "clang")) + [
                        "-std=c11", "-O2", "-pthread",
                        "-I" + str(NATIVE), "-I" + str(NATIVE / "gc"),
                    ]
                    if threaded:
                        command.append("-DSCALANATIVE_MULTITHREADING_ENABLED")
                    if populated:
                        command.append("-DPOPULATED_SECTIONS")
                    if thin_lto:
                        command.append("-flto=thin")
                    command += (["-Wl,-dead_strip"] if sys.platform == "darwin"
                                else ["-fuse-ld=lld", "-Wl,--gc-sections"])
                    command += [str(NATIVE / "staticRoots.c"),
                                str(NATIVE / "staticSections.c"),
                                str(ROOT / "scripts/ci/static-roots/registration.c"),
                                "-o", str(executable)]
                    print(name, flush=True)
                    subprocess.run(command, check=True, timeout=120)
                    for first in ("classes", "strings"):
                        subprocess.run([str(executable), first], check=True, timeout=30)


if __name__ == "__main__":
    main()
