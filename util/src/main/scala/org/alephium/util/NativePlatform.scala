package org.alephium.util

@SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
object NativePlatform {
  // True when running inside a native-image executable
  def inNativeImage: Boolean = {
    // Fast path: check system property Graal sets in native images
    val p = System.getProperty("org.graalvm.nativeimage.imagecode")
    if (p != null) true
    else {
      // Fallback: call the public API via reflection if available
      // (avoids hard dependency on graal-sdk at JVM runtime)
      try {
        val cls = Class.forName("org.graalvm.nativeimage.ImageInfo")
        val m   = cls.getMethod("inImageCode")
        java.lang.Boolean.TRUE == m.invoke(null).asInstanceOf[java.lang.Boolean]
      } catch {
        case _: Throwable => false
      }
    }
  }
}
