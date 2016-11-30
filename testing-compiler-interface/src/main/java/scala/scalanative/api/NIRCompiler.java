package scala.scalanative.api;

import java.io.File;

/**
 * NIR compiler API.
 *
 * This API is used for testing the NIR compiler (the component that compiles
 * Scala trees to NIR.)
 */
public interface NIRCompiler {
	/**
	 * Compiles the source code given and returns all the files produced during
	 * compilation.
	 *
	 * @param source The source code to compile.
	 * @return All the files produced during compilation (classfiles, nir, hnir,
	 *         etc.)
	 */
    public File[] compile(String source);

    /**
     * Compiles all the source files in `base` and returns all the files
     * produced during compilation.
     *
     * @param base The base directory containing the source files.
     * @return All the files produced during compilation (classfiles, nir, hnir,
     *         etc.)
     */
    public File[] compile(File base);
}
