/** This software is released under the University of Illinois/Research and Academic Use License. See
  * the LICENSE file in the root folder for details. Copyright (c) 2016
  *
  * Developed by: The Cognitive Computations Group, University of Illinois at Urbana-Champaign
  * http://cogcomp.cs.illinois.edu/
  */
package edu.illinois.cs.cogcomp.saul.datamodel

import edu.illinois.cs.cogcomp.core.datastructures.vectors.{ ExceptionlessInputStream, ExceptionlessOutputStream }
import edu.illinois.cs.cogcomp.saul.datamodel.edge.{ AsymmetricEdge, Edge, Link, SymmetricEdge }
import edu.illinois.cs.cogcomp.saul.datamodel.node.{ JoinNode, Node, NodeProperty }
import edu.illinois.cs.cogcomp.saul.datamodel.property.features.discrete._
import edu.illinois.cs.cogcomp.saul.datamodel.property.features.real._
import edu.illinois.cs.cogcomp.saul.datamodel.property.{ EvaluatedProperty, Property }
import edu.illinois.cs.cogcomp.saul.util.Logging

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

/** Represents the data model that stores the data object graph. Extend this trait to define nodes and edges for
  * representing data for a learning problem.
  */
trait DataModel extends Logging {
  val PID = 'PID

  final val nodes = new ListBuffer[Node[_]]
  final val properties = new ListBuffer[NodeProperty[_]]
  final val edges = new ListBuffer[Edge[_, _]]

  // TODO: Implement this function.
  def select[T <: AnyRef](node: Node[T], conditions: EvaluatedProperty[T, _]*): List[T] = {
    val conds = conditions.toList
    node.getAllInstances.filter({
      t =>
        conds.exists({
          cond => cond.property.sensor(t).equals(cond.value)
        })
    }).toList
  }

  def clearInstances(): Unit = {
    nodes.foreach(_.clear())
    edges.foreach(_.clear())
  }

  def addFromModel[T <: DataModel](dataModel: T): Unit = {
    assert(this.nodes.size == dataModel.nodes.size)
    for ((n1, n2) <- nodes.zip(dataModel.nodes)) {
      n1.populateFrom(n2)
    }
    assert(this.edges.size == dataModel.edges.size)
    for ((e1, e2) <- edges.zip(dataModel.edges)) {
      e1.populateFrom(e2)
    }
  }

  @deprecated("Use node.properties to get the properties for a specific node")
  def getPropertiesForType[T <: AnyRef](implicit tag: ClassTag[T]): List[Property[T]] = {
    this.properties.filter(a => a.tag.equals(tag)).map(_.asInstanceOf[Property[T]]).toList
  }

  @deprecated("Use node.populate() instead.")
  def populate[T <: AnyRef](node: Node[T], coll: Seq[T]) = {
    node.populate(coll)
  }

  @deprecated
  def getNodeWithType[T <: AnyRef](implicit tag: ClassTag[T]): Node[T] = {
    this.nodes.filter {
      e: Node[_] => tag.equals(e.tag)
    }.head.asInstanceOf[Node[T]]
  }

  @deprecated
  def getFromRelation[FROM <: AnyRef, NEED <: AnyRef](t: FROM)(implicit tag: ClassTag[FROM], headTag: ClassTag[NEED]): Iterable[NEED] = {
    val dm = this
    if (tag.equals(headTag)) {
      Set(t.asInstanceOf[NEED])
    } else {
      val r = this.edges.filter {
        r => r.from.tag.toString.equals(tag.toString) && r.to.tag.toString.equals(headTag.toString)
      }
      if (r.isEmpty) {
        // reverse search
        val r = this.edges.filter {
          r => r.to.tag.toString.equals(tag.toString) && r.from.tag.toString.equals(headTag.toString)
        }
        if (r.isEmpty) {
          throw new Exception(s"Failed to found relations between $tag to $headTag")
        } else r flatMap (_.asInstanceOf[Edge[NEED, FROM]].backward.neighborsOf(t)) distinct
      } else r flatMap (_.asInstanceOf[Edge[FROM, NEED]].forward.neighborsOf(t)) distinct
    }
  }

  // TODO: comment this function
  @deprecated
  def getFromRelation[T <: AnyRef, HEAD <: AnyRef](name: Symbol, t: T)(implicit tag: ClassTag[T], headTag: ClassTag[HEAD]): Iterable[HEAD] = {
    if (tag.equals(headTag)) {
      List(t.asInstanceOf[HEAD])
    } else {
      val r = this.edges.filter {
        r =>
          r.from.tag.equals(tag) && r.to.tag.equals(headTag) && r.forward.name.isDefined && name.equals(r.forward.name.get)
      }

      // there must be only one such relation
      if (r.isEmpty) {
        throw new Exception(s"Failed to find any relation between $tag to $headTag")
      } else if (r.size > 1) {
        throw new Exception(s"Found too many relations between $tag to $headTag,\nPlease specify a name")
      } else {
        r.head.forward.asInstanceOf[Link[T, HEAD]].neighborsOf(t)
      }
    }
  }

  @deprecated
  def getRelatedFieldsBetween[T <: AnyRef, U <: AnyRef](implicit fromTag: ClassTag[T], toTag: ClassTag[U]): Iterable[Link[T, U]] = {
    this.edges.filter(r => r.from.tag.equals(fromTag) && r.to.tag.equals(toTag)).map(_.forward.asInstanceOf[Link[T, U]]) ++
      this.edges.filter(r => r.to.tag.equals(fromTag) && r.from.tag.equals(toTag)).map(_.backward.asInstanceOf[Link[T, U]])
  }

  /** node definitions */
  def node[T <: AnyRef](implicit tag: ClassTag[T]): Node[T] = node((x: T) => x)

  def node[T <: AnyRef](keyFunc: T => Any)(implicit tag: ClassTag[T]): Node[T] = {
    val n = new Node[T](keyFunc, tag)
    nodes += n
    n
  }

  def join[A <: AnyRef, B <: AnyRef](a: Node[A], b: Node[B])(matcher: (A, B) => Boolean)(implicit tag: ClassTag[(A, B)]): Node[(A, B)] = {
    val n = new JoinNode(a, b, matcher, tag)
    a.joinNodes += n

    // If nodes `a` and `b` are the same Node type, do not double-count join nodes.
    if (b != a) {
      b.joinNodes += n
    }
    nodes += n
    n
  }

  /** edges */
  def edge[A <: AnyRef, B <: AnyRef](a: Node[A], b: Node[B], name: Symbol = 'default): Edge[A, B] = {
    val e = AsymmetricEdge(new Link(a, b, Some(name)), new Link(b, a, Some(Symbol("-" + name.name))))
    a.outgoing += e
    b.incoming += e
    edges += e
    e
  }

  def symmEdge[A <: AnyRef](a: Node[A], b: Node[A], name: Symbol = 'default): Edge[A, A] = {
    val e = SymmetricEdge(new Link(a, b, Some(name)))
    a.incoming += e
    a.outgoing += e
    b.incoming += e
    b.outgoing += e
    edges += e
    e
  }


  /**
    * Helper class to facilitite creating new Property instances.
    *
    * Note:
    * 1) The `cache` parameter is used to cache a Property value within a single training iteration. It's use-case is
    *    primarily for scenarios where the property value depends on a recursive Learnable evaluation.
    * 2) The `isStatic` parameter is used to cache a Property value which is not expected to change during the entire
    *    training process.
    * 3) Marking a property as `isStatic` supersedes the `cache` preference. Static Properties are always cached
    *    regardless of the value of `cache` parameter.
    *
    * @param node [[Node]] instance to add the current property to.
    * @param name Name of the property.
    * @param cache Boolean indicating if this property should be cached during training.
    * @param ordered Denoting if the order among the values in this property needs to be preserved. Only applies to
    *                collection-based properties.
    * @param isStatic Boolean indicating if this property has a static value, which does not change during training.
    *                 Caching static properties saves redundant calculation of the Property's value.
    * @tparam T Data type of the value represented by the property. This is inferred from the [[Node]] instance.
    * @return [[PropertyApply]]
    */
  class PropertyApply[T <: AnyRef] private[DataModel] (val node: Node[T],
                                                       name: String,
                                                       cache: Boolean,
                                                       ordered: Boolean,
                                                       isStatic: Boolean) {
    papply =>

    def apply(f: T => Boolean)(implicit tag: ClassTag[T]): BooleanProperty[T] = {
      val a = new BooleanProperty[T](name, f) with NodeProperty[T] {
        override def node: Node[T] = papply.node
        override val isCacheable: Boolean = isStatic || cache
      }

      // Property value will only be cached during a single training/testing iteration
      if (cache && !isStatic) node.perIterationCachePropertyList.append(a)

      papply.node.properties += a
      properties += a
      a
    }

    def apply(f: T => List[Int])(implicit tag: ClassTag[T], d: DummyImplicit): RealPropertyCollection[T] = {
      val newF: T => List[Double] = { t => f(t).map(_.toDouble) }
      val a = if (ordered) {
        new RealArrayProperty[T](name, newF) with NodeProperty[T] {
          override def node: Node[T] = papply.node
          override val isCacheable: Boolean = isStatic || cache
        }
      } else {
        new RealGenProperty[T](name, newF) with NodeProperty[T] {
          override def node: Node[T] = papply.node
          override val isCacheable: Boolean = isStatic || cache
        }
      }

      // Property value will only be cached during a single training/testing iteration
      if (cache && !isStatic) node.perIterationCachePropertyList.append(a)

      papply.node.properties += a
      properties += a
      a
    }

    /** Discrete sensor feature with range, same as real name in lbjava */
    def apply(f: T => Int)(implicit tag: ClassTag[T], d1: DummyImplicit, d2: DummyImplicit): RealProperty[T] = {
      val newF: T => Double = { t => f(t).toDouble }
      val a = new RealProperty[T](name, newF) with NodeProperty[T] {
        override def node: Node[T] = papply.node
        override val isCacheable: Boolean = isStatic || cache
      }

      // Property value will only be cached during a single training/testing iteration
      if (cache && !isStatic) node.perIterationCachePropertyList.append(a)

      papply.node.properties += a
      properties += a
      a
    }

    /** Discrete sensor feature with range, same as real% and real[] in lbjava */
    def apply(f: T => List[Double])(implicit tag: ClassTag[T], d1: DummyImplicit, d2: DummyImplicit,
      d3: DummyImplicit): RealCollectionProperty[T] = {
      val a = new RealCollectionProperty[T](name, f, ordered) with NodeProperty[T] {
        override def node: Node[T] = papply.node
        override val isCacheable: Boolean = isStatic || cache
      }

      // Property value will only be cached during a single training/testing iteration
      if (cache && !isStatic) node.perIterationCachePropertyList.append(a)

      papply.node.properties += a
      properties += a
      a
    }

    /** Discrete sensor feature with range, same as real name in lbjava */
    def apply(f: T => Double)(implicit tag: ClassTag[T], d1: DummyImplicit, d2: DummyImplicit, d3: DummyImplicit,
      d4: DummyImplicit): RealProperty[T] = {
      val a = new RealProperty[T](name, f) with NodeProperty[T] {
        override def node: Node[T] = papply.node
        override val isCacheable: Boolean = isStatic || cache
      }

      // Property value will only be cached during a single training/testing iteration
      if (cache && !isStatic) node.perIterationCachePropertyList.append(a)

      papply.node.properties += a
      properties += a
      a
    }

    /** Discrete feature without range, same as discrete SpamLabel in lbjava */
    def apply(f: T => String)(implicit tag: ClassTag[T], d1: DummyImplicit, d2: DummyImplicit, d3: DummyImplicit,
      d4: DummyImplicit, d5: DummyImplicit): DiscreteProperty[T] = {
      val a = new DiscreteProperty[T](name, f, None) with NodeProperty[T] {
        override def node: Node[T] = papply.node
        override val isCacheable: Boolean = isStatic || cache
      }

      // Property value will only be cached during a single training/testing iteration
      if (cache && !isStatic) node.perIterationCachePropertyList.append(a)

      papply.node.properties += a
      properties += a
      a
    }

    /** Discrete array feature with range, same as discrete[] and discrete% in lbjava */
    def apply(f: T => List[String])(implicit tag: ClassTag[T], d1: DummyImplicit, d2: DummyImplicit, d3: DummyImplicit,
      d4: DummyImplicit, d5: DummyImplicit, d6: DummyImplicit): DiscreteCollectionProperty[T] = {
      val a = new DiscreteCollectionProperty[T](name, f, !ordered) with NodeProperty[T] {
        override def node: Node[T] = papply.node
        override val isCacheable: Boolean = isStatic || cache
      }

      // Property value will only be cached during a single training/testing iteration
      if (cache && !isStatic) node.perIterationCachePropertyList.append(a)

      papply.node.properties += a
      properties += a
      a
    }

    /** Discrete feature with range, same as discrete{"spam", "ham"} SpamLabel in lbjava */
    def apply(range: String*)(f: T => String)(implicit tag: ClassTag[T], d1: DummyImplicit, d2: DummyImplicit, d3: DummyImplicit,
      d4: DummyImplicit, d5: DummyImplicit, d6: DummyImplicit,
      d7: DummyImplicit): DiscreteProperty[T] = {
      val r = range.toList
      val a = new DiscreteProperty[T](name, f, Some(r)) with NodeProperty[T] {
        override def node: Node[T] = papply.node
        override val isCacheable: Boolean = isStatic || cache
      }

      // Property value will only be cached during a single training/testing iteration
      if (cache && !isStatic) node.perIterationCachePropertyList.append(a)

      papply.node.properties += a
      properties += a
      a
    }
  }

  /**
    * Function to create a new [[Property]] instance inside a DataModel.
    *
    * Note:
    * 1) The `cache` parameter is used to cache a Property value within a single training iteration. It's use-case is
    *    primarily for scenarios where the property value depends on a recursive Learnable evaluation.
    * 2) The `isStatic` parameter is used to cache a Property value which is not expected to change during the entire
    *    training process.
    * 3) Marking a property as `isStatic` supersedes the `cache` preference. Static Properties are always cached
    *    regardless of the value of `cache` parameter.
    *
    * @param node [[Node]] instance to add the current property to.
    * @param name Name of the property.
    * @param cache Boolean indicating if this property should be cached during training.
    * @param ordered Denoting if the order among the values in this property needs to be preserved. Only applies to
    *                collection-based properties.
    * @param isStatic Boolean indicating if this property has a static value, which does not change during training.
    *                 Caching static properties saves redundant calculation of the Property's value.
    * @tparam T Data type of the value represented by the property. This is inferred from the [[Node]] instance.
    * @return Property instance wrapped in a helper class [[PropertyApply]].
    */
  def property[T <: AnyRef](node: Node[T],
                            name: String = "prop" + properties.size,
                            cache: Boolean = false,
                            ordered: Boolean = false,
                            isStatic: Boolean = false) =
    new PropertyApply[T](node, name, isStatic = isStatic, cache = cache, ordered = ordered)

  /** Methods for caching Data Model */
  var hasDerivedInstances = false

  def deriveInstances() = {
    nodes.foreach { node =>
      val relatedProperties = properties.filter(property => property.tag.equals(node.tag)).toList
      node.deriveInstances(relatedProperties)
    }
    edges.foreach { edge =>
      edge.deriveIndexWithIds()
    }
    hasDerivedInstances = true
  }

  val defaultDIFilePath = "models/" + getClass.getCanonicalName + ".di"

  def write(filePath: String = defaultDIFilePath) = {
    val out = ExceptionlessOutputStream.openCompressedStream(filePath)

    out.writeInt(nodes.size)
    nodes.zipWithIndex.foreach {
      case (node, nodeId) =>
        out.writeInt(nodeId)
        node.writeDerivedInstances(out)
    }

    out.writeInt(edges.size)
    edges.zipWithIndex.foreach {
      case (edge, edgeId) =>
        out.writeInt(edgeId)
        edge.writeIndexWithIds(out)
    }

    out.close()
  }

  def load(filePath: String = defaultDIFilePath) = {
    val in = ExceptionlessInputStream.openCompressedStream(filePath)

    val nodesSize = in.readInt()
    (0 until nodesSize).foreach { _ =>
      val nodeId = in.readInt()
      nodes(nodeId).loadDerivedInstances(in)
    }

    val edgesSize = in.readInt()
    (0 until edgesSize).foreach { _ =>
      val edgeId = in.readInt()
      edges(edgeId).loadIndexWithIds(in)
    }

    in.close()

    hasDerivedInstances = true
  }
}
