package io.makingthematrix.snake.visualisation.examples

import com.almasb.fxgl.dsl.FXGL
import io.makingthematrix.snake.Arguments
import io.makingthematrix.snake.engine.fields.{Dir2D, Pos2D}
import io.makingthematrix.snake.engine.{Automaton, GlobalUpdateStrategy, UpdateStrategy}
import io.makingthematrix.snake.examples.Snake.{Body, Head, Tail, TurnRight, TurnLeft}
import io.makingthematrix.snake.examples.{Snake, SnakeGlobal}
import io.makingthematrix.snake.visualisation.{UserEvent, World}
import io.makingthematrix.snake.visualisation.UserEvent.{MoveUp, MoveDown, MoveLeft, MoveRight}
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color

final class SnakeWorld(override protected val args: Arguments) extends World[Snake, SnakeGlobal] {
  override protected val auto: Automaton[Snake, SnakeGlobal] =
    Snake.automaton(
      dim = args.dim,
      updateStrategy = UpdateStrategy.onlySelf[Snake],
      globalUpdateStrategy = GlobalUpdateStrategy.onlyEvents[Snake, SnakeGlobal]
    )

  override protected def toColor(c: Snake): Color = c.cellType match {
    case Snake.Empty      => Color.WHITE
    case Snake.Treat      => Color.ORANGE
    case Snake.Head(_)    => Color.BROWN
    case Snake.Body(_, _) => Color.BLACK
    case Snake.Tail(_)    => Color.BLACK
  }

  override protected def processUserEvent(event: UserEvent): Unit = {
    val headDir = auto.globalCell.headDir
    val turn: Option[Snake.GlobalEvent] = event match {
      case MoveUp    if headDir == Dir2D.Right => Some(TurnLeft)
      case MoveUp    if headDir == Dir2D.Left  => Some(TurnRight)
      case MoveDown  if headDir == Dir2D.Right => Some(TurnRight)
      case MoveDown  if headDir == Dir2D.Left  => Some(TurnLeft)
      case MoveLeft  if headDir == Dir2D.Up    => Some(TurnLeft)
      case MoveLeft  if headDir == Dir2D.Down  => Some(TurnRight)
      case MoveRight if headDir == Dir2D.Up    => Some(TurnRight)
      case MoveRight if headDir == Dir2D.Down  => Some(TurnLeft)
      case _ => None
    }
    turn.foreach(auto.eventHub ! _)
  }

  override def init(): Unit = {
    super.init()
    val center = Pos2D(args.dim / 2, args.dim / 2)
    auto.updateCell(center) { _.copy(cellType = Body(Dir2D.Right, Dir2D.Left)) }
    auto.updateCell(center.move(Dir2D.Left)) { _.copy(cellType = Tail(Dir2D.Right)) }
    auto.updateCell(center.move(Dir2D.Right)) { _.copy(cellType = Head(Dir2D.Right)) }

    FXGL.onKeyUp(KeyCode.UP,    () => onUserEvent ! MoveUp)
    FXGL.onKeyUp(KeyCode.DOWN,  () => onUserEvent ! MoveDown)
    FXGL.onKeyUp(KeyCode.LEFT,  () => onUserEvent ! MoveLeft)
    FXGL.onKeyUp(KeyCode.RIGHT, () => onUserEvent ! MoveRight)
  }

  override def next(): Boolean =
    if (auto.globalCell.gameOver) {
      false
    } else
      super.next()
}
