package com.oceanum.cluster.tasks

import java.io.{File, IOException}

import com.oceanum.client.TaskMeta
import com.oceanum.cluster.exec.{StdHandler, TaskConfig}
import com.oceanum.common.Environment
import com.oceanum.common.Implicits.PathHelper
import com.oceanum.file.FileClient

import scala.concurrent.{ExecutionContext, Future}

abstract class ProcessTaskConfig(val propCmd: Array[String] = Array.empty,
                        val propEnv: Map[String, String] = Map.empty,
                        val propDirectory: String = Environment.EXEC_WORK_DIR,
                        val propWaitForTimeout: Long = -1,
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

  override def prepare(taskMeta: TaskMeta[_], env: Map[String, Any])(implicit ec: ExecutionContext): Future[ProcessTaskConfig] = {
    val taskConfig = SuUserTaskConfig(taskMeta.user, this).parseFunction(env)
    val fileMap: Map[String, String] = taskConfig.files
      .map(src => (src, taskMeta.execDir/new File(src).getName))
      .toMap
    val newConfig = taskConfig.convert(fileMap)
    fileMap.map(kv => FileClient.download(kv._1, kv._2))
      .reduce((f1, f2) => f1.flatMap(_ => f2))
      .map(_ => newConfig)
  }

  def parseFunction(implicit exprEnv: Map[String, Any]): ProcessTaskConfig

  def files: Seq[String] = Seq.empty

  def convert(fileMap: Map[String, String]): ProcessTaskConfig = this
}