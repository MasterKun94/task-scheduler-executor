package com.oceanum

import com.oceanum.common.SystemInit
import com.oceanum.persistence.ExpressionFactory

/**
 * @author chenmingkun
 * @date 2020/7/17
 */
package object expr {
  type JavaMap[K, V] = java.util.Map[K, V]
  type JavaHashMap[K, V] = java.util.HashMap[K, V]

  val exprFactory: ExpressionFactory = SystemInit.repositoryFactory.expressionFactory
}
