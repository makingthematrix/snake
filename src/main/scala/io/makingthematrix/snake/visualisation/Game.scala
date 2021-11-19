package io.makingthematrix.snake.visualisation

import com.almasb.fxgl.app.{ApplicationMode, GameApplication, GameSettings}
import com.almasb.fxgl.dsl.FXGL
import com.wire.signals.Signal
import com.wire.signals.ui.UiDispatchQueue
import io.makingthematrix.snake.Arguments
import io.makingthematrix.snake.visualisation.examples.SnakeWorld
import javafx.application.Platform
import javafx.scene.input.KeyCode

final class Game(args: Arguments) extends GameApplication {

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

  override def initSettings(gameSettings: GameSettings): Unit = {
    gameSettings.setWidth(args.windowSize)
    gameSettings.setHeight(args.windowSize)
    gameSettings.set3D(false)
    gameSettings.setApplicationMode(ApplicationMode.RELEASE)
    gameSettings.setGameMenuEnabled(true)
    gameSettings.setPixelsPerMeter(args.scale)
    gameSettings.setScaleAffectedOnResize(false)
  }

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
  }
}
