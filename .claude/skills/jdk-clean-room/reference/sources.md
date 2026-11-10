# Allowed and prohibited sources (generic)

## Allowed

| Source | Status | Notes |
|---|---|---|
| Scala Native repository, CI logs | ours | |
| JSR166 CVS `:pserver:anonymous@gee.cs.oswego.edu/home/jsr166/jsr166` | public domain; file headers point to CC0 | Use `scripts/fetch-jsr166.sh`. viewcvs web UI returns 404 (checked 2026-11-09). Last `ForkJoinPool.java` revision is 1.411 (2022-11-25); later JDK fixes exist only in OpenJDK |
| https://gee.cs.oswego.edu/dl/concurrency-interest/ | Doug Lea's site | Says JSR166-originated sources are public domain: all of `java.util.concurrent` and subpackages except `CopyOnWriteArrayList`, and `java.util` `Deque`, `NavigableMap`, `NavigableSet`, `Queue`, `AbstractQueue`, `ArrayDeque` |
| JSR166 javadoc https://gee.cs.oswego.edu/dl/jsr166/dist/docs/ | public domain | |
| Java SE API javadoc / JLS / JVMS / JEPs (prose) | specification | Behaviour only |
| JBS (bugs.openjdk.org) issue summary and description prose | facts | No code snippets, diffs, or links to commits/PRs/source files |
| Scala.js javalib, Apache Harmony | BSD / Apache | Allowed case by case per `docs/contrib/contributing.md`; keep their licence headers |

## Prohibited

- OpenJDK source in any form: `github.com/openjdk/*` (jdk, jdkNNu, jdkNNu-dev), `git.openjdk.org`, `hg.openjdk.org`, `cr.openjdk.org` webrevs, OpenJDK pull requests and commits, mailing-list patches.
- Doug Lea's personal OpenJDK fork (`github.com/DougLea/jdk`): it is OpenJDK and therefore GPL.
- JSR166's modified versions of `TreeMap`, `AbstractMap`, `LinkedList`, `Collections` (GPL+CE).
- `src.zip` / `lib/src.zip` from an installed JDK, decompiled JDK classes, and IDE or Metals go-to-definition into JDK classes.
- Derived copies: GraalVM, Android libcore, GNU Classpath, OpenJ9 class library.
- Blogs and Q&A answers that quote JDK implementation code.
- Code recalled from memory of any of the above.
