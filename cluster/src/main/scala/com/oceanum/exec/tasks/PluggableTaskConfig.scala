package com.oceanum.exec.tasks

import akka.actor.{ActorRef, PoisonPill}
import com.oceanum.common.Environment
import com.oceanum.exec.StdHandler
import com.oceanum.expr.{ExprParser, JavaMap}

case class PluggableTaskConfig(args: Array[String] = Array.empty,
                               plugJars: Array[String],
                               plugClass: String,
                               mainClass: String,
                               files: Array[String],
                               jars: Array[String],
                               options: Array[String] = Array.empty,
                               env: Map[String, String] = Map.empty,
                               directory: String = Environment.EXEC_WORK_DIR,
                               waitForTimeout: String = "24h",
                               communicator: ActorRef,
                               stdoutHandler: StdHandler,
                               stderrHandler: StdHandler)
  extends ProcessTaskConfig(
    {
      val _jars: Array[String] = jars ++ plugJars
      val _mainClass: String = mainClass
      val _args: Array[String] = plugClass +:
        communicator.path.toStringWithAddress(communicator.path.address.copy(
          protocol = "akka.tcp",
          host = Some(Environment.HOST),
          port = Some(Environment.CLUSTER_NODE_PORT))) +:
        Environment.HOST +:
        args
      val _options: Array[String] = options
      (Environment.EXEC_JAVA +: _options :+ "-cp" :+ _jars.mkString(":") :+ _mainClass) ++ _args
    },
    env,
    directory,
    waitForTimeout,
    stdoutHandler,
    stderrHandler
  ) {

  override def parseFunction(implicit exprEnv: JavaMap[String, AnyRef]): ProcessTaskConfig = {
    this.copy(
      args = args.map(ExprParser.parse(_)),
      files = files.map(ExprParser.parse(_)),
      jars = jars.map(ExprParser.parse(_)),
      options = options.map(ExprParser.parse(_)),
      env = env.mapValues(ExprParser.parse(_))
    )
  }

  override def fileSeq: Seq[String] = files ++ jars

  override def convert(fileMap: Map[String, String]): ProcessTaskConfig = {
    this.copy(
      files = files.map(fileMap),
      jars = jars.map(fileMap)
    )
  }

  override def close(): Unit = {
    communicator ! PoisonPill
    super.close()
  }
}

