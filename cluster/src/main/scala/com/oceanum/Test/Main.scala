package com.oceanum.Test

import com.oceanum.common.Environment

object Main {

  def main(args: Array[String]): Unit = {
    Environment.loadEnv(args)
  }
}
