package aviator

import java.util.Date

//import com.oceanum.expr.Evaluator
//import org.json4s
//import org.json4s.{Formats, JObject, NoTypeHints}
//import org.json4s.jackson.Serialization
//
///**
// * @author chenmingkun
// * @date 2020/7/16
// */
//object Test extends App {
//
//  val map = Map("name" -> "test", "id" -> 1, "date" -> new Date())
//  implicit val formats: AnyRef with Formats = Serialization.formats(NoTypeHints)
//  val str = Serialization.write(map)
//  println(str)
//  val obj = Serialization.read[JObject](str)
//  println(obj)
//  println(obj.\("id").values)
//}
//
//object JsonExample extends App {
//  import org.json4s._
//  import org.json4s.JsonDSL._
//  import org.json4s.jackson.JsonMethods._
//
//  case class Winner(id: Long, numbers: List[Int])
//  case class Lotto(id: Long, winningNumbers: List[Int], winners: List[Winner], drawDate: Option[java.util.Date])
//
//  val winners = List(Winner(23, List(2, 45, 34, 23, 3, 5)), Winner(54, List(52, 3, 12, 11, 18, 22)))
//  val lotto = Lotto(5, List(2, 45, 34, 23, 7, 5, 3), winners, None)
//
//  val json: (String, json4s.JObject) =
//    ("lotto" ->
//      ("lotto-id" -> lotto.id) ~
//        ("winning-numbers" -> lotto.winningNumbers) ~
//        ("draw-date" -> lotto.drawDate.map(_.toString)) ~
//        ("winners" ->
//          lotto.winners.map { w =>
//            (("winner-id" -> w.id) ~
//              ("numbers" -> w.numbers))}))
//
//  println(compact(render(json)))
//}