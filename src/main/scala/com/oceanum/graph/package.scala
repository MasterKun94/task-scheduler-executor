package com.oceanum

import akka.stream.scaladsl.{GraphDSL, RunnableGraph}

package object graph {
  type Mat = Workflow
  type DslBuilder = GraphDSL.Builder[Mat]
  type Graph = RunnableGraph[Mat]
}
