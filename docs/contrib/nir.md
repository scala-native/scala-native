# Native Intermediate Representation

NIR is high-level object-oriented SSA-based representation. The core of
the representation is a subset of LLVM instructions, types and values,
augmented with a number of high-level primitives that are necessary to
efficiently compile modern languages like Scala.

::: contents
:::

## Introduction

Lets have a look at the textual form of NIR generated for a simple Scala
module:

``` scala
object Test {
  def main(args: Array[String]): Unit =
    println("Hello, world!")
}
```

Would map to:

``` text
pin(@Test$::init) module @Test$ : @java.lang.Object

def @Test$::main_class.ssnr.ObjectArray_unit : (module @Test$, class @scala.scalanative.runtime.ObjectArray) => unit {
  %src.2(%src.0 : module @Test$, %src.1 : class @scala.scalanative.runtime.ObjectArray):
    %src.3 = module @scala.Predef$
    %src.4 = method %src.3 : module @scala.Predef$, @scala.Predef$::println_class.java.lang.Object_unit
    %src.5 = call[(module @scala.Predef$, class @java.lang.Object) => unit] %src.4 : ptr(%src.3 : module @scala.Predef$, "Hello, world!")
    ret %src.5 : unit
}

def @Test$::init : (module @Test$) => unit {
  %src.1(%src.0 : module @Test$):
    %src.2 = call[(class @java.lang.Object) => unit] @java.lang.Object::init : ptr(%src.0 : module @Test$)
    ret unit
}
```

Here we can see a few distinctive features of the representation:

1.  At its core NIR is very much a classical SSA-based representation.
    The code consists of basic blocks of instructions. Instructions take
    value and type parameters. Control flow instructions can only appear
    as the last instruction of the basic block.
2.  Basic blocks have parameters. Parameters directly correspond to phi
    instructions in the classical SSA.
3.  The representation is strongly typed. All parameters have explicit
    type annotations. Instructions may be overloaded for different types
    via type parameters.
4.  Unlike LLVM, it has support for high-level object-oriented features
    such as garbage-collected classes, traits and modules. They may
    contain methods and fields. There is no overloading or access
    control modifiers so names must be mangled appropriately.
5.  All definitions live in a single top-level scope indexed by globally
    unique names. During compilation they are lazily loaded until all
    reachable definitions have been discovered.

## Definitions

### Var

``` text
..$attrs var @$name: $ty = $value
```

Corresponds to LLVM\'s [global
variables](http://llvm.org/docs/LangRef.html#global-variables) when used
in the top-level scope and to fields, when used as a member of classes
and modules.

### Const

``` text
..$attrs const @$name: $type = $value
```

Corresponds to LLVM\'s [global
constant](http://llvm.org/docs/LangRef.html#global-variables). Constants
may only reside on the top-level and can not be members of classes and
modules.

### Declare

``` text
..$attrs def @$name: $type
```

Correspond to LLVM\'s
[declare](http://llvm.org/docs/LangRef.html#functions) when used on the
top-level of the compilation unit and to abstract methods when used
inside classes and traits.

### Define

``` text
..$attrs def @$name: $type { ..$blocks }
```

Corresponds to LLVM\'s
[define](http://llvm.org/docs/LangRef.html#functions) when used on the
top-level of the compilation unit and to normal methods when used inside
classes, traits and modules.

### Struct

``` text
..$attrs struct @$name { ..$types }
```

Corresponds to LLVM\'s [named
struct](http://llvm.org/docs/LangRef.html#structure-types).

### Trait

``` text
..$attrs trait @$name : ..$traits
```

Scala-like traits. May contain abstract and concrete methods as members.

### Class

``` text
..$attrs class @$name : $parent, ..$traits
```

Scala-like classes. May contain vars, abstract and concrete methods as
members.

### Module

``` text
..$attrs module @$name : $parent, ..$traits
```

Scala-like modules (i.e. `object $name`) May only contain vars and
concrete methods as members.

## Types

### Void

``` text
void
```

Corresponds to LLVM\'s
[void](http://llvm.org/docs/LangRef.html#void-type).

### Vararg

``` text
...
```

Corresponds to LLVM\'s
[varargs](http://www.llvm.org/docs/LangRef.html#function-type). May only
be nested inside function types.

### Pointer

``` text
ptr
```

Corresponds to LLVM\'s [pointer
type](http://llvm.org/docs/LangRef.html#pointer-type) with a major
distinction of not preserving the type of memory that\'s being pointed
at. Pointers are going to become untyped in LLVM in near future too.

### Boolean

``` text
bool
```

Corresponds to LLVM\'s
[i1](http://llvm.org/docs/LangRef.html#integer-type).

### Integer

``` text
i8
i16
i32
i64
```

Corresponds to LLVM [integer
types](http://llvm.org/docs/LangRef.html#integer-type). Unlike LLVM we
do not support arbitrary width integer types at the moment.

### Float

``` text
f32
f64
```

Corresponds to LLVM\'s [floating point
types](http://llvm.org/docs/LangRef.html#floating-point-types).

### Array

``` text
[$type x N]
```

Corresponds to LLVM\'s [aggregate array
type](http://llvm.org/docs/LangRef.html#array-type).

### Function

``` text
(..$args) => $ret
```

Corresponds to LLVM\'s [function
type](http://llvm.org/docs/LangRef.html#function-type).

### Struct

``` text
struct @$name
struct { ..$types }
```

Has two forms: named and anonymous. Corresponds to LLVM\'s [aggregate
structure type](http://www.llvm.org/docs/LangRef.html#t-struct).

### Unit

``` text
unit
```

A reference type that corresponds to `scala.Unit`.

### Nothing

``` text
nothing
```

Corresponds to `scala.Nothing`. May only be used a function return type.

### Class

``` text
class @$name
```

A reference to a class instance.

### Trait

``` text
trait @$name
```

A reference to a trait instance.

### Module

``` text
module @$name
```

A reference to a module.

## Control-Flow

### unreachable

``` text
unreachable
```

If execution reaches undefined instruction the behaviour of execution is
undefined starting from that point. Corresponds to LLVM\'s
[unreachable](http://llvm.org/docs/LangRef.html#unreachable-instruction).

### ret

``` text
ret $value
```

Returns a value. Corresponds to LLVM\'s
[ret](http://llvm.org/docs/LangRef.html#ret-instruction).

### jump

``` text
jump $next(..$values)
```

Jumps to the next basic block with provided values for the parameters.
Corresponds to LLVM\'s unconditional version of
[br](http://llvm.org/docs/LangRef.html#br-instruction).

### if

``` text
if $cond then $next1(..$values1) else $next2(..$values2)
```

Conditionally jumps to one of the basic blocks. Corresponds to LLVM\'s
conditional form of
[br](http://llvm.org/docs/LangRef.html#br-instruction).

### switch

``` text
switch $value {
   case $value1 => $next1(..$values1)
   ...
   default      => $nextN(..$valuesN)
}
```

Jumps to one of the basic blocks if `$value` is equal to corresponding
`$valueN`. Corresponds to LLVM\'s
[switch](http://llvm.org/docs/LangRef.html#switch-instruction).

### invoke

``` text
invoke[$type] $ptr(..$values) to $success unwind $failure
```

Invoke function pointer, jump to success in case value is returned,
unwind to failure if exception was thrown. Corresponds to LLVM\'s
[invoke](http://llvm.org/docs/LangRef.html#invoke-instruction).

### throw

``` text
throw $value
```

Throws the values and starts unwinding.

### try

``` text
try $succ catch $failure
```

## Operands

All non-control-flow instructions follow a general pattern of
`%$name = $opname[..$types] ..$values`. Purely side-effecting operands
like `store` produce `unit` value.

### call

``` text
call[$type] $ptr(..$values)
```

Calls given function of given function type and argument values.
Corresponds to LLVM\'s
[call](http://llvm.org/docs/LangRef.html#call-instruction).

### load

``` text
load[$type] $ptr
```

Load value of given type from memory. Corresponds to LLVM\'s
[load](http://llvm.org/docs/LangRef.html#load-instruction).

### store

``` text
store[$type] $ptr, $value
```

Store value of given type to memory. Corresponds to LLVM\'s
[store](http://llvm.org/docs/LangRef.html#store-instruction).

### elem

``` text
elem[$type] $ptr, ..$indexes
```

Compute derived pointer starting from given pointer. Corresponds to
LLVM\'s
[getelementptr](http://llvm.org/docs/LangRef.html#getelementptr-instruction).

### extract

``` text
extract[$type] $aggrvalue, $index
```

Extract element from aggregate value. Corresponds to LLVM\'s
[extractvalue](http://llvm.org/docs/LangRef.html#extractvalue-instruction).

### insert

``` text
insert[$type] $aggrvalue, $value, $index
```

Create a new aggregate value based on existing one with element at index
replaced with new value. Corresponds to LLVM\'s
[insertvalue](http://llvm.org/docs/LangRef.html#insertvalue-instruction).

### stackalloc

``` text
stackalloc[$type]()
```

Stack allocate a slot of memory big enough to store given type.
Corresponds to LLVM\'s
[alloca](http://llvm.org/docs/LangRef.html#alloca-instruction).

### bin

``` text
$bin[$type] $value1, $value2`
```

Where `$bin` is one of the following: `iadd`, `fadd`, `isub`, `fsub`,
`imul`, `fmul`, `sdiv`, `udiv`, `fdiv`, `srem`, `urem`, `frem`, `shl`,
`lshr`, `ashr` , `and`, `or`, `xor`. Depending on the type and
signedness, maps to either integer or floating point [binary
operations](http://llvm.org/docs/LangRef.html#binary-operations) in
LLVM.

### comp

``` text
$comp[$type] $value1, $value2
```

Where `$comp` is one of the following: `eq`, `neq`, `lt`, `lte`, `gt`,
`gte`. Depending on the type, maps to either
[icmp](http://llvm.org/docs/LangRef.html#icmp-instruction) or
[fcmp](http://llvm.org/docs/LangRef.html#fcmp-instruction) with
corresponding comparison flags in LLVM.

### conv

``` text
$conv[$type] $value
```

Where `$conv` is one of the following: `trunc`, `zext`, `sext`,
`fptrunc`, `fpext`, `fptoui`, `fptosi`, `uitofp`, `sitofp`, `ptrtoint`,
`inttoptr`, `bitcast`. Corresponds to LLVM [conversion
instructions](http://llvm.org/docs/LangRef.html#conversion-operations)
with the same name.

### sizeof

``` text
sizeof[$type]
```

Returns a size of given type.

### classalloc

``` text
classalloc @$name
```

Roughly corresponds to `new $name` in Scala. Performs allocation without
calling the constructor.

### field

``` text
field[$type] $value, @$name
```

Returns a pointer to the given field of given object.

### method

``` text
method[$type] $value, @$name
```

Returns a pointer to the given method of given object.

### dynmethod

``` text
dynmethod $obj, $signature
```

Returns a pointer to the given method of given object and signature.

### as

``` text
as[$type] $value
```

Corresponds to `$value.asInstanceOf[$type]` in Scala.

### is

``` text
is[$type] $value
```

Corresponds to `$value.isInstanceOf[$type]` in Scala.

## Values

### Boolean

``` text
true
false
```

Corresponds to LLVM\'s `true` and `false`.

### Zero and null

``` text
null
zero $type
```

Corresponds to LLVM\'s `null` and `zeroinitializer`.

### Integer

``` text
Ni8
Ni16
Ni32
Ni64
```

Correponds to LLVM\'s integer values.

### Float

``` text
N.Nf32
N.Nf64
```

Corresponds to LLVM\'s floating point values.

### Struct

``` text
struct @$name {..$values}`
```

Corresponds to LLVM\'s struct values.

### Array

``` text
array $ty {..$values}
```

Corresponds to LLVM\'s array value.

### Local

``` text
%$name
```

Named reference to result of previously executed instructions or basic
block parameters.

### Global

``` text
@$name
```

Reference to the value of top-level definition.

### Unit

``` text
unit
```

Corresponds to `()` in Scala.

### Null

``` text
null
```

Corresponds to null literal in Scala.

### String

``` text
"..."
```

Corresponds to string literal in Scala.

## Attributes

Attributes allow one to attach additional metadata to definitions and
instructions.

### Inlining

#### mayinline

``` text
mayinline
```

Default state: optimiser is allowed to inline given method.

#### inlinehint

``` text
inlinehint
```

Optimiser is incentivized to inline given methods but it is allowed not
to.

#### noinline

``` text
noinline
```

Optimiser must never inline given method.

#### alwaysinline

``` text
alwaysinline
```

Optimiser must always inline given method.

### Linking

#### link

``` text
link($name)
```

Automatically put `$name` on a list of native libraries to link with if
the given definition is reachable.

#### pin

``` text
pin(@$name)
```

Require `$name` to be reachable, whenever current definition is
reachable. Used to introduce indirect linking dependencies. For example,
module definitions depend on its constructors using this attribute.

#### pin-if

``` text
pin-if(@$name, @$cond)
```

Require `$name` to be reachable if current and `$cond` definitions are
both reachable. Used to introduce conditional indirect linking
dependencies. For example, class constructors conditionally depend on
methods overridden in given class if the method that are being
overridden are reachable.

#### pin-weak

``` text
pin-weak(@$name)
```

Require `$name` to be reachable if there is a reachable dynmethod with
matching signature.

#### stub

``` text
stub
```

Indicates that the annotated method, class or module is only a stub
without implementation. If the linker is configured with
`linkStubs = false`, then these definitions will be ignored and a
linking error will be reported. If `linkStubs = true`, these definitions
will be linked.

### Misc

#### dyn

``` text
dyn
```

Indication that a method can be called using a structural type dispatch.

#### pure

``` text
pure
```

Let optimiser assume that calls to given method are effectively pure.
Meaning that if the same method is called twice with exactly the same
argument values, it can re-use the result of first invocation without
calling the method twice.

#### extern

``` text
extern
```

Use C-friendly calling convention and don\'t name-mangle given method.

#### override

``` text
override(@$name)
```

Attributed method overrides `@$name` method if `@$name` is reachable.
`$name` must be defined in one of the super classes or traits of the
parent class.
