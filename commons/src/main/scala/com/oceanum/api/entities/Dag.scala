package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
@ISerializationMessage("DAG")
case class Dag(vertexes: Map[String, Vertex], edges: Map[String, Array[String]])
