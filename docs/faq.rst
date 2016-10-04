.. _faq:

FAQ
===

---

**Q:** How do I make the resulting executable smaller?

**A:** Try to use `-O2` and then https://upx.github.io/

---

**Q:** How do I specify which native library an extern object should link to?

**A:** Use the `@link("libraryname")` annotation to automatically link with
a native library whenever an annotated extern object is used. (e.g. `@link("png"`) to link with "libpng"):

.. code-block: scala

    @link("telldus-core")
    @extern object TelldusCore {
      def tdInit(): Unit = extern
      [...]
    }
