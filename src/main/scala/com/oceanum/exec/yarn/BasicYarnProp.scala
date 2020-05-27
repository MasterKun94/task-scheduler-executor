package com.oceanum.exec.yarn

import com.oceanum.exec.LineHandler
import com.oceanum.exec.process.ProcessProp

/**
 * @author chenmingkun
 * @date 2020/5/9
 */
case class BasicYarnProp(exec: String,
                         args: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: String = "",
                         waitForTimeout: Long = -1,
                         stdoutHandler: LineHandler,
                         stderrHandler: LineHandler)
  extends ProcessProp(exec +: args, env, directory, waitForTimeout, stdoutHandler, stderrHandler)
