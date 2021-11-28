package io.makingthematrix.snake.game

final case class Arguments(dim:     Int = 40,        // the number of cells in one dimension of the automaton
                           step:    Int = 1,          // how often the visualisation should be refresh
                           scale:   Int = 16,          // how many pixels per cell in the visualisation
                           delay:   Long = 60L,        // interval between two updates in milliseconds
                           enforceGC: Boolean = false, // enforce garbage collection every turn
                          ) {
  lazy val windowSize: Int = dim * scale
}

object Arguments {
  def parseArguments(args: Seq[String]): Arguments =
    if (args.size < 2) Arguments()
    else
      args.flatMap(_.split('=')).sliding(2, 2).foldLeft(Arguments()){
        case (acc, Seq("dim", value))         => acc.copy(dim   = Integer.parseInt(value))
        case (acc, Seq("step", value))        => acc.copy(step  = Integer.parseInt(value))
        case (acc, Seq("scale", value))       => acc.copy(scale = Integer.parseInt(value))
        case (acc, Seq("delay", value))       => acc.copy(delay = Integer.parseInt(value).toLong)
        case (acc, Seq("enforcegc", "true"))  => acc.copy(enforceGC = true)
        case (acc, Seq("enforcegc", "false")) => acc.copy(enforceGC = false)
        case (acc, Seq(name, value)) =>
          println(s"unrecognized parameter: $name with value $value"); acc
        case (acc, _) =>
          println(s"something went wrong"); acc
      }
}
