package mp

import scala.annotation._
import scala.meta._
import scala.meta.transversers._
import scala.collection.immutable.Seq

// class withAddon extends StaticAnnotation {
//   inline def apply(defn: Any): Any = meta {
//     val q"object $name { ..$stats }" = defn
//     val main = q"def banana(args: Array[String]): Unit = { ..$stats }"
//     q"object $name { $main }"
//   }
// }
