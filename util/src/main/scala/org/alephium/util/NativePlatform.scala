// Copyright 2018 The Alephium Authors
// This file is part of the alephium project.
//
// The library is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// The library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with the library. If not, see <http://www.gnu.org/licenses/>.

package org.alephium.util

@SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
object NativePlatform {
  // True when running inside a native-image executable
  def inNativeImage: Boolean = {
    // Fast path: check system property Graal sets in native images
    // scalastyle:off null
    val p = System.getProperty("org.graalvm.nativeimage.imagecode")
    if (p != null) {
      true
    } else {
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
