#!/usr/bin/env python3
"""Claude Code PreToolUse hook: refuse tool calls that would read OpenJDK (GPL) sources.

Registered by the jdk-clean-room skill frontmatter. Exit code 2 blocks the call and shows stderr to the agent.
This is a guard rail against accidents, not a guarantee; the rules in ../reference/sources.md still apply.
"""
import json
import re
import sys

BLOCKED = [
    r"github\.com/openjdk",
    r"githubusercontent\.com/openjdk",
    r"api\.github\.com/repos/openjdk",
    r"\brepos/openjdk/",
    r"\bopenjdk/jdk\d*u?(-dev)?\b",
    r"github\.com/DougLea/jdk",            # OpenJDK fork, GPL
    r"(git|hg|cr)\.openjdk\.(org|java\.net)",
    r"openjdk\.org/.*/(pull|commit|raw|blob)",
    r"\bsrc\.zip\b",
    r"/lib/src(\.zip)?\b",
    r"\bCellar/[\w@.+-]*jdk",              # Homebrew JDK kegs (openjdk, openjdk@NN)
    r"/opt/openjdk",                       # Homebrew opt symlinks to those kegs
    r"oracle/graal",
    r"android\.googlesource\.com/platform/libcore",
    r"\bjavap\b.*java\.util\.concurrent",
    r"\b(cfr|procyon|fernflower|jd-cli)\b",
]
METALS_SOURCE_TOOLS = re.compile(r"mcp__metals__(get-source|inspect|get-docs)")
JUC = re.compile(r"java[./]util[./]concurrent")


def main() -> int:
    try:
        event = json.load(sys.stdin)
    except Exception:
        return 0
    tool = event.get("tool_name", "")
    payload = json.dumps(event.get("tool_input", {}))
    for pattern in BLOCKED:
        if re.search(pattern, payload, re.IGNORECASE):
            print(
                f"Blocked by clean-room guard: '{pattern}' matches a prohibited "
                "OpenJDK/GPL source (see .claude/skills/jdk-clean-room/reference/sources.md). "
                "Use JSR166 CVS (public domain) or the problem spec instead.",
                file=sys.stderr,
            )
            return 2
    if METALS_SOURCE_TOOLS.match(tool) and JUC.search(payload):
        print(
            "Blocked by clean-room guard: Metals may resolve java.util.concurrent "
            "to JDK sources. Read javalib/src/main/scala/java/util/concurrent directly.",
            file=sys.stderr,
        )
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
