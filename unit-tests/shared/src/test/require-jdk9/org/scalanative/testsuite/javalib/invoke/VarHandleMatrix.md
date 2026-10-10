# VarHandle field-operation parity matrix

This is the JVM oracle and the Native acceptance matrix for mutable instance and
static fields, including primitive operand/result widening and operand unboxing.
Primitive results may also be boxed into compatible reference types. This does
not claim parity for reflective lookup, final fields, or arrays.

Incompatible invocation signatures throw `java.lang.invoke.WrongMethodTypeException`.
The diagnostic names the actual handle variable type and requested accessor or
call-site signature (for example, `cannot perform VarHandle.getShort on a field
of type Int; unsupported type conversion`). An operation unsupported for the actual variable type still throws
`UnsupportedOperationException` (for example, Boolean addition or Float bitwise
operations). Neither failure performs a field mutation.

Historical signature-error verification, before primitive result widening:
shared regressions and both operation matrices passed
on JVM for Scala 3.9.0, 2.13.18 and 2.12.21. Full Native VarHandle suites pass
1257 / 651 / 651 tests respectively, with full NIR checks. The sandbox Int-to-Short
read links and throws `WrongMethodTypeException` identifying `int` and `getShort`.
Compiler lowering supplies diagnostic signature text. Previously compiled callers
must be cleaned/recompiled after this internal protocol signature change.

Reference: [JDK 25 VarHandle](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/invoke/VarHandle.html).
Lookup/type-token acceptance and failure cases are tracked separately in
[VarHandleLookupMatrix.md](VarHandleLookupMatrix.md).
S = operation supported; U = UnsupportedOperationException with no field mutation.

| Method | Boolean | Byte | Short | Char | Int | Long | Float | Double | Reference |
|---|---|---|---|---|---|---|---|---|---|
| `get` | S | S | S | S | S | S | S | S | S |
| `getOpaque` | S | S | S | S | S | S | S | S | S |
| `getAcquire` | S | S | S | S | S | S | S | S | S |
| `getVolatile` | S | S | S | S | S | S | S | S | S |
| `set` | S | S | S | S | S | S | S | S | S |
| `setOpaque` | S | S | S | S | S | S | S | S | S |
| `setRelease` | S | S | S | S | S | S | S | S | S |
| `setVolatile` | S | S | S | S | S | S | S | S | S |
| `compareAndSet` | S | S | S | S | S | S | S | S | S |
| `weakCompareAndSetPlain` | S | S | S | S | S | S | S | S | S |
| `weakCompareAndSet` | S | S | S | S | S | S | S | S | S |
| `weakCompareAndSetAcquire` | S | S | S | S | S | S | S | S | S |
| `weakCompareAndSetRelease` | S | S | S | S | S | S | S | S | S |
| `compareAndExchange` | S | S | S | S | S | S | S | S | S |
| `compareAndExchangeAcquire` | S | S | S | S | S | S | S | S | S |
| `compareAndExchangeRelease` | S | S | S | S | S | S | S | S | S |
| `getAndSet` | S | S | S | S | S | S | S | S | S |
| `getAndSetAcquire` | S | S | S | S | S | S | S | S | S |
| `getAndSetRelease` | S | S | S | S | S | S | S | S | S |
| `getAndAdd` | U | S | S | S | S | S | S | S | U |
| `getAndAddAcquire` | U | S | S | S | S | S | S | S | U |
| `getAndAddRelease` | U | S | S | S | S | S | S | S | U |
| `getAndBitwiseOr` | S | S | S | S | S | S | U | U | U |
| `getAndBitwiseOrAcquire` | S | S | S | S | S | S | U | U | U |
| `getAndBitwiseOrRelease` | S | S | S | S | S | S | U | U | U |
| `getAndBitwiseAnd` | S | S | S | S | S | S | U | U | U |
| `getAndBitwiseAndAcquire` | S | S | S | S | S | S | U | U | U |
| `getAndBitwiseAndRelease` | S | S | S | S | S | S | U | U | U |
| `getAndBitwiseXor` | S | S | S | S | S | S | U | U | U |
| `getAndBitwiseXorAcquire` | S | S | S | S | S | S | U | U | U |
| `getAndBitwiseXorRelease` | S | S | S | S | S | S | U | U | U |

## Executable coverage

`VarHandleMatrixTest.scala.gyb` generates both shapes with identical expectations:

- `VarHandleMatrixTest.scala`: 279 instance-field tests, shared by Scala 3, 2.13 and 2.12.
- `VarHandleStaticMatrixTest.scala`: 279 static-field tests, Scala 3 `@static` fixtures.

Each shape has 246 successful-operation cells and 33 unsupported-operation cells.
Every cell is a separate JUnit test; unsupported cells are tested, not skipped.
Typed calls avoid reflection, varargs arrays and generic/typeclass erasure.
All fields are reset directly before invocation, so testing one mode does not
depend on the correctness of another mode.

CAS tests cover success and mismatch; weak CAS uses bounded retries.
Compare-and-exchange checks successful and failed witnesses and final state.
Reference witnesses use identity assertions; floating witnesses use raw-bit assertions.
Boolean bitwise tests cover the complete two-operand truth table.

The compiler matrix covers all 279 method/type pairs with and without an
instance coordinate on each Scala version. It checks exact typed dispatch,
memory-order and bitwise-operation constants read from NativeVarHandle itself,
and absence of varargs allocation. Exact primitive branches remain unboxed;
the conditional adaptation branches use explicit boxing/unboxing helpers.

VarHandleFenceTest invokes all five fences on both JVM and Native. These are
availability smoke tests, not inter-thread ordering tests.

### Discarded RMW results

`VarHandleDiscardedMatrixTest.scala.gyb` covers all 18 value-returning RMW modes
across the same nine variable types. Each cell is invoked both as a bare
statement and as a Unit-returning method body. The generated instance and
Scala 3 static suites each contain 324 tests: 258 supported calls and 66
unsupported calls. Supported calls must update the field even though their
witness is discarded; unsupported calls must throw without changing the field.

The compiler regression checks all 648 type/method/coordinate/context call sites
for exact fast-path dispatch, no direct boxing/unboxing or varargs instructions,
and no substitution of `unsupportedSignature` for a discarded witness.
Separate compiler/runtime cases
cover if/match/try-finally, evaluation order, nested operands, bound boxed results
and incompatible operands. Scala 2.12 synthetic match joins are identified by
their symbols, not generated names.

Regenerate the discarded-result suites:

```sh
scripts/gyb.py unit-tests/shared/src/test/require-jdk9/org/scalanative/testsuite/javalib/invoke/VarHandleDiscardedMatrixTest.scala.gyb --line-directive '' -D static=false -o unit-tests/shared/src/test/require-jdk9/org/scalanative/testsuite/javalib/invoke/VarHandleDiscardedMatrixTest.scala
scripts/gyb.py unit-tests/shared/src/test/require-jdk9/org/scalanative/testsuite/javalib/invoke/VarHandleDiscardedMatrixTest.scala.gyb --line-directive '' -D static=true -o unit-tests/shared/src/test/require-scala3-jdk9/org/scalanative/testsuite/javalib/invoke/VarHandleStaticDiscardedMatrixTest.scala
```

Regenerate from the repository root:

```sh
scripts/gyb.py unit-tests/shared/src/test/require-jdk9/org/scalanative/testsuite/javalib/invoke/VarHandleMatrixTest.scala.gyb --line-directive '' -D static=false -o unit-tests/shared/src/test/require-jdk9/org/scalanative/testsuite/javalib/invoke/VarHandleMatrixTest.scala
scripts/gyb.py unit-tests/shared/src/test/require-jdk9/org/scalanative/testsuite/javalib/invoke/VarHandleMatrixTest.scala.gyb --line-directive '' -D static=true -o unit-tests/shared/src/test/require-scala3-jdk9/org/scalanative/testsuite/javalib/invoke/VarHandleStaticMatrixTest.scala
```

Run JVM first, then compiler and Native, in Scala 3 / 2.13 / 2.12 order.
Select `*InstanceMatrixTest` and `*StaticMatrixTest` explicitly.

## Additional dimensions

Existing regression tests cover overflow, floating-point signed zero and NaN,
reference identity and null, contended floating addition, stored/passed handles,
discarded results and argument evaluation order. The field-operation matrix
checks supported operation semantics, not a proof of memory ordering.
Acquire/release publication, fence ordering and cross-platform stress testing
remain separate work.

Static fixtures for Scala 2 are not yet provided. Scala 3 static results must not
be reported as Scala 2 static coverage. Operand widening/unboxing and result
widening/boxing use identical JVM and Native expectations. No shared VarHandle
test retains an `executingInJVM` branch. Incompatible-signature tests also share
exception/type-diagnostic assertions rather than platform-specific wording.

### Operand adaptation

`VarHandleOperandMatrixTest.scala.gyb` generates 2,443 tests per field shape:
all 27 operand-taking access modes, eight exact primitive pairs and all 19 legal
widening pairs, each with primitive, wrapper-typed and `AnyRef`-typed operands.
Unsupported Boolean addition and Float/Double bitwise operations are tested,
not skipped. Static fixtures remain Scala 3-only.
The additional 256 cells per shape exercise mixed primitive/wrapper CAS operands
and null wrapper operands in either CAS position across all eight primitive
types and all eight CAS/compare-exchange modes. Null desired operands must throw
even when the expected value would not match the field.

Exact primitive calls retain an unboxed fast path, guarded by the actual handle's
declared variable type. The fallback validates operand and result signatures
before mutation, then unboxes/widens operands and invokes the atomic operation
for the actual field type. The RMW witness therefore reflects the field type,
not the source operand type. Declared reference field types are retained too.
Arguments are saved once, in source order, before choosing either path.

The compiler selects a dedicated factory for each field type. Primitive factories
take only instance/static bindings; the reference factory also retains the declared
field class. Each concrete handle dispatches adapted operations directly to its
typed helper after shared validation. There is no field-kind tag or factory switch.
Operation, memory-order, and bitwise matches use `@switch`. `AccessOperation`,
`MemoryOrder`, and `BitwiseOperation` are Scala 2-compatible `Int` aliases;
valid values are defined by their protocol companions, not duplicated literals.
Compiler regressions require NIR switch instructions for all three alias groups.
LLVM remains free to choose the final machine-code dispatch strategy.

Exact static operands bypass conversion and compatibility checks. A wrapper
matching the primitive field type also bypasses generic conversion, but still
checks null before mutation. Exact primitive or boxed result signatures skip
generic validation; compatible primitive-to-reference witnesses need boxing
only, and discarded witnesses are never boxed by the per-type result helpers.

`VarHandleOperandBoundaryTest` checks full-width Long witnesses with Int operands,
mixed CAS types, failed CAS witnesses, null unboxing, static signature rejection,
dynamic cast failures, forbidden Byte/Short-to-Char conversions, failed result
conversion before mutation, evaluation order, and Number-typed unboxing/widening.
The compiler operand matrix checks 1,458 call sites across all modes and both
coordinate shapes, requiring adaptation dispatch without varargs allocation.

Verified on 2026-10-05 with JDK 25, in Scala 3 / 2.13 / 2.12 order:

| Scala | JVM VarHandle tests | Native VarHandle tests |
|---|---:|---:|
| 3.9.0 | 8,914 passed | 8,914 passed |
| 2.13.18 | 4,493 passed | 4,493 passed |
| 2.12.21 | 4,493 passed | 4,493 passed |

All VarHandle compiler suites passed on all three versions; four Scala 3-only
static lookup cases are skipped on Scala 2. Native runs used full NIR checking
before and after optimization. Scala 3 includes static runtime matrices.

Reference operand adaptation checks `Class.isInstance` explicitly: Native's
current generic `Class.cast` does not enforce the target class dynamically.
Mixed-reference CAS regressions verify that invalid desired values throw before
comparison or mutation. Scala 2 lowering duplicates generated branch trees
before typing to avoid sharing mutable AST nodes between the two access paths.

## Primitive result widening

The following 19 conversions are allowed for reads and value-returning RMW calls:

| Field / operand type | Allowed wider result types |
|---|---|
| Byte | Short, Int, Long, Float, Double |
| Short | Int, Long, Float, Double |
| Char | Int, Long, Float, Double |
| Int | Long, Float, Double |
| Long | Float, Double |
| Float | Double |

Read bridges use the actual handle's field type. For RMW, the compiler calls the
exact typed atomic operation and then widens its witness. The field operation,
overflow behavior and memory order remain unchanged. Direct source-to-target
conversion avoids losing precision through an intermediate Float when returning
Double. Both compiler plugins validate primitive widening and select the
primitive's `toX` method directly; existing NIR generation emits the conversion
without a runtime widening helper.

`VarHandleWideningMatrixTest.scala.gyb` generates 788 tests per field shape:

- 418 cells: 19 conversions across four read and 18 RMW modes. Float bitwise
  operations retain their nine unsupported-operation cells.
- 370 cells: all 37 other distinct primitive conversion pairs across four reads,
  three exchanges and three compare-and-exchanges; these throw
  `WrongMethodTypeException` without mutation.

The instance suite is shared across all three Scala versions; the static suite
uses Scala 3 fixtures. Floating results and unchanged floating fields are checked
by raw bits, including negative zero. Floating fixtures are initialized through
exact VarHandle stores from explicit bit patterns. Five additional boundary tests
cover unknown handles, argument evaluation order, integer overflow, precision,
NaN/infinities/signed zero and rejected narrowing without mutation.

The compiler regression covers 684 widened RMW call sites (19 conversions,
18 modes and two coordinate shapes), checking typed conversion dispatch and no
boxing, unboxing or varargs allocation. Regenerate both widening suites using
`scripts/gyb_all.sh`, alongside the existing operation and discarded-result suites.

### Widening verification on 2026-10-05

JDK 25, macOS arm64. JVM suites were run first in Scala 3 / 2.13 / 2.12 order,
followed by Native in the same order. Full Native NIR correctness checks passed
before and after optimization; dumps and source-level debug metadata were disabled.

| Scala | JVM VarHandle runtime tests | Native VarHandle runtime tests | VarHandle compiler suite |
|---|---|---|---|
| 3.9.0 | 2838 pass | 2838 pass | 13 pass |
| 2.13.18 | 1444 pass | 1444 pass | 13 pass |
| 2.12.21 | 1444 pass | 1444 pass | 13 pass |

The Scala 3 totals include both instance and static matrices. Scala 2 totals
include instance matrices only. This verification does not include the separately
tracked lookup-validation compiler suite or platforms other than macOS arm64.

## Primitive result boxing

Reads and value-returning RMW operations can return the field's primitive wrapper,
`Object`/`AnyRef`, or a compatible wrapper supertype. Boxing does not first widen:
an Int field returns `Integer`, never `Double`. Incompatible reference result
types throw `WrongMethodTypeException` before mutation, not `ClassCastException`
after the atomic operation. Arguments are still evaluated once in source order.

The public `java.lang.invoke.VarHandle` API is unchanged. The internal protocol
adds `getBoxedReference(receiver, resultType, mode)` so reads through unknown
handles can validate the requested reference class before returning a boxed value.
RMW lowering reuses the standard `scala.runtime.BoxesRunTime.boxTo*` methods.
Both compiler plugins unbox adapted witnesses directly with a wrapper cast and
its primitive `*Value()` method, preserving null-unboxing exceptions rather than
Scala's null-to-zero defaults. There are no dedicated runtime boxing or unboxing
helpers. Exact primitive access and discarded results remain unboxed.
Native's Boolean wrapper also implements `Serializable`, matching JVM wrappers.

`VarHandleBoxingMatrixTest.scala.gyb` generates 840 tests per field shape:

- 660 cells across all 22 read/RMW modes, returning Object, the exact wrapper,
  Serializable, or Number when compatible. These include 81 unsupported-operation
  cells, which must throw without mutation.
- 180 incompatible-reference cells across four reads and six exchange modes,
  checking String, a wrong wrapper, and Number for Boolean/Char fields.

Three shared boundary tests check inferred/Any results, argument evaluation on
incompatible boxing, and operand unboxing with boxed witnesses. The compiler
matrix checks 288 boxed RMW call sites across eight primitive types, 18 modes and
two coordinate shapes, without varargs allocation. Regenerate both boxing suites
using `scripts/gyb_all.sh`.

### Boxing verification on 2026-10-05

JDK 25, macOS arm64. Full JVM VarHandle suites were run first, followed by
compiler tests and Native suites, each in Scala 3 / 2.13 / 2.12 order. Native
full NIR checks passed before and after optimization; dumps and source-level debug
metadata were disabled.

| Scala | JVM VarHandle runtime tests | Native VarHandle runtime tests | VarHandle compiler suite |
|---|---|---|---|
| 3.9.0 | 4521 pass | 4521 pass | 14 pass |
| 2.13.18 | 2287 pass | 2287 pass | 14 pass |
| 2.12.21 | 2287 pass | 2287 pass | 14 pass |

Scala 3 includes static matrices; Scala 2 includes instance matrices only.
The separate lookup-validation compiler suite and other platforms are not covered
by this verification.

## Validation on 2026-10-04

JDK 25, macOS arm64; these results do not cover 32-bit platforms.

| Scala | JVM instance cells | JVM static cells | Native instance cells | Native static cells | Compiler matrix |
|---|---|---|---|---|---|
| 3.9.0 | 279 pass | 279 pass | 279 pass | 279 pass | 558 call sites pass |
| 2.13.18 | 279 pass | Not provided | 279 pass | Not provided | 558 call sites pass |
| 2.12.21 | 279 pass | Not provided | 279 pass | Not provided | 558 call sites pass |

All five fence smoke tests pass on both JVM and Native on all three Scala versions.

The previously failing `VarHandleTest.discardedRmwStillUpdatesTheField` now passes.
Discarded result paths are marked before access lowering; the exact typed RMW
operation executes without boxing its witness. Reference-typed control-flow
joins receive an unused null result rather than a boxed primitive. Bound and
operand results still undergo signature validation.

The expanded compiler suite passes all 11 tests on each Scala version, including
648 discarded call sites in addition to the 558 exact-result call sites.
The discarded-result matrix and wider runtime regressions pass on JVM: 667 tests
on Scala 3 (both field shapes), 341 each on Scala 2.13 and 2.12 (instance fields).
The full VarHandle Native suite passes 1246 tests on Scala 3 and 641 on each of
Scala 2.13 and 2.12. The reported discarded-RMW failure passes on all three.

Historical full VarHandle verification (before removal of development-only
JVM lookup oracles and addition of shared token/multiple-static-field checks):

| Scala | JVM runtime tests | Native runtime tests | VarHandle compiler suite |
|---|---|---|---|
| 3.9.0 | 1333 pass | 1246 pass | 11 pass |
| 2.13.18 | 728 pass | 641 pass | 11 pass |
| 2.12.21 | 728 pass | 641 pass | 11 pass |

The 87 development-only JVM lookup oracle tests explain the historical count
difference. These fixtures are no longer retained; current runtime tests are
shared, including eight moved class-token checks and one new Scala 3
multiple-static-field check. Regenerate all VarHandle matrices using
`scripts/gyb_all.sh`.
The separately documented lookup-validation compiler failures are outside this
discarded-result fix and are not claimed to pass.

Native runs use full NIR correctness checks, with NIR dumps and source-level debug
metadata disabled for this verification session to reduce disk usage. No platform
coverage beyond macOS arm64 is implied.

The first Scala 2.12 link ran out of disk space. The retry passed after removing
only regenerable LLVM/object intermediates from the completed Scala 3 and 2.13
test builds (their executables/reports were retained) and reducing the sbt heap
to 3 GB. All compiler tests were rerun successfully after formatting.

## Helper-free adaptation verification on 2026-10-05

After removing the dedicated boxing, unboxing and widening helpers, the full
shared VarHandle runtime suites passed on JVM and Native (JDK 25, macOS arm64).
Versions were tested in Scala 3, 2.13, 2.12 order, with JVM before Native per
version. Native used full NIR checks before and after optimization.

| Scala | JVM runtime tests | Native runtime tests | Access-lowering compiler tests |
|---|---|---|---|
| 3.9.0 | 9426 pass | 9426 pass | 18 pass |
| 2.13.18 | 4749 pass | 4749 pass | 18 pass |
| 2.12.21 | 4749 pass | 4749 pass | 18 pass |

The compiler regression additionally verifies direct wrapper instance unboxing
for all eight primitives, without Scala's null-defaulting unboxing methods.
The separately tracked lookup-validation compiler suites were not rerun in this
verification session.

## Dedicated-factory migration verification on 2026-10-05

The runtime has eight primitive factories plus `createReferenceHandle`. Primitive
factories accept only the two field bindings; the reference factory additionally
accepts the declared field class. The compiler selects the factory by field type.
Each concrete handle overrides adapted-operation dispatch to call only its own
typed helper, retaining shared signature validation and conversion before mutation.
The field-kind protocol and the generic factories have been removed.

`fieldLookupsUseDedicatedFactories` verifies all nine instance factories and,
on Scala 3, all nine static factories. It checks the selected method, parameter
count, absence of integer tags, and reference-only class tokens. Boxed primitive
field lookups separately verify selection of the reference factory.

All VarHandle compiler suites, including lookup validation, passed on Scala 3,
2.13 and 2.12; four Scala 3-only static lookup-validation cases are skipped on
each Scala 2 version. The access-lowering suite now has 19 tests per version.
Full JVM/Native runtime counts remain 9426/9426 for Scala 3 and 4749/4749 for each
Scala 2 version. Native NIR checks passed before and after optimization.

Initial incremental Native runs found stale NIR calling removed factories. The
successful runs followed `disable-action-cache` and project-scoped cleans of
`tests3`, `tests2_13` and `tests2_12`, regenerating all Native test sources; no
compatibility factories were reintroduced.
