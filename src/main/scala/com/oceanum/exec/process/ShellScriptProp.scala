package com.oceanum.exec.process

import com.oceanum.common.Environment
import com.oceanum.exec.LineHandler

/**
 * @author chenmingkun
 * @date 2020/5/2
 */
case class ShellScriptProp(scriptFile: String,
                           args: Array[String] = Array.empty,
                           env: Map[String, String] = Map.empty,
                           directory: String = "",
                           waitForTimeout: Long = -1,
                           stdoutHandler: LineHandler,
                           stderrHandler: LineHandler)
  extends ProcessProp(
      Environment.EXEC_SHELL +: args :+ scriptFile,
      env,
      directory,
      waitForTimeout,
      stdoutHandler,
      stderrHandler)
