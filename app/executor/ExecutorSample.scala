package executor

import akka.actor.ActorSystem

import java.time.LocalDateTime
import java.util.concurrent.{ExecutorService, Executors}
import scala.collection.parallel.CollectionConverters.ImmutableIterableIsParallelizable
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

  def sample4 = {

    val numSeq =
      Seq(1, 2, 3, 4, 5)
    val numParSeq = (numSeq).par
    val resultParSeq = numParSeq.map { i =>
      println(s"Thread: ${Thread.currentThread().getName}")
      i * 2
    }
    val result = resultParSeq.seq
    println(result)

    numParSeq.foreach(println(_))

    val nn = Seq(20, 10, 5, 2, 1)
    val rr = nn.reduce(_ - _)
    val rr1 = nn.par.reduce(_ - _)
    println(rr, rr1)

  }

  /** Futureの例外補足
    */
  def sample5 = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Future {
      1 / 0
    }.recover { case _: ArithmeticException =>
      println("ArithmeticException")
    }
  }

  /** よくあるミス
   * Futureが
    */
  def sample6 = {
    import scala.concurrent.ExecutionContext.Implicits.global
    try {
      Future { 1 / 0 }
    } catch {
      case _: ArithmeticException => println("ArithmeticException")
    }
  }

  val myExecutorService = Executors.newFixedThreadPool(3)
  ExecutionContext.fromExecutorService(myExecutorService)

  val myExecutor = Executors.newFixedThreadPool(3)
  ExecutionContext.fromExecutor(myExecutor)

}
