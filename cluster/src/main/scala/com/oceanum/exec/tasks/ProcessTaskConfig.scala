package com.oceanum.exec.tasks

import java.io.{File, IOException}

import com.oceanum.exec.{StdHandler, TaskConfig}
import com.oceanum.common.{Environment, GraphContext, TaskMeta}
import com.oceanum.common.Implicits.PathHelper
import com.oceanum.expr.JavaMap
import com.oceanum.file.FileClient

import scala.concurrent.{ExecutionContext, Future}

abstract class ProcessTaskConfig(val propCmd: Array[String] = Array.empty,
                        val propEnv: Map[String, String] = Map.empty,
                        val propDirectory: String = Environment.EXEC_WORK_DIR,
                        val propWaitForTimeout: String = "24h",
                        val propStdoutHandler: StdHandler,
                        val propStderrHandler: StdHandler) extends TaskConfig {
  override def close(): Unit = {
    try {
      propStderrHandler.close()
      propStdoutHandler.close()
    } catch {
      case e: IOException =>
        throw new RuntimeException("Unable to close the handler :" + e)
    }
  }

  override def prepare(env: GraphContext)(implicit ec: ExecutionContext): Future[ProcessTaskConfig] = {
    Future {
      val rawEnv = env.javaExprEnv
      val taskMeta = env.taskMeta
      val taskConfig = SuUserTaskConfig(taskMeta.user, this).parseFunction(rawEnv)
      val fileMap: Map[String, String] = taskConfig.files
        .map(src => (src, taskMeta.execDir/new File(src).getName))
        .toMap
      val newConfig = taskConfig.convert(fileMap)
      fileMap.map(kv => FileClient.download(kv._1, kv._2))
        .reduce((f1, f2) => f1.flatMap(_ => f2))
        .map(_ => newConfig)
    } flatMap {
      future => future
    }
  }

  def parseFunction(implicit exprEnv: JavaMap[String, AnyRef]): ProcessTaskConfig

  def files: Seq[String] = Seq.empty

  def convert(fileMap: Map[String, String]): ProcessTaskConfig = this
}