package com.oceanum.common


import com.oceanum.annotation.ISerializationMessage
import com.oceanum.expr.{ExprParser, JavaMap}

import scala.collection.JavaConverters._

@SerialVersionUID(1L)
@ISerializationMessage("GRAPH_CONTEXT")
case class GraphContext(env: Map[String, Any], graphMeta: GraphMeta = null, taskMeta: TaskMeta = null) {

  def exprEnv: Map[String, Any] = graphMeta.env ++ env + (GraphContext.graphKey -> graphMeta) + (GraphContext.taskKey -> taskMeta)

  def javaExprEnv: JavaMap[String, AnyRef] = evaluate(toJava(exprEnv).asInstanceOf[JavaMap[String, AnyRef]])

  def +(kv: (String, Any)): GraphContext = this.copy(env = env + (kv._1 -> kv._2))

  def get[V](key: String): Option[V] = exprEnv.get(key).map(_.asInstanceOf[V])

  def iterator: Iterator[(String, Any)] = env.iterator

  def -(key: String): GraphContext = this.copy(env = env - key)

  def ++(right: GraphContext): GraphContext = this ++ right.env

  def ++(right: Map[String, Any]): GraphContext = this.copy(env = env ++ right)

  def put(key: String, value: Any): GraphContext = this + (key -> value)

  def remove(key: String): GraphContext = this - key

  def apply[OUT](key: String): OUT = exprEnv(key).asInstanceOf[OUT]

  private def toJava(ref: Any): AnyRef = ref.asInstanceOf[AnyRef] match {
    case map: Map[_, _] => map.mapValues(_.asInstanceOf[AnyRef]).mapValues(toJava).asJava
    case seq: Seq[_] => seq.map(_.asInstanceOf[AnyRef]).map(toJava).asJava
    case set: Set[_] => set.map(_.asInstanceOf[AnyRef]).map(toJava).asJava
    case itr: Iterable[_] => itr.map(_.asInstanceOf[AnyRef]).map(toJava).asJava
    case itr: Iterator[_] => itr.map(_.asInstanceOf[AnyRef]).map(toJava).asJava
    case default => default
  }

  private def evaluate(ref: AnyRef, env: JavaMap[String, AnyRef]): AnyRef = ref match {
    case str: String => ExprParser.parse(str)(env)
    case map: java.util.Map[_, _] => map.asScala.mapValues(v => evaluate(v.asInstanceOf[AnyRef], env)).asJava
    case seq: java.util.List[_] => seq.asScala.map(v => evaluate(v.asInstanceOf[AnyRef], env)).asJava
    case set: java.util.Set[_] => set.asScala.map(v => evaluate(v.asInstanceOf[AnyRef], env)).asJava
    case default => default
  }

  private def evaluate(env: JavaMap[String, AnyRef]): JavaMap[String, AnyRef] = {
    evaluate(env, env).asInstanceOf[JavaMap[String, AnyRef]]
  }
}

object GraphContext {
  val taskKey = "task"
  val graphKey = "graph"

  def empty: GraphContext = GraphContext(Map.empty, null, null)
}