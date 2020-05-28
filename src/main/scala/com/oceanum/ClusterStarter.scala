package com.oceanum

import com.oceanum.actors.ClusterMain
import com.oceanum.common.Environment

/**
 * @author chenmingkun
 * @date 2020/5/28
 */
object ClusterStarter {

  def main(args: Array[String]): Unit = {

    val arg = args.map(str => str.split("="))
      .map(arr => (arr(0), arr(1)))
      .toMap
    val topics = arg
      .get("--topics")
      .map(_.split(",").map(_.trim))
      .getOrElse(Array.empty)
      .union(Environment.CLUSTER_NODE_TOPICS)
      .distinct
      .toSeq
    val host = arg.getOrElse("--host", Environment.CLUSTER_NODE_HOST)
    val port = arg.get("--port").map(_.toInt).getOrElse(Environment.CLUSTER_NODE_PORT)
    val seedNodes = arg.get("--seed-node")
      .map(_.split(",").map(_.trim).toSeq)
      .getOrElse(Environment.CLUSTER_SEED_NODE)
    ClusterMain.start(topics, host, port, seedNodes)
  }
}
