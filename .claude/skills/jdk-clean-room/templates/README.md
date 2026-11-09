# Clean-room task: <one-line problem title>

Fresh implementer: run `/jdk-clean-room implement <task-slug>` before anything else. That loads the
rules and turns on the source guard. Then read every file in this folder.

| File | Purpose |
|---|---|
| `README.md` | The task, task-specific constraints, acceptance criteria |
| `evidence.md` | Facts from our own CI logs and tests |
| `problem-spec.md` | Behaviour-only description of the defects |
| `sources.md` | Task-specific allowed sources (extends the skill's `reference/sources.md`) |
| `provenance-log.md` | Your audit trail; you fill it in |

## Spec-writer statement

<Say whether the spec writer saw OpenJDK sources. If it did: "This spec intentionally describes
only the problem and contains no solution details.">

## Task

<numbered steps: reproducer first, then fixes per defect id>

## Acceptance criteria

- The reproducer test fails before the fix and passes after it, with a bounded wait.
- Existing suites still pass: <list>.
- CI on the maintainer's fork (`ci-base/*` draft PR) passes, including <platforms>.
- `provenance-log.md` is complete. The PR has a "Clean-room provenance" section with the link to
  this session.

## Useful commands

```bash
sbt "tests3/testOnly <pattern>"
```
