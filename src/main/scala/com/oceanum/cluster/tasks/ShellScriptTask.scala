package com.oceanum.cluster.tasks

import com.oceanum.client.InputStreamHandler
import com.oceanum.common.Environment

/**
 * @author chenmingkun
 * @date 2020/5/2
 */
case class ShellScriptTask(scriptFile: String,
                           args: Array[String] = Array.empty,
                           env: Map[String, String] = Map.empty,
                           directory: String = Environment.EXEC_WORK_DIR,
                           waitForTimeout: Long = -1,
                           stdoutHandler: InputStreamHandler,
                           stderrHandler: InputStreamHandler)
  extends ProcessTask(
      Environment.EXEC_SHELL +: args :+ scriptFile,
      env,
      directory,
      waitForTimeout,
      stdoutHandler,
      stderrHandler)
