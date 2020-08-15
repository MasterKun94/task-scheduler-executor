package com.oceanum.api

import com.oceanum.expr.JavaMap
import scala.collection.JavaConversions.mapAsJavaMap

/**
 * @author chenmingkun
 * @date 2020/8/16
 */
case class SearchRequest(expr: String = "repo.findAll()", sorts: Seq[Sort] = Array.empty[Sort], size: Int = 10, page: Int = 0, env: Map[String, Any] = Map.empty) {
  def getEnv: JavaMap[String, AnyRef] = env.mapValues(_.asInstanceOf[AnyRef])
}
