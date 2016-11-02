package scala.scalanative.testing.compiler.api;

import java.io.File;

public interface NIRCompiler {
    public File[] getNIR(String source);
    public File[] getNIR(File base);
}
