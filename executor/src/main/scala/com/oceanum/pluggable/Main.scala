package com.oceanum.pluggable

import java.util

import akka.actor.ActorSelection
import com.oceanum.pluggable.Env.UpdateState

import scala.collection.JavaConversions.mapAsScalaMap

class Main {
  def main(args: Array[String]): Unit = {
    val className = args(0)
    val actorPath = args(1)
    val taskArgs = args.drop(2)
    val executor = Class.forName(className).getConstructor().newInstance().asInstanceOf[Executor]
    val primEndpoint = Env.system.actorSelection(actorPath)
    val listener = executorListener(primEndpoint)
    addShutDownHook(executor, listener)
    try {
      executor.run(taskArgs, listener)
      System.exit(0)
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        System.exit(1)
    }
  }

  private def executorListener(primEndpoint: ActorSelection): StateListener = new StateListener {
    override def updateState(info: util.Map[String, String]): Unit = {
      primEndpoint ! UpdateState(info.toMap)
    }
  }

  private def addShutDownHook(executor: Executor, listener: StateListener): Unit = {
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run(): Unit = {
        if (executor.isRunning) {
          executor.kill(listener)
        }
        executor.close()
      }
    }))
    Env.system.terminate()
    Env.system.getWhenTerminated.toCompletableFuture.get()
  }
}
