package com.oceanum.common

/**
 * @author chenmingkun
 * @date 2020/8/3
 */
class ConsistentHashing[T](set: Set[T], strFunc: T => String) {
  private val hashing: Seq[String => Int] = Seq(getHash1, getHash2, getHash3, getHash4, getHash5)
  private val limit = 20000
  def select(value: Int): T = {
    val bucket: Seq[(Int, T)] = set.toSeq.flatMap(t => hashing.map(func => (math.abs(func(strFunc(t))) % limit, t))).sortBy(_._1)
    val num = math.abs(value) % limit
    println(bucket)
    for (elem <- bucket) {
      if (elem._1 > num) {
        return elem._2
      }
    }
    bucket.head._2
  }

  private def getHash1(key: String): Int = {
    val p = 16777619
    var hash = 2166136261L.toInt
    var i = 0
    while (i < key.length) {
      hash = (hash ^ key.charAt(i)) * p
      i += 1
    }
    hash += hash << 13
    hash ^= hash >> 7
    hash += hash << 3
    hash ^= hash >> 17
    hash += hash << 5

    // 如果算出来的值为负数则取其绝对值
    math.abs(hash)
  }

  private def getHash2(str: String): Int = {
    val b = 378551
    var a = 63689
    var hash = 0
    for (i <- 0 until str.length) {
      hash = hash * a + str.charAt(i)
      a = a * b
    }
    hash & 0x7FFFFFFF
  }

  private def getHash3(str: String): Int = {
    str.hashCode
  }

  private def getHash4(str: String): Int = {
    var key = str.hashCode
    key += ~(key << 15)
    key ^= (key >>> 10)
    key += (key << 3)
    key ^= (key >>> 6)
    key += ~(key << 11)
    key ^= (key >>> 16)
    key
  }

  private def getHash5(str: String): Int = {
    var hash = str.length
    for (i <- 0 until str.length) {
      hash = ((hash << 5) ^ (hash >> 27)) ^ str.charAt(i)
    }
    hash & 0x7FFFFFFF
  }
}