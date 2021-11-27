package io.makingthematrix.snake

import com.wire.signals.ui.UiDispatchQueue
import com.wire.signals.{CancellableFuture, Signal}
import io.makingthematrix.snake.visualisation.examples.SnakeWorld
import io.makingthematrix.snake.visualisation.{GameContract, GameState, UserEvent}
import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.VBox
import javafx.stage.{Stage, WindowEvent}

import scala.jdk.CollectionConverters._
import scala.concurrent.duration._

final class Main extends Application {
  import com.wire.signals.Threading.defaultContext

  private var args = Option.empty[Arguments]
  private var world = Option.empty[GameContract]

  override def start(stage: Stage): Unit = {
    val args = Arguments.parseArguments(getParameters.getRaw.asScala.toSeq)
    this.args = Some(args)
    val world = new SnakeWorld(args)
    this.world = Some(world)
    stage.setScene(new Scene(new VBox(world.canvas), args.windowSize, args.windowSize))
    stage.show()

    UiDispatchQueue.setUi(Platform.runLater)
    world.init()

    stage.setOnCloseRequest((_: WindowEvent) => gameState ! GameState.End)
    stage.addEventFilter(KeyEvent.KEY_RELEASED, (t: KeyEvent) => t.getCode match {
      case KeyCode.SPACE =>
        gameState.mutate {
          case GameState.Pause => GameState.Play
          case GameState.Play => GameState.Pause
          case other => other
        }
      case KeyCode.UP => world.onUserEvent ! UserEvent.MoveUp
      case KeyCode.DOWN => world.onUserEvent ! UserEvent.MoveDown
      case KeyCode.LEFT => world.onUserEvent ! UserEvent.MoveLeft
      case KeyCode.RIGHT => world.onUserEvent ! UserEvent.MoveRight
      case _ =>
    })

    CancellableFuture.delayed(5.seconds){
      println("Let's play!")
      gameState ! GameState.Play
    }
  }

  private val gameState = Signal[GameState](GameState.Pause)
  gameState.foreach {
    case GameState.Play => run()
    case GameState.End => endGame()
    case _ =>
  }

  private def run(): Unit = (world, args) match {
    case (Some(w), Some(a)) =>
      while (gameState.currentValue.contains(GameState.Play)) {
        if (!w.next()) gameState ! GameState.End
        if (a.delay > 0L) Thread.sleep(a.delay)
      }
    case _ =>
  }

  private def endGame(): Unit = {
    Platform.runLater(() => Platform.exit())
  }
}

object Main extends App {
  Application.launch(classOf[Main], args: _*)
}
