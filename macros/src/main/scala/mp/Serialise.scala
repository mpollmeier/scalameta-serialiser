package mp

import scala.annotation.compileTimeOnly
import scala.meta._

@compileTimeOnly("@mp.Serialise not expanded")
class serialise extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    // val q"""..$mods class A(..$params) {} """ = defn
    val q"..$mods class $tname[..$tparams] (...$paramss) extends $template" = defn //full match
    val res = q"""class A(i: Int)"""

    println("XXXXXXXXXXXXXXXXX")
    println(defn)
    println(s"mods=$mods")
    println(s"tname=$tname")
    println(s"paramss=$paramss")
    println(s"tparams=$tparams")
    println(s"template=$template")
    // println(res)
    println("XXXXXXXXXXXXXXXXX")
    res
  }
}
