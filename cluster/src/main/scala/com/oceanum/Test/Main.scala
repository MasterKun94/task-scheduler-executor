package com.oceanum.Test

import com.oceanum.annotation.util.JobEnum
import com.oceanum.common.Environment

object Main {

  def main(args: Array[String]): Unit = {
    Environment.loadArgs(args)
    println(JobEnum.jobWithAnnotation)
  }
}
