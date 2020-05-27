package com.oceanum.actors

/**
 * @author chenmingkun
 * @date 2020/5/3
 */
object ClientMain {
//  def main(args: Array[String]): Unit = {
//    val conf = ConfigFactory.load("client.conf")
//    val clientSystem = ActorSystem("ClientSystem", conf)
//    /* 从 conf 文件里读取 contact-points 地址
//      val initialContacts = immutableSeq(conf.getStringList("contact-points")).map {
//        case AddressFromURIString(addr) ⇒ RootActorPath(addr) / "system" / "receptionist"
//      }.toSet
//    */
//
//    //先放一个contact-point, 系统会自动增加其它的点
//    val initialContacts = Set(
//      ActorPaths.fromString("akka.tcp://sys@127.0.0.1:3551/system/receptionist")
//    )
//
//    val clusterClient = clientSystem.actorOf(
//      ClusterClient.props(
//        ClusterClientSettings(clientSystem)
//          .withInitialContacts(initialContacts)),
//      "client-endpoint")
//    clientSystem.actorOf(Props(classOf[ClientListener], clusterClient), "client-event-listener")
//    val client: ActorRef = clientSystem.actorOf(Props(classOf[ClientActor], clusterClient), "client")
//
//    client ! AvailableExecutorRequest("test")
//    Thread.sleep(1000)
//    client ! ExecuteOperatorRequest(OperatorMessage(
//      "test",
//      3,
//      3000,
//      1,
//      PythonPropMessage(
//        pyFile = "C:\\Users\\chenmingkun\\work\\idea\\work\\task-scheduler-core\\task-scheduler-executor\\src\\main\\resources\\test.py",
//        args = Array("hello", "world"),
//        stdoutLineHandler = InputHandlerProducer.file("C:\\Users\\chenmingkun\\work\\idea\\work\\task-scheduler-core\\task-scheduler-executor\\src\\main\\resources\\test1.txt"),
//        stderrLineHandler = InputHandlerProducer.file("C:\\Users\\chenmingkun\\work\\idea\\work\\task-scheduler-core\\task-scheduler-executor\\src\\main\\resources\\test2.txt"),
//        waitForTimeout = 100000
//      )), CheckStateScheduled(FiniteDuration(1000, TimeUnit.MILLISECONDS), client, state => println(state)))
//  }
}
