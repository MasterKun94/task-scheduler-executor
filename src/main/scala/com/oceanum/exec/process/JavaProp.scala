package com.oceanum.exec.process

import com.oceanum.common.Environment
import com.oceanum.exec.LineHandler

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
case class JavaProp(jars: Array[String],
                    mainClass: String,
                    args: Array[String] = Array.empty,
                    options: Array[String] = Array.empty,
                    env: Map[String, String] = Map.empty,
                    directory: String = "",
                    waitForTimeout: Long = -1,
                    stdoutHandler: LineHandler,
                    stderrHandler: LineHandler)
  extends ProcessProp(
      (Environment.EXEC_JAVA +: options :+ "-cp" :+ jars.mkString(":") :+ mainClass) ++ args,
      env,
      directory,
      waitForTimeout,
      stdoutHandler,
      stderrHandler
  )
