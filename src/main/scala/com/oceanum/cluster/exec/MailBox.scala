package com.oceanum.cluster.exec

import java.util.Comparator
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author chenmingkun
 * @date 2020/5/1
 */

class MailBox[T](queue: BlockingQueue[T], listener: T => Unit, listenerNum: Int) {
  val running = new AtomicBoolean(true)

  private val threads: Array[Thread] = {
    Array.fill(listenerNum) {
      val thread = new Thread() {
        override def run(): Unit = {
          while (running.get()) {
            listener(queue.take())
          }
        }
      }
      thread.setDaemon(true)
      thread.start()
      thread
    }
  }

  def send(t: T): Unit = queue.put(t)

  def queueSize: Int = queue.size()

  def close(): Unit = running.set(false)
}

object MailBox {
  def apply[T](listener: T => Unit, listenerNum: Int): MailBox[T] = {
    new MailBox[T](new LinkedBlockingQueue[T](), listener, listenerNum)
  }

  def priority[T](listener: T => Unit, listenerNum: Int, sortBy: (T, T) => Int): MailBox[T] = {
    new MailBox[T](new PriorityBlockingQueue[T](32, new Comparator[T] {
      override def compare(o1: T, o2: T): Int = sortBy(o1, o2)
    }), listener, listenerNum)
  }

  def delay[T <: Delayed](listener: T => Unit, listenerNum: Int): MailBox[T] = {
    new MailBox[T](new DelayQueue[T](), listener, listenerNum)
  }
}
