package com.oceanum.cluster.tasks

import com.oceanum.cluster.exec.InputStreamHandler
import com.oceanum.common.Environment

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
case class PythonTaskConfig(pyFile: String,
                            args: Array[String] = Array.empty,
                            env: Map[String, String] = Map.empty,
                            directory: String = Environment.EXEC_WORK_DIR,
                            waitForTimeout: Long = -1,
                            stdoutHandler: InputStreamHandler,
                            stderrHandler: InputStreamHandler)
  extends ProcessTaskConfig(
    Environment.EXEC_PYTHON +: pyFile +: args,
    env,
    directory,
    waitForTimeout,
    stdoutHandler,
    stderrHandler)
