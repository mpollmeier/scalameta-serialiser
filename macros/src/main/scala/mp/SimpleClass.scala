package mp

import scala.annotation.compileTimeOnly
import scala.meta._

@compileTimeOnly("@mp.simpleClass not expanded")
class simpleClass extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"class $name () { ..$stats }" = defn
    val main = q"def banana(args: Array[String]): Unit = { ..$stats }"
    val res = q"""
      class $name {
        $main
      }
    """

    // println("XXXXXXXXXXXXXXXXX")
    // println(res)
    // println("XXXXXXXXXXXXXXXXX")
    res
  }
}
