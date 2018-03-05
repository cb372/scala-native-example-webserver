package webserver

import scala.scalanative.native._

object UvUtil {

  def bailOnError(f: => CInt): Unit = {
    val result = f
    if (result != 0) {
      val errorName = uv.getErrorName(result)
      println(s"Failed: ${fromCString(errorName)}")
      System.exit(1)
    }
  }

}
