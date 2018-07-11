
.. _faq:

FAQ
===

---

**Q:** How do I make the resulting executable smaller?

**A1:** Compress the binary with https://upx.github.io/

**A2:** Your operating system may allow stripping symbols from the
        executable (man strip). On systems which allow symbol stripping,
	the nativeLinkerOption can be set (see :ref:`interopOptionsExamples`)
	to do this at link time.

---

**Q:** Does Scala Native support WebAssembly?

**A:** Support for WebAssembly is out of scope for the project.
If you need to run Scala code in the browser, consider using
`Scala.js <https://www.scala-js.org>`_ instead.

Troubleshooting
---------------
When compiling your Scala Native project, the linker ``ld`` may fail with the following message:

::

  relocation R_X86_64_32 against `.rodata.str1.1' can not be used when making a shared object; recompile with -fPIC

It is likely that the ``LDFLAGS`` environment variable enables hardening. For example, this occurs when the ``hardening-wrapper`` package is installed on Arch Linux. It can be safely removed.

