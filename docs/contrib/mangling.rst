Name mangling
-------------

Scala Native toolchain mangles names for all definitions except
the ones which have been explicitly exported to C using
``extern``. Mangling scheme is defined through a simple grammar
that uses a notation inspired by
`Itanium ABI <http://refspecs.linuxbase.org/cxxabi-1.83.html>`_::

    <mangled-name> ::=
        _S <defn-name>

    <defn-name> ::=
        T <name>                       // top-level name
        M <name> <sig-name>            // member name

    <sig-name> ::=
        F <name>                       // field name
        R <type-name>+ E               // constructor name
        D <name> <type-name>+ E        // method name
        P <name> <type-name>+ E        // proxy name
        C <name>                       // c extern name
        G <name>                       // generated name
        K <sig-name> <type-name>+ E    // duplicate name

    <type-name> ::=
        v                              // c vararg
        R _                            // c pointer type-name
        R <type-name>+ E               // c function type-name
        S <type-name>+ E               // c anonymous struct type-name
        A <type-name> <number> _       // c array type-name
        <integer-type-name>            // signed integer type-name
        z                              // scala.Boolean
        c                              // scala.Char
        f                              // scala.Float
        d                              // scala.Double
        u                              // scala.Unit
        l                              // scala.Null
        n                              // scala.Nothing
        L <nullable-type-name>         // nullable type-name
        A <type-name> _                // nonnull array type-name
        X <name>                       // nonnull exact class type-name
        <name>                         // nonnull class type-name

    <nullable-type-name> ::=
        A <type-name> _                // nullable array type-name
        X <name>                       // nullable exact class type-name
        <name>                         // nullable class type-name

    <integer-type-name> ::=
        b                              // scala.Byte
        s                              // scala.Short
        i                              // scala.Int
        j                              // scala.Long

    <name> ::=
        <length number> <chars>        // raw identifier of given length
