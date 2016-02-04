# Intrinsics

Intrinsics provide a set of runtime-implemented functions.
Unlike regular FFI external definitions compiler knows about
semantics of these functions and  might optimise them based on
their invariants (e.g. eliminate redundant boxing,
string conversions etc)

* **Primitive boxing**:

   Name         | Signature            | Description
  --------------|----------------------|------------------------------
  `#bool_box`   | `(bool)  => boolean` | `j.l.Boolean#valueOf`
  `#char_box`   | `(i16) => character` | `j.l.Character#valueOf`
  `#byte_box`   | `(i8)  => byte`      | `j.l.Byte#valueOf`
  `#short_box`  | `(i16) => short`     | `j.l.Short#valueOf`
  `#int_box`    | `(i32) => integer`   | `j.l.Integer#valueOf`
  `#long_box`   | `(i64) => long`      | `j.l.Long#valueOf`
  `#float_box`  | `(f32) => float`     | `j.l.Float#valueOf`
  `#double_box` | `(f64) => double`    | `j.l.Double#valueOf`

* **Primitive unboxing**:

   Name           | Signature            | Description
  ----------------|----------------------|----------------------------------
  `#bool_unbox`   | `(bool) => i1`       | `j.l.Boolean#booleanValue`
  `#char_unbox`   | `(character) => i16` | `j.l.Character#charValue`
  `#byte_unbox`   | `(byte) => i8`       | `j.l.Byte#byteValue`
  `#short_unbox`  | `(boolean) => i16`   | `j.l.Short#shortValue`
  `#int_unbox`    | `(integer) => i32`   | `j.l.Integer#intValue`
  `#long_unbox`   | `(long) => i64`      | `j.l.Long#longValue`
  `#float_unbox`  | `(float) => f32`     | `j.l.Float#floatValue`
  `#double_unbox` | `(double) => f64`    | `j.l.Double#doubleValue`

* **Primitive to string**:

   Name               | Signature         | Description
  --------------------|-------------------|-------------------------------
  `#bool_to_string`   | `(i1)  => string` | `j.l.Boolean#toString`
  `#char_to_string`   | `(i16) => string` | `j.l.Character#toString`
  `#byte_to_string`   | `(i8)  => string` | `j.l.Byte#toString`
  `#short_to_string`  | `(i16) => string` | `j.l.Short#toString`
  `#int_to_string`    | `(i32) => string` | `j.l.Integer#toString`
  `#long_to_string`   | `(i64) => string` | `j.l.Long#toString`
  `#float_to_string`  | `(f32) => string` | `j.l.Float#toString`
  `#double_to_string` | `(f64) => string` | `j.l.Double#toString`

* **Primitive from string**:

   Name          | Signature              | Description
  ---------------|------------------------|------------------------------------
  `#bool_parse`  | `(string) => i1`       | `j.l.Boolean#parseBoolean`
  `#byte_parse`  | `(string, int) => i8`  | `j.l.Byte#parseByte(String, int)`
  `#short_parse` | `(string, int) => i16` | `j.l.Short#parseShort(String, int)`
  `#int_parse`   | `(string, int) => i32` | `j.l.Integer#parseInt(String, int)`
  `#long_parse`  | `(string, int) => i64` | `j.l.Long#parseLong(String, int)`
  `#float_parse` | `(string) => f32`      | `j.l.Float#parseFloat`
  `#double_parse`| `(string) => f64`      | `j.l.Double#parseDouble`

* **Primitive hash code**:

   Name               | Signature      | Description
  --------------------|----------------|-------------------------
  `#bool_hash_code`   | `(i1)  => i32` | `j.l.Boolean#hashCode`
  `#char_hash_code`   | `(i16) => i32` | `j.l.Character#hashCode`
  `#byte_hash_code`   | `(i8)  => i32` | `j.l.Byte#hashCode`
  `#short_hash_code`  | `(i16) => i32` | `j.l.Short#hashCode`
  `#int_hash_code`    | `(i32) => i32` | `j.l.Int#hashCode`
  `#long_hash_code`   | `(i64) => i32` | `j.l.Long#hashCode`
  `#float_hash_code`  | `(f32) => i32` | `j.l.Float#hashCode`
  `#double_hash_code` | `(f64) => i32` | `j.l.Double#hashCode`

* **Object**:

   Name               | Signature                | Description
  --------------------|--------------------------|-----------------------
  `#object_equals`    | `(object, object) => i1` | `j.l.Object#equals`
  `#object_to_string` | `(object) => string`     | `j.l.Object#toString`
  `#object_hash_code` | `(object) => i32`        | `j.l.Object#hashCode`
  `#object_get_class` | `(object) => class`      | `j.l.Object#getClass`

* **Class**:

   Name             | Signature           | Description
  ------------------|---------------------|---------------------
  `#class_get_name` | `(class) => string` | `j.l.Class.getName`

* **Monitor**:

   Name                 | Signature                    | Description
  ----------------------|------------------------------|-----------------------
  `#monitor_enter`      | `(object) => unit`           | start of synchronized block
  `#monitor_exit`       | `(object) => unit`           | end of synchronized block
  `#monitor_notify`     | `(object) => unit`           | `java.lang.Object#notify`
  `#monitor_notify_all` | `(object) => unit`           | `java.lang.Object#notifyAll`
  `#monitor_wait`       | `(object, i64, i32) => unit` | `java.lang.Object#wait`

* **String**:

   Name                       | Signature              | Description
  ----------------------------|------------------------|---------
  `#string_char_at`           | `(string, i32) => i16` | `java.lang.String#charAt`
  `#string_code_point_at`     | `(string, i32) => i32` | `java.lang.String#codePointAt`
  `...`                       | `...`                  | `...`

## Implementation

Intrinsics are implemented as C functions with the name of
intrinsic prepended by `nrt_` prefix. Type signatures of the
intrinsics are translated according to following type
correspondance rules:

 NIR Type         | C Type                   | C Convenience Alias
------------------|--------------------------|----------------------
 `void`           | `void`                   | `nrt_void`
 `bool`           | `bool`                   | `nrt_bool`
 `i8`, ..., `i64` | `int8_t`, ..., `int64_t` | `nrt_i8`, ..., `nrt_i64`
 `f32`, `f64`     | `float`, `double`        | `nrt_f32`, `nrt_f64`
 `[T x N]`        | `T[N]`                   | n/a
 `ptr T`          | `T*`                     | n/a
 `struct $name`   | `struct $name`           | n/a
 `size`           | `size_t`                 | `nrt_size`
 class types      | `void*`                  | `nrt_obj`

So for example `#int_box` is going to have following C signature:

```C
nrt_obj nrt_int_box(nrt_i32 value);
```

