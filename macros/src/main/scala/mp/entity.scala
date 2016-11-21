package mp

import scala.annotation.compileTimeOnly
import scala.annotation.StaticAnnotation
import scala.collection.immutable.Seq
import scala.meta._

@compileTimeOnly("@mp.entity not expanded")
class entity extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"..$mods class $tname[..$tparams] ..$ctorMods (...$paramss) extends $template" = defn

    val paramssFlat: Seq[Term.Param] = paramss.flatten
    val toMapContents: Seq[Term] = paramssFlat.map { param =>
      val memberName = Term.Name(param.name.value)
      q"${param.name.value} -> $memberName"
    }

    // TODO low prio: support multiple constructor params lists
    val ctorParamsFirst: Seq[Term.Param] = paramss.headOption.getOrElse(Nil)

    // def ctorArgs(): Seq[Term] = ctorParamsFirst.map { param =>  // TODO: drop this line
    def ctorArgs(values: Map[String, Any]): Seq[Term] = ctorParamsFirst.map { param =>
      val nameTerm = Term.Name(param.name.value)
      val tpe: Type = param.decltpe.get match { // TODO:don't do option.get
        case tpe: Type$Name$TypeNameImpl => tpe  // TODO: understand where TypeNameImpl is coming from and adapt
      }
      q"""
        $nameTerm = values(${param.name.value}).asInstanceOf[$tpe]
      """
      // $nameTerm = "blub".asInstanceOf[$tpe]
    }

    val typeTermName = Term.Name(tname.value)
    val res = q"""
      ..$mods class $tname[..$tparams](...$paramss) {
        def toMap(): Map[String, Any] = Map[String, Any](..$toMapContents)
      }

      object $typeTermName {
        def fromMap(values: Map[String, Any]): $tname = {
          ${typeTermName}(..${ctorArgs(Map.empty[String, Any])}) // TODO: pass in the actual map reference
        }
      }
    """
    // ${typeTermName}(..$ctorArgs(values))
    // ${typeTermName}(i = 42, s = "test")
    // def serialise(it: $tname): Map[String, Any] = Map[String, Any](..${keyValues(it)})

    println(res)
    res
  }

  // def keyValues(it: Int): Int = ???//paramssFlat.map { param =>
                                   //   val memberName = Term.Name(param.name.value)
                                   //   q"${param.name.value} -> $memberName"
                                   // }

  // def bla(i: Int): Term = q"1 + $i"
}
