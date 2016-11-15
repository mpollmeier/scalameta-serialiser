package mp

import scala.annotation.compileTimeOnly
import scala.meta._

@compileTimeOnly("@mp.Serialise not expanded")
class serialise extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"class $name () { ..$stats }" = defn
    val fun = q"def fun(i: Int): Unit = { ..$stats }"
    val res = q"""
      class $name () {
        $fun
      }
    """

    println("XXXXXXXXXXXXXXXXX")
    println(res)
    println("XXXXXXXXXXXXXXXXX")
    res
  }
}
