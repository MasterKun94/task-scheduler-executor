package com.oceanum.cluster.tasks

import com.oceanum.cluster.exec.StdHandler
import com.oceanum.common.Environment

/**
 * @author chenmingkun
 * @date 2020/5/2
 */
case class ShellScriptTaskConfig(scriptFile: String,
                                 args: Array[String] = Array.empty,
                                 env: Map[String, String] = Map.empty,
                                 directory: String = Environment.EXEC_WORK_DIR,
                                 waitForTimeout: Long = -1,
                                 stdoutHandler: StdHandler,
                                 stderrHandler: StdHandler)
  extends ProcessTaskConfig(
      Environment.EXEC_SHELL +: args :+ scriptFile,
      env,
      directory,
      waitForTimeout,
      stdoutHandler,
      stderrHandler)
