# Runtime type information

Every class and trait has a runtime type information (RTTI) structure, emitted
by the toolchain as `<Name>$G4type` into the `sn_classinfo` section. At runtime
it is the `java.lang.Class` instance of the type (`scala.scalanative.runtime._Class`).
Each object starts with a pointer to the RTTI of its class.

The contents of an RTTI structure depend only on the type and its ancestors,
never on other classes of the program. Adding a class or a trait does not
change the RTTI of unrelated types, nor the code that tests against them.

## Layout

Fields shared by classes and traits:

| Field             | Type    | Contents                                         |
|-------------------|---------|--------------------------------------------------|
| `class`           | pointer | RTTI of `java.lang.Class`                        |
| `lockWord`        | pointer | object monitor, only with multithreading enabled |
| `flags`           | `int`   | kind of the type, see below                      |
| `interfacesCount` | `int`   | number of implemented traits                     |
| `interfaces`      | pointer | RTTI of every implemented trait, sorted by name  |
| `name`            | pointer | `java.lang.String` with the type name            |

Fields of classes only:

| Field             | Type          | Contents                                          |
|-------------------|---------------|---------------------------------------------------|
| `size`            | `int`         | instance size in bytes                            |
| `superclassCount` | `int`         | number of superclasses, 0 for root classes        |
| `refFieldOffsets` | pointer       | classes: reference map, see below; arrays: element type RTTI |
| `itableMask`      | `int`         | itable size minus one                             |
| `itableShift`     | `int`         | selects the trait key bits that index the itable  |
| `itables`         | pointer       | itable entries, see below                         |
| `lineage`         | pointer[9]    | superclasses from the root, the class, then nulls; last: next chunk |
| `vtable`          | pointer[]     | virtual method table                              |

The layout is defined by `codegen.CommonMemoryLayouts` and mirrored by
`runtime._Class` and `ObjectHeader.h`, which must be kept in sync.

## Flags

| Bits | Meaning                                                        |
|------|----------------------------------------------------------------|
| 0    | trait                                                          |
| 1    | primitive type                                                 |
| 2    | `runtime.Array` or one of its subclasses                       |
| 3-4  | arrays: element scan, 0 none, 1 references, 2 conservative     |
| 5-6  | reference kind: 0 none, 1 weak, 2 soft, 3 phantom              |
| 7    | classes: the GC calls the object tracer for marked instances   |
| 8-15 | arrays: number of dimensions                                   |
| 16-31 | classes: hash of the class name, for reflective calls         |
| 8-31 | traits: low 24 bits of the itable key                          |

The garbage collectors and `java.lang.Class` read these bits instead of
comparing class ids. Immix and Commix clear weak and phantom referents once
nothing else marks them. Soft referents stay alive, except when an allocation
fails after a collection that found soft references: the collector then runs
once more with soft referents cleared like weak ones, and retries the
allocation. The javalib enqueues cleared references on their queue. Boehm
keeps weak and phantom referents as hidden pointers and soft ones as strong.

## Reference map and arrays

For classes, `refFieldOffsets` points to the offsets, in bytes from the start
of an object, of its reference fields, terminated with -1, followed by the
offsets of raw pointer fields to scan conservatively, terminated with -1. Only
`unsafe.Ptr` has such a field: a boxed pointer can point into the heap, for
example to an array element, and be the only thing keeping it alive.

The referent of `java.lang.ref.Reference` is its first field, so it directly
follows the object header in every reference object, like the fields of
`String` and `Class`; the toolchain fails the build otherwise. It is left out
of the reference map; the collectors mark soft referents themselves.

Arrays have no fields to scan, the GC uses the element scan kind from the flags
instead. In array RTTI the same slot holds the RTTI of the element type, read
by `Class.getComponentType`.

## Array classes

Every array type has one class, named as on the JVM: `[I`, `[Ljava.lang.Object;`,
`[[Ljava.lang.String;`.

- Arrays of primitives are instances of `IntArray` and the like, arrays of
  `Object` of `ObjectArray`.
- Any other array type used by the program, through the type of a value, an
  allocation, a `classOf` or a type test, gets a class from the linker: a
  subclass of `ObjectArray` with no members, named by the JVM binary name of
  the array type. Its RTTI holds the component class and the number of
  dimensions, and it inherits `ObjectArray`'s methods, so the code of arrays of
  references is shared.
- `runtime.ArrayClasses` finds the class of arrays of a component for
  reflection (`java.lang.reflect.Array.newInstance`, `Class.arrayType`,
  `Class.forName`) among the classes of the class list. Array types the linker
  never saw get a class on first use: a copy of `ObjectArray`'s RTTI, with its
  own name, component and dimensions, allocated outside the heap and never
  freed. Its lineage and class key are `ObjectArray`'s, so type tests against
  `ObjectArray`, virtual and reflective calls work as for any array of
  references. Its name and lock word are GC roots.

Arrays of references are covariant (`String[]` is an `Object[]`), unlike their
classes. Type tests against an array type whose element has no subtypes
(`String[]`, `int[][]`) compare the class. Against other arrays of references
they test for an array of references, then test its component class against
the element as any class test; a component that is a trait, whose RTTI has no
lineage nor itable, goes to `ArrayClasses.isSubclass`. Poly-inlined calls test
classes of arrays last: classes created at runtime match no test and share
`ObjectArray`'s methods. With `SemanticsConfig.strictArrayTypeChecks` disabled
these tests only check that the value is an array of references, as before
0.5.14. Reflection checks element types either way.

## Class list

Every class and trait also has `<Name>$G9classlist`, a pointer to its RTTI in
the `sn_classlist` section. `Class.forName` iterates that section.

## Class instance tests

The lineage of a class lists its superclasses starting from the root, then the
class itself, so `lineage[superclassCount]` is the class. An object is an
instance of class `C` exactly when `C` appears in the lineage of its class at
index `superclassCount(C)`, so for `superclassCount(C) < 8`:

```c
obj->rtti->lineage[superclassCount(C)] == &C$G4type
```

`superclassCount(C)` and the address of `C`'s RTTI are compile-time constants.
Tests against a class without subclasses compare the RTTI pointer directly. The
technique is known as a Cohen display.

The ninth slot of `lineage` continues the lineage of classes with 8 or more
superclasses: it points to a chunk of the same shape, 8 entries padded with
nulls and a pointer to the next chunk, and is null for other classes. A test
against such a deep class follows `superclassCount(C) / 8` of these pointers,
answering false at a null one, and compares entry `superclassCount(C) % 8` of
the chunk it reaches. `getSuperclass` reads the lineage the same way.

## Traits

The itable of a class is an open-addressing hash table with a power-of-two size.
Each entry is `{trait RTTI, method table}`, a null trait marks an empty slot.
A lookup for trait `T` starts at `(key(T) >>> itableShift) & itableMask`, where
`key(T)` is the 32-bit FNV-1a hash of the mangled trait name, and probes
linearly until it finds `T` or an empty slot. Entries are compared by RTTI address, so traits
with equal keys only make a lookup longer.

Tables are at most half full, so every lookup ends at an empty slot: the
smallest table of a class with `n` traits has the first power of two of at
least `2n` slots. The toolchain picks for each class the smallest table, up to
4 times that size, and the smallest shift, in which every trait is found on
the first probe. Failing that, it picks the smallest table, up to 64 times that
size, and shift that keep every trait in the slot where its lookup starts or in
the next one. No table does for three traits whose keys are equal in the bits
itables use: the class then gets the smallest table, without a shift, and
traits can end up further from the slot where their lookup starts.

Generated code relies on three facts about the whole program:

- When every class holds a trait in the slot where its lookup starts, calls and
  instance tests for that trait read only that slot.
- When every class holds it in that slot or the next one, they probe both
  inline. A call takes the next slot when the first one holds another trait;
  an instance test compares one more entry.
- When no class shifts the key of a trait, lookups skip loading the shift.

For any other trait, a lookup that finds another trait in the first slot
continues in `scalanative_itable_find`. These facts only shape generated code:
RTTI does not record them. `java.lang.Class` takes the key from the trait's
flags, probes the first slot inline and continues out of line past any other
trait.

## Modules

The instance of each module is stored in its own global, `<Name>$G4slot`,
placed in the `sn_global_objects` section that the garbage collector scans as
roots. Constant modules use a statically allocated instance instead.

## Reflective calls

Reflective calls, from structural types, look up proxies of the called method
in one table per program, keyed by class RTTI and a hash of the proxy
signature. Each class in the table lists its own proxies and the inherited
ones. The program registers the table at startup, so class RTTI and the code
calling reflectively do not depend on which reflective calls the program
makes. A lookup starts at a slot derived from both hashes, the class hash being
stored in its flags, and compares RTTI addresses. The build fails if two
signatures used by the program share a key.
