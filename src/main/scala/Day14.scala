import scala.io.*
import scala.annotation.*

object Day14 extends App:

  val day: String =
    this.getClass.getName.drop(3).init

  case class Pos(x: Int, y: Int):

    infix def +(that: Pos): Pos =
      Pos(x + that.x, y + that.y)

    def neighbourOf(p: Pos): Boolean =
      List(copy(x = x - 1), copy(x = x + 1), copy(y = y - 1), copy(y = y +1)).contains(p)

  case class Robot(p: Pos, v: Pos)

  val input: Vector[Robot] =
    Source.fromResource(s"input$day.txt").getLines.toVector.map:
      case s"p=$px,$py v=$vx,$vy" => Robot(Pos(px.toInt, py.toInt), Pos(vx.toInt, vy.toInt))

  case class Space(robots: Vector[Robot], sizeX: Int, sizeY: Int):

    lazy val robotsByPos: Map[Pos,Vector[Robot]] =
      robots.groupMap(_.p)(identity)

    def render(p: Pos): String =
      robotsByPos.get(p).map(_.size.toString).getOrElse(".")

    override def toString: String =
      tiles.grouped(sizeX).map(_.map(render).mkString("")).mkString("\n")

    def tiles: IndexedSeq[Pos] =
      for {
        y <- 0 until sizeY
        x <- 0 until sizeX
      } yield Pos(x, y)

    def move(r: Robot): Robot =
      val n = r.p + r.v
      val nx = if n.x < 0 then sizeX + n.x else if n.x >= sizeX then n.x - sizeX else n.x
      val ny = if n.y < 0 then sizeY + n.y else if n.y >= sizeY then n.y - sizeY else n.y
      Robot(Pos(nx, ny), r.v)

    def next: Space =
      copy(robots = robots.map(move))

    def robotsIn(min: Pos, max: Pos): Vector[Robot] =
      robots.filter(r => r.p.x >= min.x & r.p.x <= max.x & r.p.y >= min.y & r.p.y <= max.y)

    val mid = Pos(sizeX / 2, sizeY / 2)
    def robotsInQ1: Vector[Robot] = robotsIn(Pos(0, 0), Pos(mid.x - 1, mid.y - 1))
    def robotsInQ2: Vector[Robot] = robotsIn(Pos(mid.x + 1, 0), Pos(sizeX - 1, mid.y - 1))
    def robotsInQ3: Vector[Robot] = robotsIn(Pos(0, mid.y + 1), Pos(mid.x - 1, sizeY - 1))
    def robotsInQ4: Vector[Robot] = robotsIn(Pos(mid.x + 1, mid.y+1), Pos(sizeX - 1, sizeY - 1))

    def safetyFactor: Long =
      List(robotsInQ1, robotsInQ2, robotsInQ3, robotsInQ4).map(_.size.toLong).product

  val start1: Long  = System.currentTimeMillis
  val answer1: Long = (0 until 100).foldLeft(Space(input, sizeX = 101, sizeY = 103))((s,_) => s.next).safetyFactor
  println(s"Answer day $day part 1: $answer1 [${System.currentTimeMillis - start1}ms]")

  def connected(space: Space): Set[Set[Pos]] =
    @tailrec
    def loop(todo: Vector[Pos], result: Set[Set[Pos]]): Set[Set[Pos]] =
      if todo.isEmpty then
        result
      else
        val remove = result.filter(_.exists(_.neighbourOf(todo.head)))
        val add    = remove.foldLeft(Set.empty[Pos])(_ ++ _) + todo.head
        loop(todo.tail, result = result -- remove + add)

    val todo  = space.robotsByPos.keys.toVector
    val start = Set(Set(todo.head))
    loop(todo.tail, start)

  val start2: Long = System.currentTimeMillis
  val answer2: Int =
    val (_, iterations) = Iterator
      .iterate(Space(input, sizeX = 101, sizeY = 103))(_.next)
      .zipWithIndex
      .find((s,t) => connected(s).maxBy(_.size).size > 50)
      .get
    iterations

  println(s"Answer day $day part 2: $answer2 [${System.currentTimeMillis - start2}ms]")

