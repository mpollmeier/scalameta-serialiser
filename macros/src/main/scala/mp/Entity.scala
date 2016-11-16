package mp

import scala.annotation.compileTimeOnly
import scala.annotation.StaticAnnotation
import scala.meta._

@compileTimeOnly("@mp.Serialise not expanded")
class entity extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"..$mods class $tname[..$tparams] ..$ctorMods (...$paramss) extends $template" = defn
    val res = q"""class A(i: Int)"""

    println("XXXXXXXXXXXXXXXXX")
    println(defn)
    println(s"mods=$mods")
    println(s"tname=$tname")
    println(s"paramss=$paramss")
    println(s"tparams=$tparams")
    println(s"template=$template")
    println(s"ctorMods=$ctorMods")
    println("XXXXXXXXXXXXXXXXX")
    println(res)
    res
  }
}
