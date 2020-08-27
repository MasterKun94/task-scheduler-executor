package com.oceanum.jdbc
import slick.dbio.Effect
import slick.jdbc.{GetResult, JdbcType, MySQLProfile}
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{ProvenShape, Rep}
import slick.sql.FixedSqlStreamingAction

import scala.util.{Failure, Success}

object Test extends App {

  case class TestUser(id: String, name: String)

  class TestUserTable(tag: Tag) extends Table[TestUser](tag, "test_user") {
    def id: Rep[String] = column[String]("id")

    def name: Rep[String] = column[String]("name")

    override def * : ProvenShape[TestUser] = (id, name) <> (TestUser.tupled, TestUser.unapply)
  }
  def tableQuery: TableQuery[TestUserTable] = TableQuery[TestUserTable]

  val url = "jdbc:mysql://192.168.10.136/optimus_scheduler_test?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true"
  val user = "root"
  val pass = "lygr@0907"
  val driver = "com.mysql.jdbc.Driver"
  val db = Database.forURL(
    url = url,
    driver = driver,
    user = user,
    password = pass
  )
  val colType: JdbcType[String] = stringColumnType
  def func[T]: Table[T] => Rep[Boolean] = t => t.column("id")(colType) =!= ""

  val query: FixedSqlStreamingAction[Seq[TestUser], TestUser, Effect.Read] = tableQuery.filter(func).drop(1).take(2).result

  import scala.concurrent.ExecutionContext.Implicits.global
  val getResult = GetResult(r => TestUser(r.nextString(), r.nextString()))
  val sql = sql"select id, name from test_user".as(getResult)
  db.run(query).andThen {
    case Success(value) =>
      value.foreach(println)

    case Failure(exception) => exception.printStackTrace()
  }


  def select(c: TestUser): DBIO[Int] = sqlu"insert into coffees values (${c.id}, ${c.name})"

  Thread.sleep(3000)
  println(sql)
  println(select(TestUser("1", "lala")))
}

