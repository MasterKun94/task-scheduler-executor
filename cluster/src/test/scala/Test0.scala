import java.util.Date

import com.oceanum.Test.Test
import com.oceanum.common.{Environment, RichGraphMeta, RichTaskMeta, SystemInit}
import com.oceanum.serialize.Serialization

/**
 * @author chenmingkun
 * @date 2020/7/25
 */
object Test0 {


  def main(args: Array[String]): Unit = {
    Environment.loadArgs(args)
    SystemInit.initAnnotatedClass()
    SystemInit.serialization
  }
}
