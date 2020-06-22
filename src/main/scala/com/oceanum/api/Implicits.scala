package com.oceanum.api

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
 * @author chenmingkun
 * @date 2020/6/22
 */
object Implicits {
  implicit class DurationHelper(val sc: StringContext) extends AnyVal {
    def d(args: Any*): Duration = toDuration(args)

    def fd(args: Any*): FiniteDuration = {
      val duration = toDuration(args)
      FiniteDuration(duration._1, duration._2)
    }

    def duration(args: Any*): Duration = toDuration(args)

    def finiteDuration(args: Any*): FiniteDuration = {
      val duration = toDuration(args)
      FiniteDuration(duration._1, duration._2)
    }

    private def toDuration(args: Seq[Any]): Duration = Duration(sc.s(args: _*))
  }
}
