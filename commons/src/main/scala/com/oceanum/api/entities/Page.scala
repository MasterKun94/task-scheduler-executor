package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage

@ISerializationMessage("ELEMENTS")
case class Page[T](elements: Seq[T], size: Int, page: Int) {
  def elementSize: Int = elements.size
}
