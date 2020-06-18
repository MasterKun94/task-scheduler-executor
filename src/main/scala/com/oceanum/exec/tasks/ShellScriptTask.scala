package com.oceanum.exec.tasks

import com.oceanum.api.InputStreamHandler
import com.oceanum.common.Environment

/**
 * @author chenmingkun
 * @date 2020/5/2
 */
case class ShellScriptTask(scriptFile: String,
                           args: Array[String] = Array.empty,
                           env: Map[String, String] = Map.empty,
                           directory: String = "",
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
