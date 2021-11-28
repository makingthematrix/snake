package io.makingthematrix.snake.game

import com.wire.signals.Signal
import io.makingthematrix.snake.engine.fields.{Dir2D, Pos2D}
import io.makingthematrix.snake.engine.{Automaton, GlobalUpdateStrategy, UpdateStrategy}
import io.makingthematrix.snake.game.Snake._
import io.makingthematrix.snake.game.UserEvent.{MoveDown, MoveLeft, MoveRight, MoveUp}
import javafx.scene.paint.Color

import scala.util.chaining.scalaUtilChainingOps

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
    moveDirection ! (event match {
      case MoveUp    => Some(Dir2D.Up)
      case MoveDown  => Some(Dir2D.Down)
      case MoveLeft  => Some(Dir2D.Left)
      case MoveRight => Some(Dir2D.Right)
      case UserEvent(Some(to), UserEventType.Drag(from)) => Some(from.dir(to).approx4)
      case _ => None
    })
  }

  private val moveDirection = Signal(Option.empty[Dir2D])
  moveDirection.foreach {
    case Some(dir) =>
      dir.crossZ(auto.globalCell.headDir) match {
        case cross if cross > 0 => auto.eventHub ! TurnLeft
        case cross if cross < 0 => auto.eventHub ! TurnRight
      }
    case _ =>
  }

  override def init(): Unit = {
    super.init()
    val center = Pos2D(args.dim / 2, args.dim / 2)
    auto.updateCell(center) { _.copy(cellType = Body(Dir2D.Right, Dir2D.Left)) }
    auto.updateCell(center.move(Dir2D.Left)) { _.copy(cellType = Tail(Dir2D.Right)) }
    auto.updateCell(center.move(Dir2D.Right)) { _.copy(cellType = Head(Dir2D.Right)) }
  }

  override def next(): Boolean =
    if (auto.globalCell.gameOver) {
      false
    } else
      super.next()
}
