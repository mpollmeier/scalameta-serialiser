package mp

import scala.annotation.compileTimeOnly
import scala.annotation.StaticAnnotation
import scala.collection.immutable.Seq
import scala.meta._

@compileTimeOnly("@mp.entity not expanded")
class entity extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"..$mods class $tname[..$tparams] ..$ctorMods (...$paramss) extends $template" = defn
    val typeTermName = Term.Name(tname.value)

    object ToMap {
      val entityName: Term.Name = q"entity"
      val paramssFlat: Seq[Term.Param] = paramss.flatten
      def keyValues(entityName: Term.Name): Seq[Term] = paramssFlat.map { param =>
        val memberName = Term.Name(param.name.value)
        q"${param.name.value} -> $entityName.$memberName"
      }
    }

    object FromMap {
      val ctorValuesName: Term.Name = q"values"

    // TODO: support multiple constructor params lists
      val ctorParamsFirst: Seq[Term.Param] = paramss.headOption.getOrElse(Nil)
      def ctorArgs(valuesName: Term.Name): Seq[Term] = ctorParamsFirst.map { param =>
        val nameTerm = Term.Name(param.name.value)
        val tpe: Type = param.decltpe.get.asInstanceOf[Type.Name] // TODO: don't do option.get, don't cast
        q""" $nameTerm = $valuesName(${param.name.value}).asInstanceOf[$tpe] """
      }

    }
    val res = q"""
      ..$mods class $tname[..$tparams](...$paramss) {
      }

      object $typeTermName {
        def toMap(${ToMap.entityName}: $tname): Map[String, Any] =
          Map[String, Any](..${ToMap.keyValues(ToMap.entityName)})

        def fromMap(values: Map[String, Any]): $tname =
          ${typeTermName}(..${FromMap.ctorArgs(FromMap.ctorValuesName)})
      }
    """

    // println(res)
    res
  }
}
