package io.makingthematrix.snake

import javafx.application.Application
import javafx.stage.Stage

import scala.jdk.CollectionConverters._

final class Main extends Application {
  override def start(stage: Stage): Unit = {
    val args = Arguments.parseArguments(getParameters.getRaw.asScala.toSeq)
    //stage.setScene(new Scene(GameApplication.embeddedLaunch(new Game(args)), args.windowSize, args.windowSize))
    stage.show()
  }
}

object Main extends App {
  Application.launch(classOf[Main], args: _*)
}
