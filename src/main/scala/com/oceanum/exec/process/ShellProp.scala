package com.oceanum.exec.process

import com.oceanum.exec.{InputStreamHandler, LineHandler}

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
case class ShellProp(cmd: Array[String] = Array.empty,
                     env: Map[String, String] = Map.empty,
                     directory: String = "",
                     waitForTimeout: Long = -1,
                     stdoutHandler: InputStreamHandler,
                     stderrHandler: InputStreamHandler)
extends ProcessProp(cmd, env, directory, waitForTimeout, stdoutHandler, stderrHandler)
