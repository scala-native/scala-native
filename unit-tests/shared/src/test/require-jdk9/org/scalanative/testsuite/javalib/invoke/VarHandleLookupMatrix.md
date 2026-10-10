# VarHandle lookup acceptance matrix

Lookup validation is separate from the field-operation matrix in
[VarHandleMatrix.md](VarHandleMatrix.md). JVM lookup happens at runtime; Native
resolves the supported literal lookups at compile time. Invalid Native lookups
must not silently produce a handle for a different field or type.

## Primitive and wrapper class tokens

For each of Boolean, Byte, Short, Char/Character, Int/Integer, Long, Float and
Double, `classOf[Primitive]` and `java.lang.Wrapper.TYPE` identify the same
primitive class. `classOf[java.lang.Wrapper]` identifies a different reference
class. A boxed field is a reference field, not an atomic primitive field.

| Declared field type | `classOf[Primitive]` | `Wrapper.TYPE` | `classOf[Wrapper]` |
|---|---|---|---|
| Primitive | Accept | Accept | Reject |
| Boxed wrapper | Reject | Reject | Accept |

Reject means `NoSuchFieldException` on JVM and a type-mismatch compile error on
Native. Matching is exact, not assignability or unboxing. Shared runtime tests
also assert primitive-class identity and wrapper-class distinction.

Executable coverage:

- `nscplugin/.../VarHandleLookupTypeTests.scala.gyb`: 48 compiler tests, six per type.
- `VarHandlePrimitiveLookupTest.scala.gyb`: 32 shared runtime tests exercising
  class-token identity and read, write and CAS through both accepted primitive
  tokens, plus reference read/write/CAS/exchange for all eight boxed field types,
  on JVM and Native.
- `StaticVarHandleTest.scala`: shared Scala 3 runtime tests, including selection
  of multiple static fields with different variable types.

Compiler fixtures expose a direct field read so Scala 3 cannot eliminate the
private field as unused before the Native interop phase.

Boxed-field lookup acceptance is covered by compiler tests and shared runtime
tests. These fields use reference operations; accepting their wrapper token does
not make them primitive atomic fields or enable operand unboxing.

## Additional lookup validation

| Case | JVM oracle | Native expectation |
|---|---|---|
| Missing/empty name; getter without backing field | `NoSuchFieldException` | Compile error |
| Wrong primitive type; String requested as Object/CharSequence | `NoSuchFieldException` | Exact-type compile error |
| Instance/static direction mismatch | `IllegalAccessException` | Compile error |
| Private field from unrelated lookup | `IllegalAccessException` | Access compile error |
| Public Scala getter, private JVM backing field | `IllegalAccessException` | Access compile error |
| Null owner/name/type | `NullPointerException` | Compile error |
| Dynamic owner/type/name | Runtime resolution using actual values | Reject unresolved inputs |
| Side-effecting owner expression | Evaluate expression before lookup | Reject; do not erase evaluation |
| Passed Lookup | Privileges belong to its originating lookup class | Reject unproven lookup provenance |
| Final instance/static field | Read succeeds; set/CAS throw `UnsupportedOperationException` | Explicit mutable-only subset rejection |
| Multiple static fields | Resolve the requested name and exact type | Same field selection |
| Missing static name despite another matching-type field | `NoSuchFieldException` | Compile error |

`VarHandleLookupValidationTests` has 17 cross-version tests and four Scala 3
static tests, explicitly skipped on Scala 2. Invalid and dynamic lookups belong
in compiler-validation tests because Native rejects them before runtime.
JVM-only lookup oracles are local development tools, not retained test sources.

Final-field support is a documented Native subset difference, not JVM parity.
These tests do not yet cover inherited/shadowed fields, protected/package access,
`privateLookupIn`, arrays, byte-array/buffer views, or class-initialization timing.

## Running and regenerating

Run in Scala 3, 2.13, 2.12 order. With `V` replaced by `3`, `2_13`, or `2_12`:

```text
testsJVMV/testOnly *PrimitiveLookupTest *StaticVarHandleTest
nscpluginV/testOnly *LookupTypeTests *LookupValidationTests
testsV/testOnly *PrimitiveLookupTest *StaticVarHandleTest
```

```sh
scripts/gyb_all.sh
```

## Shared runtime verification after removing JVM-only fixtures

The selected shared lookup suites pass on JDK 25 and macOS arm64, JVM first
and then Native, in Scala 3 / 2.13 / 2.12 order:

| Scala | JVM | Native |
|---|---|---|
| 3.9.0 | 27 pass (24 primitive + 3 static) | 27 pass |
| 2.13.18 | 24 pass | 24 pass |
| 2.12.21 | 24 pass | 24 pass |

Native runs enabled full NIR correctness checks and disabled dumps/debug metadata
for the verification session. `scripts/gyb_all.sh` completed successfully; all six
generated VarHandle outputs match their templates. The compiler lookup failures
listed below were not changed or rerun as part of this test-source cleanup.

## Lookup-validation fixes verified on 2026-10-05

The historical compiler failures below are fixed. Scala 3 now recognizes only
literal class tokens and actual wrapper TYPE symbols, without unboxing wrapper
class literals or using Class[T] as proof of a literal expression. Both plugins
require direct MethodHandles.lookup() provenance and validate the backing field's
access independently of its public Scala getters. Scala 3 classifies boxed fields
as references rather than primitive storage.

All applicable VarHandle compiler tests pass on Scala 3, 2.13 and 2.12, including
the 69 lookup cases and 15 lowering regressions. Four Scala 3 static cases are
skipped on Scala 2. The added lowering regression checks the reference field-kind
constant for all eight boxed field types. The nscplugin3/test task also passes.

JVM-first and Native verification of the selected shared lookup, boxing-boundary
and general VarHandle regressions passes 57 tests on Scala 3 and 54 each on
Scala 2.13 and 2.12. This includes eight boxed-field tests with identity-based CAS,
including rejection of a distinct wrapper with the same value. Native full NIR
checks pass before and after optimization; dumps/debug metadata were disabled.
Verification uses JDK 25 on macOS arm64 and does not claim other platforms.

## Historical lookup audit on 2026-10-04

JDK 25, macOS arm64. All 103 new JVM tests pass on Scala 3.9.0, 2.13.18 and
2.12.21 (87 development-only JVM lookup tests plus 16 shared primitive lookup
tests at the time). The JVM-only sources were subsequently removed; their
class-token checks were moved into the shared tests. Counts below describe the
original audit, not the current runtime suite.

The new compiler tests intentionally retain the desired JVM-derived expectations;
they are not marked as expected failures or weakened to match implementation bugs.

Scala 3 failed 21 of 69 compiler tests in the original audit:

- Eight primitive fields incorrectly accept `classOf[Wrapper]`.
- Eight boxed fields incorrectly reject their own `classOf[Wrapper]`.
- Dynamic owner/type tokens and side-effecting owner expressions are accepted.
- Public Scala getters incorrectly grant backing-field access.
- Lookup parameters with unproven privileges are accepted.

Scala 2.13 and 2.12 pass all 48 primitive/wrapper token cases. Each has two
failures among 65 applicable compiler tests: public-getter access and unproven
Lookup privileges. The four Scala 3 static cases are skipped on Scala 2.

All 16 shared primitive lookup runtime tests pass on Native Scala 3, 2.13 and 2.12.
The first Scala 2.13 link attempt ran out of disk space; the retry passed without
deleting any files.

| Scala | New JVM runtime tests | New Native primitive runtime tests | Compiler failures |
|---|---|---|---|
| 3.9.0 | 103 pass | 16 pass | 21 of 69 |
| 2.13.18 | 103 pass | 16 pass | 2 of 65 applicable; four static tests skipped |
| 2.12.21 | 103 pass | 16 pass | 2 of 65 applicable; four static tests skipped |

Counts above refer to JUnit methods, not the runner's assumption accounting:
the sbt JUnit reporter counts the four skipped Scala 2 methods again in its total.
