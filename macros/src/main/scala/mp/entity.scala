package mp

import scala.annotation.compileTimeOnly
import scala.annotation.StaticAnnotation
import scala.collection.immutable.Seq
import scala.meta._

@compileTimeOnly("@mp.entity not expanded")
class entity extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"..$mods class $tname[..$tparams] ..$ctorMods (...$paramss) extends $template" = defn

    // println(defn)
    // println(template)

    val paramssFlat: Seq[Term.Param] = paramss.flatten
    val toMapContents: Seq[Term] = paramssFlat.map { param =>
      val memberName = Term.Name(param.name.value)
      q"${param.name.value} -> $memberName"
    }

    val termName = Term.Name(tname.value)

    val res = q"""
      ..$mods class $tname[..$tparams](...$paramss) {
        def toMap(): Map[String, Any] = Map[String, Any](..$toMapContents)
      }
    """
    // object $termName
    println(res)
    res
  }
}
