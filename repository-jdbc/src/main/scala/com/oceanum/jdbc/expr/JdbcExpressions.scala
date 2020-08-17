package com.oceanum.jdbc.expr

import com.oceanum.persistence.Expression

case class WhereExpression(expr: Array[String]) extends Expression
case class LimitExpression(limit: Int = 0) extends Expression
case class SelectExpression(where: WhereExpression, limit: LimitExpression)