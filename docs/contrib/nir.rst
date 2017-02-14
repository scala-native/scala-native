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

    object Test {
      def main(args: Array[String]): Unit =
        println("Hello, world!")
    }

Would map to:

.. code-block:: text

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

Here we can see a few distinctive features of the representation:

1. At its core NIR is very much a classical SSA-based representation.
   The code consists of basic blocks of instructions. Instructions take
   value and type parameters. Control flow instructions can only appear
   as the last instruction of the basic block.

2. Basic blocks have parameters. Parameters directly correspond to phi
   instructions in the classical SSA.

3. The representation is strongly typed. All parameters have explicit type
   annotations. Instructions may be overloaded for different types via type
   parameters.

4. Unlike LLVM, it has support for high-level object-oriented features such as
   garbage-collected classes, traits and modules. They may contain methods and
   fields. There is no overloading or access control modifiers so names must be
   mangled appropriately.

5. All definitions live in a single top-level scope indexed by globally
   unique names. During compilation they are lazily loaded until all
   reachable definitions have been discovered. `pin` and `pin-if` attributes
   are used to express additional dependencies.

Definitions
-----------

Var
```
.. code-block:: text

    ..$attrs var @$name: $ty = $value

Corresponds to LLVM's `global variables <http://llvm.org/docs/LangRef.html#global-variables>`_
when used in the top-level scope and to fields, when used as a member of
classes and modules.

Const
`````
.. code-block:: text

    ..$attrs const @$name: $type = $value

Corresponds to LLVM's `global constant <http://llvm.org/docs/LangRef.html#global-variables>`_.
Constants may only reside on the top-level and can not be members of classes and
modules.

Declare
````````
.. code-block:: text

    ..$attrs def @$name: $type

Correspond to LLVM's
`declare <http://llvm.org/docs/LangRef.html#functions>`_
when used on the top-level of the compilation unit and
to abstract methods when used inside classes and traits.

Define
``````
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
`named struct <http://llvm.org/docs/LangRef.html#structure-types>`_.

Trait
`````
.. code-block:: text

    ..$attrs trait @$name : ..$traits

Scala-like traits. May contain abstract and concrete methods as members.

Class
`````
.. code-block:: text

    ..$attrs class @$name : $parent, ..$traits

Scala-like classes. May contain vars, abstract and concrete methods as members.

Module
``````
.. code-block:: text

    ..$attrs module @$name : $parent, ..$traits

Scala-like modules (i.e. ``object $name``) May only contain vars and concrete
methods as members.

Types
-----

Void
````
.. code-block:: text

    void

Corresponds to LLVM's `void <http://llvm.org/docs/LangRef.html#void-type>`_.

Vararg
``````
.. code-block:: text

    ...

Corresponds to LLVM's `varargs <http://www.llvm.org/docs/LangRef.html#function-type>`_.
May only be nested inside function types.

Pointer
```````
.. code-block:: text

    ptr

Corresponds to LLVM's `pointer type <http://llvm.org/docs/LangRef.html#pointer-type>`_
with a major distinction of not preserving the type of memory that's being
pointed at. Pointers are going to become untyped in LLVM in near future too.

Boolean
```````
.. code-block:: text

    bool

Corresponds to LLVM's `i1 <http://llvm.org/docs/LangRef.html#integer-type>`_.

Integer
```````
.. code-block:: text

    i8
    i16
    i32
    i64

Corresponds to LLVM `integer types <http://llvm.org/docs/LangRef.html#integer-type>`_.
Unlike LLVM we do not support arbitrary width integer types at the moment.

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

Function
````````
.. code-block:: text

    (..$args) => $ret

Corresponds to LLVM's `function type <http://llvm.org/docs/LangRef.html#function-type>`_.

Struct
``````
.. code-block:: text

    struct @$name
    struct { ..$types }

Has two forms: named and anonymous. Corresponds to LLVM's
`aggregate structure type <http://www.llvm.org/docs/LangRef.html#t-struct>`_.

Unit
````
.. code-block:: text

    unit

A reference type that corresponds to ``scala.Unit``.

Nothing
```````
.. code-block:: text

    nothing

Corresponds to ``scala.Nothing``. May only be used a function return type.

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

Jumps to one of the basic blocks if ``$value`` is equal to
corresponding ``$valueN``. Corresponds to LLVM's
`switch <http://llvm.org/docs/LangRef.html#switch-instruction>`_.

invoke
``````
.. code-block:: text

    invoke[$type] $ptr(..$values) to $success unwind $failure

Invoke function pointer, jump to success in case value is returned,
unwind to failure if exception was thrown. Corresponds to LLVM's
`invoke <http://llvm.org/docs/LangRef.html#invoke-instruction>`_.

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

All non-control-flow instructions follow a general pattern of
``%$name = $opname[..$types] ..$values``. Purely side-effecting operands
like ``store`` produce ``unit`` value.

call
````
.. code-block:: text

    call[$type] $ptr(..$values)

Calls given function of given function type and argument values.
Corresponds to LLVM's
`call <http://llvm.org/docs/LangRef.html#call-instruction>`_.

load
````
.. code-block:: text

    load[$type] $ptr

Load value of given type from memory. Corresponds to LLVM's
`load <http://llvm.org/docs/LangRef.html#load-instruction>`_.

store
`````
.. code-block:: text

    store[$type] $ptr, $value

Store value of given type to memory. Corresponds to LLVM's
`store <http://llvm.org/docs/LangRef.html#store-instruction>`_.

elem
````
.. code-block:: text

    elem[$type] $ptr, ..$indexes

Compute derived pointer starting from given pointer. Corresponds to LLVM's
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

Create a new aggregate value based on existing one with element at index
replaced with new value. Corresponds to LLVM's
`insertvalue <http://llvm.org/docs/LangRef.html#insertvalue-instruction>`_.

stackalloc
``````````
.. code-block:: text

    stackalloc[$type]

Stack allocate a slot of memory big enough to store given type.
Corresponds to LLVM's
`alloca <http://llvm.org/docs/LangRef.html#alloca-instruction>`_.

bin
```
.. code-block:: text

    $bin[$type] $value1, $value2`


Where ``$bin`` is one of the following:
``iadd``, ``fadd``, ``isub``, ``fsub``, ``imul``, ``fmul``,
``sdiv``, ``udiv``, ``fdiv``, ``srem``, ``urem``, ``frem``,
``shl``, ``lshr``, ``ashr`` , ``and``, ``or``, ``xor``.
Depending on the type and signedness, maps to either integer or floating point
`binary operations <http://llvm.org/docs/LangRef.html#binary-operations>`_ in LLVM.

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
`conversion instructions <http://llvm.org/docs/LangRef.html#conversion-operations>`_
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

dynmethod
`````````
.. code-block:: text

    dynmethod $obj, $signature

Returns a pointer to the given method of given object and signature.

as
``
.. code-block:: text

    as[$type] $value

Corresponds to ``$value.asInstanceOf[$type]`` in Scala.

is
``
.. code-block:: text

    is[$type] $value

Corresponds to ``$value.isInstanceOf[$type]`` in Scala.

Values
------

Boolean
```````
.. code-block:: text

    true
    false

Corresponds to LLVM's ``true`` and ``false``.

Zero
````
.. code-block:: text

    zero $type

Corresponds to LLVM's ``zeroinitializer``.

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

    %$name

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

Corresponds to ``()`` in Scala.

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

Inlining
````````

mayinline
*********
.. code-block:: text

    mayinline

Default state: optimiser is allowed to inline given method.

inlinehint
**********
.. code-block:: text

    inlinehint

Optimiser is incentivized to inline given methods but is it allowed not to.

noinline
********
.. code-block:: text

    noinline

Optimiser must never inline given method.

alwaysinline
************
.. code-block:: text

    alwaysinline

Optimiser must always inline given method.

Linking
```````

link
****
.. code-block:: text

    link($name)

Automatically put ``$name`` on a list of native libraries to link with if the
given definition is reachable.

pin
***
.. code-block:: text

    pin(@$name)

Require ``$name`` to be reachable, whenever current definition is reachable.
Used to introduce indirect linking dependencies. For example, module definitions
depend on its constructors using this attribute.

pin-if
******
.. code-block:: text

    pin-if(@$name, @$cond)

Require ``$name`` to be reachable if current and ``$cond`` definitions are
both reachable. Used to introduce conditional indirect linking dependencies.
For example, class constructors conditionally depend on methods overriden in
given class if the method that are being overriden are reachable.

pin-weak
********
.. code-block:: text

    pin-weak(@$name)

Require ``$name`` to be reachable if there is a reachable dynmethod with matching signature.

Misc
````

dyn
***
.. code-block:: text

    dyn

Indication that a method can be called using a structural type dispatch.

pure
****
.. code-block:: text

    pure

Let optimiser assume that calls to given method are effectively pure.
Meaning that if the same method is called twice with exactly the same argument
values, it can re-use the result of first invocation without calling the method
twice.

extern
******
.. code-block:: text

    extern

Use C-friendly calling convention and don't name-mangle given method.

override
********
.. code-block:: text

    override(@$name)

Attributed method overrides ``@$name`` method if ``@$name`` is reachable.
``$name`` must be defined in one of the super classes or traits of
the parent class.

