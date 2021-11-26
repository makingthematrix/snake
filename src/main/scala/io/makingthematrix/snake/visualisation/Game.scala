package io.makingthematrix.snake.visualisation

import com.wire.signals.Signal
import io.makingthematrix.snake.Arguments
import io.makingthematrix.snake.visualisation.examples.SnakeWorld
import javafx.application.Platform

final class Game(args: Arguments) {

  private lazy val world = new SnakeWorld(args)

  private val gameState = Signal[GameState](GameState.Pause)
  gameState.foreach {
    case GameState.Play => run()
    case GameState.End => endGame()
    case _ =>
  }

  private def run(): Unit =
    while(gameState.currentValue.contains(GameState.Play)) {
      if (!world.next()) gameState ! GameState.End
      if (args.delay > 0L) Thread.sleep(args.delay)
    }

  private def endGame(): Unit = {
    Platform.runLater(() => Platform.exit())
  }
  /*
  override protected def initUI(): Unit = {
    UiDispatchQueue.setUi(Platform.runLater)
    world.init()
  }

  override def initInput(): Unit = {
   FXGL.onKeyUp(KeyCode.SPACE, () => gameState.mutate {
      case GameState.Pause => GameState.Play
      case GameState.Play  => GameState.Pause
      case other => other
    })
  }*/
}
