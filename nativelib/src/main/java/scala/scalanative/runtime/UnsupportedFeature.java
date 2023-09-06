package scala.scalanative.runtime;

/**
 * Mock methods used to detect at linktime usage of unsupported features. Use
 * with caution and always use guarded with linktime-reasolved conditions. Eg.
 * 
 * <pre>
 * {@code
 * object MyThread extends Thread(){
 *   override def run(): Unit = 
 *    if !scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled 
 *    then UnsupportedFeature.threads() // fail compilation if multithreading is disabled
 *    else runLogic()
 * }
 * }
 * </pre>
 * 
 * Checking of unsupported features can be disabled in nativeConfig using
 * checkFeatures flag
 */
public abstract class UnsupportedFeature {
	// Always sync with tools/src/main/scala/scala/scalanative/linker/Reach.scala
	// UnsupportedFeature and UnsupportedFeatureExtractor and the stubs
	public static void threads() {
	}

	public static void virtualThreads() {
	}

	public static void continuations() {
	}
}
