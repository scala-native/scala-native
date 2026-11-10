---
name: jdk-clean-room
description: Clean-room workflow for fixing or porting Scala Native javalib (java.*, javax.*) behaviour whose reference implementation is OpenJDK (GPL). Use when a javalib bug or missing feature is fixed upstream only in OpenJDK, when someone wants to compare our code with "the JDK sources", when porting from JSR166, or when a fresh agent is told to implement a clean-room task from .claude/clean-room/<task>/.
argument-hint: "[spec <task-slug> | implement <task-slug>]"
hooks:
  PreToolUse:
    - matcher: "Bash|WebFetch|WebSearch|Read|Grep|Glob|Agent|mcp__.*"
      hooks:
        - type: command
          # Fail closed: if the guard script cannot be found, block the call.
          command: "g=scripts/block-openjdk-sources.py; for d in \"$CLAUDE_PROJECT_DIR/.claude/skills/jdk-clean-room\" \"$HOME/.claude/skills/jdk-clean-room\"; do [ -f \"$d/$g\" ] && exec python3 -I \"$d/$g\"; done; echo 'jdk-clean-room guard script not found; refusing tool call' >&2; exit 2"
---

# JDK clean-room workflow

Scala Native's javalib is distributed under a permissive licence. By signing the Scala CLA,
contributors confirm that their code is their own creation and that it was **not copied from
OpenJDK** (GPLv2 + Classpath exception), Android, GNU Classpath, or similar projects
(`docs/contrib/contributing.md`). This skill keeps that guarantee and produces an audit trail.

Claude desktop worktree sessions load project skills from the main checkout. So until this skill
is on `main`, install a personal copy first: `cp -R .claude/skills/jdk-clean-room ~/.claude/skills/`.

Invoking this skill turns on a PreToolUse guard (`scripts/block-openjdk-sources.py`) for the rest of
the session. The guard blocks fetches of OpenJDK repositories, `src.zip`, GraalVM/Android copies, and
Metals source lookups on `java.util.concurrent`. It is a safety net, not a substitute for the rules
below.

## Hard rules (both roles)

1. Never read OpenJDK source code in any form. `reference/sources.md` lists what is allowed and
   what is prohibited.
2. Never reproduce newer JDK code from memory. If you recognise an upstream solution, write that
   down in the provenance log and design your own.
3. Public-domain JSR166 code is allowed. Fetch it with `scripts/fetch-jsr166.sh`. Classes in
   `java.util.concurrent` (except `CopyOnWriteArrayList`) and the JSR166 collection interfaces are
   CC0. JSR166's changes to `TreeMap`, `AbstractMap`, `LinkedList` and `Collections` are GPL.
4. Bug-tracker prose (JBS summaries and descriptions), Java SE javadoc and specifications are
   allowed as facts and problem statements. Code snippets, diffs, and links to commits or pull
   requests found in those pages are not.

## Roles

A clean room needs two separate contexts:

- **Spec writer.** Diagnoses the problem and writes a task pack that describes **only the
  problem**: symptoms, evidence, the race or the spec violation, constraints and acceptance
  criteria. It never describes how upstream fixed it. If this session has ever seen OpenJDK
  sources, it is contaminated, and it must not implement the fix itself.
- **Implementer.** A fresh session with no shared context: never a fork of the spec writer's
  conversation. It works in its own new git worktree and sees only the task pack, our repository,
  and the allowed sources.

## Spec writer: `/jdk-clean-room spec <task-slug>`

1. Collect evidence from our own CI logs and tests. Use `gh run view --log-failed`.
2. Compare our code with the public-domain source it was ported from. The port header names it,
   e.g. `// Ported from JSR-166, revision: 1.411`. Check whether a newer public-domain revision
   already fixes the problem; if it does, port that directly and skip the clean room.
3. Look up public bug reports for prose descriptions of the same problem. Use the JBS REST API with
   `fields=summary,description` only, e.g.
   `curl -s 'https://bugs.openjdk.org/rest/api/2/issue/JDK-<id>?fields=summary,description'`.
   Paraphrase the problem and leave out solution ideas.
4. Create `.claude/clean-room/<task-slug>/` from `templates/`:
   - `README.md`: the brief.
   - `evidence.md`.
   - `problem-spec.md`.
   - `sources.md`: the task-specific allowed sources. Copy the generic list from
     `reference/sources.md` and add the task's JBS IDs.
   - `provenance-log.md`.
5. Commit the task pack on a branch, so the implementer's worktree contains it and history shows
   that the spec predates the implementation.
6. Start the implementer with `mcp__ccd_session__start_session`:
   - `context: "fresh"` (never `"fork"`).
   - `use_worktree: true`.
   - `source_branch` set to the branch holding the pack.
   - A prompt that tells it to run `/jdk-clean-room implement <task-slug>` before anything else.
7. Write down in the task README that the spec writer was contaminated (if it was) and that the
   spec intentionally contains no solution.

## Implementer: `/jdk-clean-room implement <task-slug>`

1. Read every file in `.claude/clean-room/<task-slug>/`. Follow its README.
2. Check that the guard is active. A `curl -sI https://github.com/openjdk/jdk` must be blocked.
   If it isn't blocked, stop and tell the user.
3. Write a reproducer test with a bounded wait that fails on the current code.
4. Design and implement the fix yourself. Fetch any JSR166 source you need with
   `scripts/fetch-jsr166.sh <dir> <file>`. Record every external source in
   `provenance-log.md` as you go.
5. Run the relevant test suites locally. Run CI only on the maintainer's fork via a `ci-base/*`
   draft PR, and only after asking the user. Never use upstream for proof-of-concept CI runs.
6. When opening the pull request, after the user approves:
   - Get this session's link with `mcp__ccd_session_mgmt__get_session` and `session_id: "self"`;
     use its `link` field.
   - Add a **Clean-room provenance** section to the PR description containing:
     - the link to the implementing agent session;
     - the commit that added the task pack (the spec);
     - a summary of `provenance-log.md` (sources used, design notes, the declaration).
   - If the user wants the transcript attached, export it with
     `mcp__ccd_session_mgmt__export_transcript` and ask them where to put it. Never upload it
     without asking.
7. Before the PR is published, ask the user whether `.claude/clean-room/<task-slug>/` should stay in
   the PR or be dropped. It is useful provenance, but it isn't product code.

## Files

- `scripts/block-openjdk-sources.py`: the guard hook (exit code 2 blocks the call).
- `scripts/fetch-jsr166.sh`: anonymous-CVS checkout of JSR166 files, plus their `cvs log`.
- `reference/sources.md`: generic list of allowed and prohibited sources, with licence notes.
- `templates/`: skeletons for the task pack.
