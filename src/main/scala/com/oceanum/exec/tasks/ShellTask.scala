package com.oceanum.exec.tasks

import com.oceanum.api.InputStreamHandler

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
case class ShellTask(cmd: Array[String] = Array.empty,
                     env: Map[String, String] = Map.empty,
                     directory: String = "",
                     waitForTimeout: Long = -1,
                     stdoutHandler: InputStreamHandler,
                     stderrHandler: InputStreamHandler)
extends ProcessTask(cmd, env, directory, waitForTimeout, stdoutHandler, stderrHandler)
