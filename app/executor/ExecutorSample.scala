package executor

import akka.actor.ActorSystem

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
