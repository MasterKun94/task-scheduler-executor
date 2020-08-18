package com.oceanum.jdbc

import tech.ibit.sqlbuilder.{Column, Criteria, CriteriaItem, Table}

import scala.language.implicitConversions

package object expr {

  def table(name: String): Table = {
    new Table(name, name)
  }

  implicit def column(name: String)(implicit table: Table): Column = {
    new Column(table, name)
  }

  implicit def convert(item: CriteriaItem): Criteria = {
    Criteria.and(item)
  }
}
