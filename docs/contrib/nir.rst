.. _nir:

Native Intermediate Representation
==================================

NIR is high-level object-oriented SSA-based representation. The core of the
representation is a subset of LLVM instructions, types and values, augmented
with a number of high-level primitives that are necessary to
efficiently compiler modern languages like Scala.

.. contents::

Introduction
------------

Lets have a look at the textual form of NIR generated for a simple Scala module:

.. code-block:: scala

    package test

    import System.out.println

    object Test {
      def main(args: Array[String]): Unit =
        println("Hello, world!")
    }

Would map to:

.. code-block:: text

    pin(@test.Test::init) module @test.Test : #java.lang.Object

    def @test.Test::main_class.ssnr.ObjectArray_unit : (module @test.Test, class #scala.scalanative.runtime.ObjectArray) => unit {
      %src.2(%src.0: module @test.Test, %src.1: class #scala.scalanative.runtime.ObjectArray):
        %src.3 = module @java.lang.System
        %src.4 = field[...] %src.3: module @java.lang.System, @java.lang.System::field.out
        %src.5 = load[...] %src.4: ptr
        %src.6 = method[...] %src.5: class #java.io.PrintStream, #java.io.PrintStream::println_class.java.lang.String_unit
        %src.7 = call[...] %src.6: ptr(%src.5: class #java.io.PrintStream, "Hello, world!")
        ret %src.7: unit
    }

    def @test.Test::init : (module @test.Test) => void {
      %src.1(%src.0: module @test.Test):
        %src.2 = call[(class #java.lang.Object) => void] #java.lang.Object::init(%src.0: module @test.Test)
        ret unit
    }

Here we can see a few major points:

1. At its core NIR is very much a classical SSA-based representation.
   The code consists of basic blocks of instructions. Instructions take
   value and type parameters. Control flow instructions can only appear
   as the last instruction of the basic block.

2. Basic blocks have parameters. Parameters directly correspond to phi
   instructions in the classical SSA.

3. The representation is strongly typed. All parameters have corresponding type
   annotations. Instructions may take type arguments (they are ommited
   here for brevity.)

4. Unlike LLVM, it has support for high-level features such as java-like
   classes. Classes may contain methods and fields. There is no overloading
   or access control modifiers so names must be mangled appropriately.

5. All definitions live in a single top-level scope. During compilation they
   are lazily loaded until all reachable definitions have been discovered.
   `pin` and `pin-if` attributes are used to expressed additional dependencies.
   Nesting/ownership is of definitions is expressed through names.

Definitions
-----------

Var
```
.. code-block:: text

    ..$attrs var @name: $type = $value

Corresponds to LLVM's `global <http://llvm.org/docs/LangRef.html#global-variables>`_
when used in the top-level scope and to fields, when used as a member of
classes and modules.

Const
`````
.. code-block:: text

    ..$attrs const @$name: $type = $value

Corresponds to LLVM's `const <http://llvm.org/docs/LangRef.html#global-variables>`_
when used in the top-level scope.

Decl
````
.. code-block:: text

    ..$attrs def @$name: $type

Correspond to LLVM's
`declare <http://llvm.org/docs/LangRef.html#functions>`_
when used on the top-level of the compilation unit and
to abstract methods when used inside classes and traits.

Defn
````
.. code-block:: text

    ..$attrs def @$name: $type { ..$blocks }

Corresponds to LLVM's
`define <http://llvm.org/docs/LangRef.html#functions>`_
when used on the top-level of the compilation unit and
to normal methods when used inside classes, traits and modules.

Struct
``````
.. code-block:: text

    ..$attrs struct @$name { ..$types }

Corresponds to LLVM's
`%$name = type { ... } <http://llvm.org/docs/LangRef.html#structure-types>`_
struct definition.

Trait
`````
.. code-block:: text

    ..$attrs trait @$name : ..$interfaces

Scala-like traits. May contain abstract and concrete methods as members.

Class
`````
.. code-block:: text

    ..$attrs class @$name : $parent, ..$traits

Scala-like classes. May contain vars, abstract and concrete methods as members.

Module
``````
.. code-block:: text

    ..$attrs module @$name : $parent, ..$interfaces

Scala-like modules (i.e. ``object $name``) May contains vars and concrete
methods as members.

Types
-----

Void
````
.. code-block:: text

    void

Corresponds to LLVM's `void <http://llvm.org/docs/LangRef.html#void-type>`_.

Boolean
```````
.. code-block:: text

    bool

Corresponds to LLVM's `i1 <http://llvm.org/docs/LangRef.html#integer-type>`_ and
C's `bool <http://pubs.opengroup.org/onlinepubs/009695399/basedefs/stdbool.h.html>`_.

Integer
```````
.. code-block:: text

    i8
    i16
    i32
    i64

Corresponds to LLVM `integer types <http://llvm.org/docs/LangRef.html#integer-type>`_.

Float
`````
.. code-block:: text

    f32
    f64

Corresponds to LLVM's `floating point types <http://llvm.org/docs/LangRef.html#floating-point-types>`_.

Array
`````
.. code-block:: text

    [$type x N]

Corresponds to LLVM's `aggregate array type <http://llvm.org/docs/LangRef.html#array-type>`_.

Pointer
```````
.. code-block:: text

    ptr

Corresponds to LLVM's `pointer type <http://llvm.org/docs/LangRef.html#pointer-type>`_
with a major distinction of not preserving the type of memory that's being
pointed at. Pointers are going to become untyped in LLVM in near future too,
currently we just always compile them to `i8*`.

Function
````````
.. code-block:: text

    (..$args) => $ret

Corresponds to LLVM's `function type <http://llvm.org/docs/LangRef.html#function-type>`_.

Struct
``````
.. code-block:: text

    struct @$name

Corresponds to LLVM's `aggregate structure type <http://llvm.org/docs/LangRef.html#structure-type>`_.

Unit
````
.. code-block:: text

    unit

Corresponds to ``scala.Unit``.

Nothing
```````
.. code-block:: text

    nothing

Corresponds to ``scala.Nothing``.

Class
`````
.. code-block:: text

    class @$name

A reference to a class instance.

Trait
`````
.. code-block:: text

    trait @$name

A reference to a trait instance.

Module
``````
.. code-block:: text

    module @$name

A reference to a module.

Control-Flow
-------------

unreachable
```````````
.. code-block:: text

   unreachable

If execution reaches undefined instruction the behaviour of execution is undefined
starting from that point. Corresponds to LLVM's
`unreachable <http://llvm.org/docs/LangRef.html#unreachable-instruction>`_.

ret
```
.. code-block:: text

   ret $value

Returns a value. Corresponds to LLVM's
`ret <http://llvm.org/docs/LangRef.html#ret-instruction>`_.

jump
````
.. code-block:: text

   jump $next(..$values)

Jumps to the next basic block with provided values for the parameters.
Corresponds to LLVM's unconditional version of
`br <http://llvm.org/docs/LangRef.html#br-instruction>`_.

if
``
.. code-block:: text

    if $cond then $next1(..$values1) else $next2(..$values2)

Conditionally jumps to one of the basic blocks.
Corresponds to LLVM's conditional form of
`br <http://llvm.org/docs/LangRef.html#br-instruction>`_.

switch
``````
.. code-block:: text

    switch $value {
       case $value1 => $next1(..$values1)
       ...
       default      => $nextN(..$valuesN)
    }

Jumps to one of the basic blocks if `$value` matches corresponding `$valueN`.
Corresponds to LLVM's
`switch <http://llvm.org/docs/LangRef.html#switch-instruction>`_.

invoke
``````
.. code-block:: text

    invoke[$type] $funptr(..$values) to $success unwind $failure

Invoke function pointer, jump to success in case value is returned,
unwind to failure if exception was thrown. Corresponds to LLVM's
`invoke <http://llvm.org/docs/LangRef.html#invoke-instruction>`_.

resume
``````
.. code-block:: text

    resume $excrec

throw
`````
.. code-block:: text

    throw $value

Throws the values and starts unwinding.

try
```
.. code-block:: text

    try $succ catch $failure

Operands
--------

All non-control-flow instructions follow general pattern of
``%N = ..$attrs $op``. The value produced by the instruction may be
omitted if instruction is used purely for side-effect. Operations
follow the pattern of ``$opname[..$types] ..$values``.

call
````
.. code-block:: text

    call[$type] $ptrvalue(..$values)

Calls given function of given function type and argument values.
Corresponds to LLVM's
`call <http://llvm.org/docs/LangRef.html#call-instruction>`_.

load
````
.. code-block:: text

    load[$type] $ptrvalue

Load value of given type from memory.
Corresponds to LLVM's
`load <http://llvm.org/docs/LangRef.html#load-instruction>`_.

store
`````
.. code-block:: text

    store[$type] $ptrvalue, $value

Store value of given type to memory.
Corresponds to LLVM's
`store <http://llvm.org/docs/LangRef.html#store-instruction>`_.

elem
````
.. code-block:: text

    elem[$type] $ptrvalue, ..$indexes

Compute derived pointer starting from given pointer value.
Corresponds to LLVM's
`getelementptr <http://llvm.org/docs/LangRef.html#getelementptr-instruction>`_.

extract
```````
.. code-block:: text

    extract[$type] $aggrvalue, $index

Extract element from aggregate value.
Corresponds to LLVM's
`extractvalue <http://llvm.org/docs/LangRef.html#extractvalue-instruction>`_.

insert
``````
.. code-block:: text

    insert[$type] $aggrvalue, $value, $index

Create a new aggregate value based on existing one with element at index replaced with new value.
Corresponds to LLVM's
`insertvalue <http://llvm.org/docs/LangRef.html#insertvalue-instruction>`_.

alloca
``````
.. code-block:: text

    alloca[$type]

Stack allocate a slot of memory big enough to store given type.
Corresponds to LLVM's
`alloca <http://llvm.org/docs/LangRef.html#alloca-instruction>`_.

bin
```
.. code-block:: text

    $bin[$type] $value1, $value2`

Where ``$bin`` is one of the following:
``add``, ``sub``, ``mul``, ``div``, ``mod``, ``shl``, ``lshr``
``ashr``, ``and``, ``or``, ``xor``. Depending on the type, maps
to either integer or floating point
`binary operation <http://llvm.org/docs/LangRef.html#binary-operations>`_ in LLVM.

comp
````
.. code-block:: text

    $comp[$type] $value1, $value2

Where ``$comp`` is one of the following: ``eq``, ``neq``, ``lt``, ``lte``,
``gt``, ``gte``. Depending on the type, maps to either
`icmp <http://llvm.org/docs/LangRef.html#icmp-instruction>`_ or
`fcmp <http://llvm.org/docs/LangRef.html#fcmp-instruction>`_ with
corresponding comparison flags in LLVM.

conv
````
.. code-block:: text

    $conv[$type] $value

Where ``$conv`` is one of the following: ``trunc``, ``zext``, ``sext``, ``fptrunc``,
``fpext``, ``fptoui``, ``fptosi``, ``uitofp``, ``sitofp``, ``ptrtoint``, ``inttoptr``,
``bitcast``.
Corresponds to LLVM
`conversion instruction <http://llvm.org/docs/LangRef.html#conversion-operations>`_
with the same name.

sizeof
``````
.. code-block:: text

    sizeof[$type]

Returns a size of given type.

classalloc
``````````
.. code-block:: text

    classalloc @$name

Roughly corresponds to ``new $name`` in Scala.
Performs allocation without calling the constructor.

field
`````
.. code-block:: text

    field[$type] $value, @$name

Returns a pointer to the given field of given object.

method
``````
.. code-block:: text

    method[$type] $value, @$name

Returns a pointer to the given method of given object.

as
``
.. code-block:: text

    as[$type] $value

Corresponds to `$value.asInstanceOf[$type]` in Scala.

is
``
.. code-block:: text

    is[$type] $value

Corresponds to `$value.isInstanceOf[$type]` in Scala.

Values
------

Boolean
```````
.. code-block:: text

    true
    false

Corresponds to LLVM's `true` and `false`.

Zero
````
.. code-block:: text

    zero $type

Corresponds to LLVM's `zeroinitializer`.

Integer
```````
.. code-block:: text

    Ni8
    Ni16
    Ni32
    Ni64

Correponds to LLVM's integer values.

Float
`````
.. code-block:: text

    N.Nf32
    N.Nf64

Corresponds to LLVM's floating point values.

Struct
``````
.. code-block:: text

    struct @$name {..$values}`

Corresponds to LLVM's struct values.

Array
`````
.. code-block:: text

    array $ty {..$values}

Corresponds to LLVM's array value.

Local
`````
.. code-block:: text

    %N

Named reference to result of previously executed
instructions or basic block parameters.

Global
``````
.. code-block:: text

    @$name

Reference to the value of top-level definition.

Unit
````
.. code-block:: text

    unit

Corresponds to `()` in Scala.

Null
````
.. code-block:: text

    null

Corresponds to null literal in Scala.

String
``````
.. code-block:: text

    "..."

Corresponds to string literal in Scala.

Attributes
----------

Attributes allow one to attach additional metadata to definitions and instructions.
