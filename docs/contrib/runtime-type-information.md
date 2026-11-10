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
| `superClass`      | pointer       | RTTI of the parent class, or null                 |
| `lineage`         | pointer[8]    | superclasses from the root, the class, then nulls |
| `dynmap`          | pointer       | reflective proxies, or null                       |
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
| 7    | traits: every implementor holds it in its home itable slot     |
| 8-15 | arrays: number of dimensions                                   |
| 8-31 | traits: low 24 bits of the itable key                          |

The garbage collectors and `java.lang.Class` read these bits instead of
comparing class ids. Only weak references clear their referent today; soft and
phantom references are marked for future support.

## Reference map and arrays

For classes, `refFieldOffsets` points to the offsets, in bytes from the start
of an object, of its reference fields, terminated with -1, followed by the
offsets of raw pointer fields to scan conservatively, terminated with -1. Only
`unsafe.Ptr` has such a field: a boxed pointer can point into the heap, for
example to an array element, and be the only thing keeping it alive.

Arrays have no fields to scan, the GC uses the element scan kind from the flags
instead. In array RTTI the same slot holds the RTTI of the element type, read
by `Class.getComponentType`. Reference arrays are all `ObjectArray` today, with
`java.lang.Object` as element type; one RTTI per array type would only change
this slot and the dimensions in the flags.

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
Tests against classes with more superclasses check `superclassCount` and walk
`superClass` instead. Tests against a class without subclasses compare the RTTI
pointer directly. The technique is known as a Cohen display.

## Traits

The itable of a class is an open-addressing hash table with a power-of-two size.
Each entry is `{trait RTTI, method table}`, a null trait marks an empty slot.
A lookup for trait `T` starts at `(key(T) >>> itableShift) & itableMask`, where
`key(T)` is the 32-bit FNV-1a hash of the mangled trait name, and probes
linearly until it finds `T` or an empty slot. Entries are compared by RTTI address, so traits
with equal keys only make a lookup longer.

Tables are at most half full, so every lookup ends at an empty slot. The
toolchain picks for each class the smallest table, up to four times that size,
and the smallest shift, in which every trait is found on the first probe. Generated code inlines the
whole lookup. When every class of the program holds a trait in the slot where
its lookup starts, calls and instance tests for that trait read only that slot.
`java.lang.Class` runs the same lookup, taking the key from the trait's flags.

## Modules

The instance of each module is stored in its own global, `<Name>$G4slot`,
placed in the `sn_global_objects` section that the garbage collector scans as
roots. Constant modules use a statically allocated instance instead.

## Reflective calls

Reflective proxies are found through a per-class perfect hash map keyed by the
FNV-1a hash of the proxy signature. The toolchain fails the build if two
signatures used by the program share a key.
