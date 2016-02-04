# Native Compiler (NC)

The primary role of the native compiler is to translate all high-level features from
NIR to a combination of low-level features. After that emission of LLVM code is
trivial, as all low-level constructs map cleanly on corresponding LLVM primitives.

## Assumptions

The compiler assumes the following:

1. There is no dynamic code loading of any kind.
   Definitions that are not reachable may be dropped from the resulting LLVM IR.
   Methods which have not been overriden in the reachable set of classes are effectively final.

1. Resulting LLVM module is going to be linked with Native Runtime.

## Lowering passes

1. **Array lowering**. Translates arrays and operations on them.

1. **Interface lowering**. Translates interfaces and calls on interface methods.

1. **Module lowering**. Translates modules to top-level lazy vals with corresponding
   backing module class and accessor.

1. **Intrinsic lowering**. Maps intrinsic references to corresponding runtime implementation.

1. **Void lowering**. Translates away `unit` and `nothing` types to `void`.

1. **Object lowering**. Translates user-defined, built-in classes and `null` types.

1. **Size lowering**. Translates `size` operation and `size` type.

1. **Throw lowering**. Translates away `throw` instruction.

