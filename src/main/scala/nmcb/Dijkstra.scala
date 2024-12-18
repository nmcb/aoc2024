package nmcb

import predef.*

import scala.annotation.*

object Dijkstra:

  final case class Edge[A](from: A, to: A, weight: Int):
    def inverse: Edge[A] = Edge(to, from, weight)

  final case class Graph[A](neighbours: Map[A,Vector[Edge[A]]] = Map.empty):

    def add(edge: Edge[A]): Graph[A] =
      Graph(neighbours.updated(edge.from, neighbours.getOrElse(edge.from, Vector.empty) :+ edge))

    def bidirectional(edge: Edge[A]): Graph[A] =
      add(edge).add(edge.inverse)

  object Graph:

    def empty[A]: Graph[A] =
      Graph(Map.empty)

    def fromGrid[A](grid: Grid[A], node: A, dist: (A,A) => Int = (_: A, _: A) => 1): Graph[Pos] =
      grid.elements.filter(_.element == node).foldLeft(Graph.empty): (graph, from) =>
        from.pos.adjWithinGrid(grid).filter(grid.contains(_, node))
          .foldLeft(graph): (graph, to) =>
            graph.add(Edge(from.pos, to, dist(from.element, grid.peek(to))))

  case class Result[A](edgeTo: Map[A,Edge[A]], distancesTo: Map[A,Int]):

    def pathTo(node: A): Vector[Edge[A]] =
      @tailrec
      def build(node: A, edges: Vector[Edge[A]] = Vector.empty): Vector[Edge[A]] =
        edgeTo.get(node) match
          case Some(edge) => build(edge.from, edge +: edges)
          case None       => edges
      if hasEdge(node) then build(node) else Vector.empty

    def hasEdge(node: A): Boolean =
      distancesTo.get(node).map(_ < Int.MaxValue).isDefined

    def distanceTo(node: A): Option[Int] =
      distancesTo.get(node).filter(_ < Int.MaxValue)

  import scala.collection.mutable

  def run[A](graph: Graph[A], from: A): Result[A] =
    val edgeTo: mutable.Map[A,Edge[A]] = mutable.Map.empty
    val distTo: mutable.Map[A,Int]     = mutable.Map.from(graph.neighbours.map((node,_) => node -> Int.MaxValue))

    distTo += from -> 0
    val sourceEdge = from -> distTo(from)
    val queue = mutable.PriorityQueue[(A,Int)](sourceEdge)(Ordering.by[(A,Int),Int](_._2).reverse)

    while (queue.nonEmpty)
      val (minDistNode, _) = queue.dequeue()
      val edges = graph.neighbours.getOrElse(minDistNode, Vector.empty)

      edges.foreach: edge =>
        if distTo(edge.to) > distTo(edge.from) + edge.weight then
          distTo.update(edge.to, distTo(edge.from) + edge.weight)
          edgeTo.update(edge.to, edge)
          if !queue.exists((node,_) => node == edge.to) then
            queue.enqueue((edge.to, distTo(edge.to)))

    Result(edgeTo.toMap, distTo.toMap)

  extension [A](path: Vector[Edge[A]])
    def toTrail: Vector[A] =
      if path.isEmpty then
        Vector.empty
      else
        path.foldLeft(Vector.empty[A])(_ :+ _.from) :+ path.last.to
