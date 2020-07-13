package com.oceanum.cluster.tasks

import com.oceanum.cluster.exec.StdHandler
import com.oceanum.common.Environment

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
case class JavaTaskConfig(jars: Array[String],
                          mainClass: String,
                          args: Array[String] = Array.empty,
                          options: Array[String] = Array.empty,
                          env: Map[String, String] = Map.empty,
                          directory: String = Environment.EXEC_WORK_DIR,
                          waitForTimeout: Long = -1,
                          stdoutHandler: StdHandler,
                          stderrHandler: StdHandler)
  extends ProcessTaskConfig(
      (Environment.EXEC_JAVA +: options :+ "-cp" :+ jars.mkString(":") :+ mainClass) ++ args,
      env,
      directory,
      waitForTimeout,
      stdoutHandler,
      stderrHandler
  )
