package com.oceanum.exec.tasks

import com.oceanum.api.InputStreamHandler
import com.oceanum.common.Environment

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
case class JavaTask(jars: Array[String],
                    mainClass: String,
                    args: Array[String] = Array.empty,
                    options: Array[String] = Array.empty,
                    env: Map[String, String] = Map.empty,
                    directory: String = "",
                    waitForTimeout: Long = -1,
                    stdoutHandler: InputStreamHandler,
                    stderrHandler: InputStreamHandler)
  extends ProcessTask(
      (Environment.EXEC_JAVA +: options :+ "-cp" :+ jars.mkString(":") :+ mainClass) ++ args,
      env,
      directory,
      waitForTimeout,
      stdoutHandler,
      stderrHandler
  )
