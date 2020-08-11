package com.oceanum.pluggable

import java.util

import akka.actor.{ActorPath, ActorSelection, ActorSystem}
import com.oceanum.pluggable.Env.UpdateState

import scala.collection.JavaConversions.mapAsScalaMap

object Main {

  def main(args: Array[String]): Unit = {
    val className = args(0)
    val actorPath = args(1)
    val host = args(2)
    val taskArgs = args.drop(3)
    val system = Env.createSystem(host)
    val primEndpoint = system.actorSelection(ActorPath.fromString(actorPath))
    val listener = executorListener(primEndpoint)

    try {
      val executor = Class.forName(className).getConstructor().newInstance().asInstanceOf[Executor]
      addShutDownHook(executor, listener, system)
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

  private def addShutDownHook(executor: Executor, listener: StateListener, system: ActorSystem): Unit = {
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run(): Unit = {
        println()
        if (executor.isRunning) {
          executor.kill(listener)
          println("executor killed")
        }
        executor.close()
        println("executor closed")

        system.terminate()
        system.getWhenTerminated.toCompletableFuture.get()
        println("terminated")
        println()
      }
    }))
  }
}
