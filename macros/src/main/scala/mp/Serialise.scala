package mp

import scala.annotation.compileTimeOnly
import scala.meta._

@compileTimeOnly("@mp.Serialise not expanded")
class serialise extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"class A(..$params) {}" = defn
    val res = q"""class A(i: Int)"""

    println("XXXXXXXXXXXXXXXXX")
    println(res)
    println(params)
    println("XXXXXXXXXXXXXXXXX")
    res
  }
}
