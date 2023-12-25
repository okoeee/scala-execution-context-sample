package executor

import akka.actor.ActorSystem

import java.time.LocalDateTime
import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.duration.Duration
import scala.concurrent.{
  Await,
  ExecutionContext,
  ExecutionContextExecutor,
  Future
}

object ExecutorSample {

  val system = ActorSystem()
  val blokingDispatcher = system.dispatchers.lookup("blocking-dispatcher")

  val myExecutors: ExecutorService = Executors.newFixedThreadPool(3)
  val myExecutionContext: ExecutionContextExecutor =
    ExecutionContext.fromExecutor(myExecutors)

  def conflictExec(): Unit = {
    var i = 0
    val thread1 = new Thread(() => {
      (1 to 100).foreach { _ =>
        i.synchronized { i += 1 }
      }
    })
    val thread2 = new Thread(() => {
      (1 to 100).foreach { _ =>
        i.synchronized { i += 1 }
      }
    })
    thread1.start()
    thread2.start()
    thread1.join()
    thread2.join()
    println(i)
    // 結果: 200
  }

  def deadlockExec(): Unit = {
    val i = "hoge"
    val j = "fuga"

    val thread1 = new Thread(() => {
      println("thread1 start")
      i.synchronized { // iのロックを取得
        Thread.sleep(1000)
        j.synchronized {
          println("thread1 j synchronized")
        }
      }
    })
    val thread2 = new Thread(() => {
      println("thread2 start")
      j.synchronized { // jのロックを取得
        Thread.sleep(1000)
        i.synchronized {
          println("thread1 i synchronized")
        }
      }
    })

    thread1.start()
    thread2.start()
    thread1.join()
    thread2.join()

    // 結果
    // thread1 start
    // thread2 start
    // ここで終わり
  }

  private def printThreadNameWith(msg: String): Unit = {
    val now = LocalDateTime.now()
    val threadName = Thread.currentThread().getName
    println(s"$now - [$threadName] - $msg")
  }

  private def execFutureTask(
    msg: String
  )(ex: ExecutionContext): Future[Unit] = {
    Future {
      printThreadNameWith(s"$msg task executed")
    }(ex)
  }

  def futureSample1(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.{global => ex}
    printThreadNameWith("Method start")
    execFutureTask("Future1")(ex)
    execFutureTask("Future2")(ex)
  }

  def newLine: Unit = println("""
      |==============================================================
      |
      |""".stripMargin)

  def sample1 = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val f = Future {
      Thread.sleep(1000)
      println("Future complete: thread = " + Thread.currentThread().getName)
    }

    println("Main sleep: thread = " + Thread.currentThread().getName)
    Await.ready(f, Duration.Inf)
  }

  def sample2 = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val start = System.currentTimeMillis()

    val f = Future.traverse((1 to 100).toList) { i =>
      Future {
        Thread.sleep(100)
        println(
          s"""
             |Future complete: thread = ${Thread
              .currentThread()
              .getName}, Index = $i
             |""".stripMargin
        )
      }(myExecutionContext)
    }

    Await.ready(f, Duration.Inf)
    println("Total time: " + (System.currentTimeMillis() - start))
  }

  def sample3 = {
    println(s"Main Thread: ${Thread.currentThread().getName}")
    new Thread(new Runnable {
      override def run(): Unit = {
        println(s"Other Thread: ${Thread.currentThread().getName}")
      }
    }).start()
  }

}
