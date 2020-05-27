package com.oceanum.exec.process

import com.oceanum.exec.LineHandler

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
case class ShellProp(cmd: Array[String] = Array.empty,
                     env: Map[String, String] = Map.empty,
                     directory: String = "",
                     waitForTimeout: Long = -1,
                     stdoutHandler: LineHandler,
                     stderrHandler: LineHandler)
extends ProcessProp(cmd, env, directory, waitForTimeout, stdoutHandler, stderrHandler)
