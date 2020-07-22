package com.oceanum.common


import scala.collection.JavaConverters._
import com.oceanum.expr.JavaMap

@SerialVersionUID(1L)
case class ExprContext(env: Map[String, Any]) {

  def taskMeta: RichTaskMeta = env.get(ExprContext.taskKey).map(_.asInstanceOf[RichTaskMeta]).getOrElse(new RichTaskMeta())

  def graphMeta: RichGraphMeta = env(ExprContext.graphKey).asInstanceOf[RichGraphMeta]

  def toJava: JavaMap[String, AnyRef] = evaluate(toJava(env).asInstanceOf[JavaMap[String, AnyRef]])

  def +(taskMeta: RichTaskMeta): ExprContext = this.copy(env + (ExprContext.taskKey -> taskMeta))

  def +(graphMeta: RichGraphMeta): ExprContext = this.copy(graphMeta.env ++ env + (ExprContext.graphKey -> graphMeta))

  def +(kv: (String, Any)): ExprContext = ExprContext(env + (kv._1 -> kv._2))

  def get[V](key: String): Option[V] = env.get(key).map(_.asInstanceOf[V])

  def iterator: Iterator[(String, Any)] = env.iterator

  def -(key: String): ExprContext = ExprContext(env - key)

  def ++(right: ExprContext): ExprContext = this ++ right.env

  def ++(right: Map[String, Any]): ExprContext = ExprContext(env ++ right)

  def put(key: String, value: Any): ExprContext = this + (key -> value)

  def remove(key: String): ExprContext = this - key

  def apply[OUT](key: String): OUT = env(key).asInstanceOf[OUT]

  private def toJava(ref: Any): AnyRef = ref.asInstanceOf[AnyRef] match {
    case map: Map[_, _] => map.mapValues(_.asInstanceOf[AnyRef]).mapValues(toJava).asJava
    case seq: Seq[_] => seq.map(_.asInstanceOf[AnyRef]).map(toJava).asJava
    case set: Set[_] => set.map(_.asInstanceOf[AnyRef]).map(toJava).asJava
    case itr: Iterable[_] => itr.map(_.asInstanceOf[AnyRef]).map(toJava).asJava
    case itr: Iterator[_] => itr.map(_.asInstanceOf[AnyRef]).map(toJava).asJava
    case default => default
  }

  private def evaluate(ref: AnyRef, env: JavaMap[String, AnyRef]): AnyRef = ref match {
    case str: String => StringParser.parseExprRaw(str)(env)
    case map: java.util.Map[_, _] => map.asScala.mapValues(v => evaluate(v.asInstanceOf[AnyRef], env)).asJava
    case seq: java.util.List[_] => seq.asScala.map(v => evaluate(v.asInstanceOf[AnyRef], env)).asJava
    case set: java.util.Set[_] => set.asScala.map(v => evaluate(v.asInstanceOf[AnyRef], env)).asJava
    case default => default
  }

  private def evaluate(env: JavaMap[String, AnyRef]): JavaMap[String, AnyRef] = {
    evaluate(env, env).asInstanceOf[JavaMap[String, AnyRef]]
  }
}

object ExprContext {
  val taskKey = "task"
  val graphKey = "graph"

  def empty: ExprContext = ExprContext(Map.empty)
}