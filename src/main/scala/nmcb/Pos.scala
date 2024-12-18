package nmcb

import Dir.*

case class Pos(x: Int, y: Int):
  infix inline def +(p: Pos): Pos = copy(x = x + p.x, y = y + p.y)
  infix inline def -(p: Pos): Pos = copy(x = x - p.x, y = y - p.y)
  infix inline def *(i: Int): Pos = copy(x = x * i, y = y * i)

  def move(dir: Dir): Pos =
    dir match
      case N => copy(y = y - 1)
      case E => copy(x = x + 1)
      case S => copy(y = y + 1)
      case W => copy(x = x - 1)

  def adj: Set[Pos] =
    Dir.values.map(move).toSet

  def adjWithinBounds(min: Pos, max: Pos): Set[Pos] =
    adj.filter(_.withinBounds(min, max))

  def adjWithinGrid[A](g: Grid[A]): Set[Pos] =
    adjWithinBounds(g.minPos, g.maxPos)

  def withinBounds(min: Pos, max: Pos): Boolean =
    x >= min.x & x <= max.x & y >= min.y & y <= max.y


object Pos:

  def zero: Pos = Pos(0, 0)

  extension (tuple: (Int, Int))
    def toPos: Pos = Pos(tuple._1, tuple._2)

  extension [A](it: Array[A])
    def toPos: Pos =
      assert(it.size == 2)
      Pos(it(0).toString.toInt, it(1).toString.toInt)
