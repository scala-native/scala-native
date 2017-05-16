/*                     __                                               *\
**     ________ ___   / /  ___      __ ____  Scala.js Benchmarks        **
**    / __/ __// _ | / /  / _ | __ / // __/  (c) 2013, Jonas Fonseca    **
**  __\ \/ /__/ __ |/ /__/ __ |/_// /_\ \                               **
** /____/\___/_/ |_/____/_/ | |__/ /____/                               **
**                          |/____/                                     **
\*                                                                      */

// Copyright 2011 Google Inc. All Rights Reserved.
// Copyright 1996 John Maloney and Mario Wolczko
//
// This file is part of GNU Smalltalk.
//
// GNU Smalltalk is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2, or (at your option) any later version.
//
// GNU Smalltalk is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
// details.
//
// You should have received a copy of the GNU General Public License along with
// GNU Smalltalk; see the file COPYING.  If not, write to the Free Software
// Foundation, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
//
// Translated first from Smalltalk to JavaScript, and finally to
// Dart by Google 2008-2010.
// Translated to Scala.js by Jonas Fonseca 2013

package deltablue

/**
 * A Scala implementation of the DeltaBlue constraint-solving
 * algorithm, as described in:
 *
 * "The DeltaBlue Algorithm: An Incremental Constraint Hierarchy Solver"
 *   Bjorn N. Freeman-Benson and John Maloney
 *   January 1990 Communications of the ACM,
 *   also available as University of Washington TR 89-08-06.
 *
 * Beware: this benchmark is written in a grotesque style where
 * the constraint model is built by side-effects from constructors.
 * I've kept it this way to avoid deviating too much from the original
 * implementation.
 */
import benchmarks.{BenchmarkRunningTime, MediumRunningTime}

import scala.collection.mutable.{ArrayBuffer, ListBuffer, Stack}

class DeltaBlueBenchmark extends benchmarks.Benchmark[Unit] {

  override val runningTime: BenchmarkRunningTime = MediumRunningTime

  override def run(): Unit = {
    chainTest(100)
    projectionTest(100)
  }

  override def check(t: Unit): Boolean =
    true

  /**
   * This is the standard DeltaBlue benchmark. A long chain of equality
   * constraints is constructed with a stay constraint on one end. An
   * edit constraint is then added to the opposite end and the time is
   * measured for adding and removing this constraint, and extracting
   * and executing a constraint satisfaction plan. There are two cases.
   * In case 1, the added constraint is stronger than the stay
   * constraint and values must propagate down the entire length of the
   * chain. In case 2, the added constraint is weaker than the stay
   * constraint so it cannot be accomodated. The cost in this case is,
   * of course, very low. Typical situations lie somewhere between these
   * two extremes.
   */
  def chainTest(n: Int) {
    implicit val planner = new Planner()
    var prev: Variable   = null
    var first: Variable  = null
    var last: Variable   = null

    // Build chain of n equality constraints.
    for (i <- 0 to n) {
      val v = new Variable("v", 0)
      if (prev != null) new EqualityConstraint(prev, v, REQUIRED)
      if (i == 0) first = v
      if (i == n) last = v
      prev = v
    }
    new StayConstraint(last, STRONG_DEFAULT)
    val edit = new EditConstraint(first, PREFERRED)
    val plan = planner.extractPlanFromConstraints(Seq(edit))
    for (i <- 0 until 100) {
      first.value = i
      plan.execute()
      if (last.value != i) {
        print("Chain test failed.\n{last.value)\n{i}")
      }
    }
  }

  /**
   * This test constructs a two sets of variables related to each
   * other by a simple linear transformation (scale and offset). The
   * time is measured to change a variable on either side of the
   * mapping and to change the scale and offset factors.
   */
  def projectionTest(n: Int) {
    implicit val planner = new Planner()
    val scale            = new Variable("scale", 10)
    val offset           = new Variable("offset", 1000)
    var src: Variable    = null
    var dst: Variable    = null

    val dests = new ArrayBuffer[Variable](n)
    for (i <- 0 until n) {
      src = new Variable("src", i)
      dst = new Variable("dst", i)
      dests += dst
      new StayConstraint(src, NORMAL)
      new ScaleConstraint(src, scale, offset, dst, REQUIRED)
    }
    change(src, 17)
    if (dst.value != 1170) print("Projection 1 failed")
    change(dst, 1050)
    if (src.value != 5) print("Projection 2 failed")
    change(scale, 5)
    for (i <- 0 until n - 1) {
      if (dests(i).value != i * 5 + 1000) print("Projection 3 failed")
    }
    change(offset, 2000)
    for (i <- 0 until n - 1) {
      if (dests(i).value != i * 5 + 2000) print("Projection 4 failed")
    }
  }

  def change(v: Variable, newValue: Int)(implicit planner: Planner) {
    val edit = new EditConstraint(v, PREFERRED)
    val plan = planner.extractPlanFromConstraints(Seq(edit))
    for (i <- 0 until 10) {
      v.value = newValue
      plan.execute()
    }
    edit.destroyConstraint
  }
}

/**
 * Strengths are used to measure the relative importance of constraints.
 * New strengths may be inserted in the strength hierarchy without
 * disrupting current constraints.  Strengths cannot be created outside
 * this class, so == can be used for value comparison.
 */
sealed class Strength(val value: Int, val name: String) {
  def nextWeaker = value match {
    case 0 => STRONG_PREFERRED
    case 1 => PREFERRED
    case 2 => STRONG_DEFAULT
    case 3 => NORMAL
    case 4 => WEAK_DEFAULT
    case 5 => WEAKEST
  }
}

case object REQUIRED         extends Strength(0, "required")
case object STRONG_PREFERRED extends Strength(1, "strongPreferred")
case object PREFERRED        extends Strength(2, "preferred")
case object STRONG_DEFAULT   extends Strength(3, "strongDefault")
case object NORMAL           extends Strength(4, "normal")
case object WEAK_DEFAULT     extends Strength(5, "weakDefault")
case object WEAKEST          extends Strength(6, "weakest")

// Compile time computed constants.
object Strength {

  def stronger(s1: Strength, s2: Strength): Boolean =
    s1.value < s2.value

  def weaker(s1: Strength, s2: Strength): Boolean =
    s1.value > s2.value

  def weakest(s1: Strength, s2: Strength) =
    if (weaker(s1, s2)) s1 else s2

  def strongest(s1: Strength, s2: Strength) =
    if (stronger(s1, s2)) s1 else s2
}

abstract class Constraint(val strength: Strength)(implicit planner: Planner) {

  def isSatisfied(): Boolean
  def markUnsatisfied(): Unit
  def addToGraph(): Unit
  def removeFromGraph(): Unit
  def chooseMethod(mark: Int): Unit
  def markInputs(mark: Int): Unit
  def inputsKnown(mark: Int): Boolean
  def output(): Variable
  def execute(): Unit
  def recalculate(): Unit

  /// Activate this constraint and attempt to satisfy it.
  def addConstraint() {
    addToGraph()
    planner.incrementalAdd(this)
  }

  /**
   * Attempt to find a way to enforce this constraint. If successful,
   * record the solution, perhaps modifying the current dataflow
   * graph. Answer the constraint that this constraint overrides, if
   * there is one, or nil, if there isn't.
   * Assume: I am not already satisfied.
   */
  def satisfy(mark: Int): Constraint = {
    chooseMethod(mark)
    if (!isSatisfied()) {
      if (strength == REQUIRED) {
        print("Could not satisfy a required constraint!")
      }
      null
    } else {
      markInputs(mark)
      val out        = output()
      val overridden = out.determinedBy
      if (overridden != null)
        overridden.markUnsatisfied()
      out.determinedBy = this
      if (!planner.addPropagate(this, mark))
        print("Cycle encountered")
      out.mark = mark
      overridden
    }
  }

  def destroyConstraint {
    if (isSatisfied())
      planner.incrementalRemove(this)
    removeFromGraph()
  }

  /**
   * Normal constraints are not input constraints.  An input constraint
   * is one that depends on external state, such as the mouse, the
   * keybord, a clock, or some arbitraty piece of imperative code.
   */
  def isInput = false
}

/**
 * Abstract superclass for constraints having a single possible output variable.
 */
abstract class UnaryConstraint(myOutput: Variable, strength: Strength)(
    implicit planner: Planner)
    extends Constraint(strength) {

  private var satisfied = false

  addConstraint()

  /// Adds this constraint to the constraint graph
  def addToGraph() {
    myOutput.addConstraint(this)
    satisfied = false
  }

  /// Decides if this constraint can be satisfied and records that decision.
  def chooseMethod(mark: Int) {
    satisfied = (myOutput.mark != mark) &&
        Strength.stronger(strength, myOutput.walkStrength)
  }

  /// Returns true if this constraint is satisfied in the current solution.
  def isSatisfied() = satisfied

  def markInputs(mark: Int) {
    // has no inputs.
  }

  /// Returns the current output variable.
  def output() = myOutput

  /**
   * Calculate the walkabout strength, the stay flag, and, if it is
   * 'stay', the value for the current output of this constraint. Assume
   * this constraint is satisfied.
   */
  def recalculate() {
    myOutput.walkStrength = strength
    myOutput.stay = !isInput
    if (myOutput.stay) execute(); // Stay optimization.
  }

  /// Records that this constraint is unsatisfied.
  def markUnsatisfied() {
    satisfied = false
  }

  def inputsKnown(mark: Int) = true

  def removeFromGraph() {
    if (myOutput != null) myOutput.removeConstraint(this)
    satisfied = false
  }
}

/**
 * Variables that should, with some level of preference, stay the same.
 * Planners may exploit the fact that instances, if satisfied, will not
 * change their output during plan execution.  This is called "stay
 * optimization".
 */
class StayConstraint(v: Variable, str: Strength)(implicit planner: Planner)
    extends UnaryConstraint(v, str) {
  def execute() {
    // Stay constraints do nothing.
  }
}

/**
 * A unary input constraint used to mark a variable that the client
 * wishes to change.
 */
class EditConstraint(v: Variable, str: Strength)(implicit planner: Planner)
    extends UnaryConstraint(v, str) {

  /// Edits indicate that a variable is to be changed by imperative code.
  override val isInput = true

  def execute() {
    // Edit constraints do nothing.
  }
}

object Direction {
  final val NONE     = 1
  final val FORWARD  = 2
  final val BACKWARD = 0
}

/**
 * Abstract superclass for constraints having two possible output
 * variables.
 */
abstract class BinaryConstraint(v1: Variable,
                                v2: Variable,
                                strength: Strength)(implicit planner: Planner)
    extends Constraint(strength) {

  protected var direction = Direction.NONE

  addConstraint()

  /**
   * Decides if this constraint can be satisfied and which way it
   * should flow based on the relative strength of the variables related,
   * and record that decision.
   */
  def chooseMethod(mark: Int) {
    if (v1.mark == mark) {
      direction =
        if ((v2.mark != mark && Strength.stronger(strength, v2.walkStrength)))
          Direction.FORWARD
        else
          Direction.NONE
    }
    if (v2.mark == mark) {
      direction =
        if (v1.mark != mark && Strength.stronger(strength, v1.walkStrength))
          Direction.BACKWARD
        else
          Direction.NONE
    }
    if (Strength.weaker(v1.walkStrength, v2.walkStrength)) {
      direction =
        if (Strength.stronger(strength, v1.walkStrength))
          Direction.BACKWARD
        else
          Direction.NONE
    } else {
      direction =
        if (Strength.stronger(strength, v2.walkStrength))
          Direction.FORWARD
        else
          Direction.BACKWARD
    }
  }

  /// Add this constraint to the constraint graph.
  override def addToGraph() {
    v1.addConstraint(this)
    v2.addConstraint(this)
    direction = Direction.NONE
  }

  /// Answer true if this constraint is satisfied in the current solution.
  def isSatisfied() = direction != Direction.NONE

  /// Mark the input variable with the given mark.
  def markInputs(mark: Int) {
    input().mark = mark
  }

  /// Returns the current input variable
  def input() = if (direction == Direction.FORWARD) v1 else v2

  /// Returns the current output variable.
  def output() = if (direction == Direction.FORWARD) v2 else v1

  /**
   * Calculate the walkabout strength, the stay flag, and, if it is
   * 'stay', the value for the current output of this
   * constraint. Assume this constraint is satisfied.
   */
  def recalculate() {
    val ihn = input()
    val out = output()
    out.walkStrength = Strength.weakest(strength, ihn.walkStrength)
    out.stay = ihn.stay
    if (out.stay) execute()
  }

  /// Record the fact that this constraint is unsatisfied.
  def markUnsatisfied() {
    direction = Direction.NONE
  }

  def inputsKnown(mark: Int): Boolean = {
    val i = input()
    i.mark == mark || i.stay || i.determinedBy == null
  }

  def removeFromGraph() {
    if (v1 != null) v1.removeConstraint(this)
    if (v2 != null) v2.removeConstraint(this)
    direction = Direction.NONE
  }
}

/**
 * Relates two variables by the linear scaling relationship: "v2 =
 * (v1 * scale) + offset". Either v1 or v2 may be changed to maintain
 * this relationship but the scale factor and offset are considered
 * read-only.
 */
class ScaleConstraint(v1: Variable,
                      scale: Variable,
                      offset: Variable,
                      v2: Variable,
                      strength: Strength)(implicit planner: Planner)
    extends BinaryConstraint(v1, v2, strength) {

  /// Adds this constraint to the constraint graph.
  override def addToGraph() {
    super.addToGraph()
    scale.addConstraint(this)
    offset.addConstraint(this)
  }

  override def removeFromGraph() {
    super.removeFromGraph()
    if (scale != null) scale.removeConstraint(this)
    if (offset != null) offset.removeConstraint(this)
  }

  override def markInputs(mark: Int) {
    super.markInputs(mark)
    scale.mark = mark
    offset.mark = mark
  }

  /// Enforce this constraint. Assume that it is satisfied.
  def execute() {
    if (direction == Direction.FORWARD) {
      v2.value = v1.value * scale.value + offset.value
    } else {
      // XXX: Truncates the resulting value
      v1.value = (v2.value - offset.value) / scale.value
    }
  }

  /**
   * Calculate the walkabout strength, the stay flag, and, if it is
   * 'stay', the value for the current output of this constraint. Assume
   * this constraint is satisfied.
   */
  override def recalculate() {
    val ihn = input()
    val out = output()
    out.walkStrength = Strength.weakest(strength, ihn.walkStrength)
    out.stay = ihn.stay && scale.stay && offset.stay
    if (out.stay) execute()
  }

}

/**
 * Constrains two variables to have the same value.
 */
class EqualityConstraint(v1: Variable, v2: Variable, strength: Strength)(
    implicit planner: Planner)
    extends BinaryConstraint(v1, v2, strength) {
  /// Enforce this constraint. Assume that it is satisfied.
  def execute() {
    output().value = input().value
  }
}

/**
 * A constrained variable. In addition to its value, it maintain the
 * structure of the constraint graph, the current dataflow graph, and
 * various parameters of interest to the DeltaBlue incremental
 * constraint solver.
 */
class Variable(val name: String, var value: Int) {

  val constraints              = new ListBuffer[Constraint]()
  var determinedBy: Constraint = null
  var mark                     = 0
  var walkStrength: Strength   = WEAKEST
  var stay                     = true

  /**
   * Add the given constraint to the set of all constraints that refer
   * this variable.
   */
  def addConstraint(c: Constraint) {
    constraints += c
  }

  /// Removes all traces of c from this variable.
  def removeConstraint(c: Constraint) {
    constraints -= c
    if (determinedBy == c) determinedBy = null
  }
}

class Planner {

  var currentMark = 0

  /**
   * Attempt to satisfy the given constraint and, if successful,
   * incrementally update the dataflow graph.  Details: If satifying
   * the constraint is successful, it may override a weaker constraint
   * on its output. The algorithm attempts to resatisfy that
   * constraint using some other method. This process is repeated
   * until either a) it reaches a variable that was not previously
   * determined by any constraint or b) it reaches a constraint that
   * is too weak to be satisfied using any of its methods. The
   * variables of constraints that have been processed are marked with
   * a unique mark value so that we know where we've been. This allows
   * the algorithm to avoid getting into an infinite loop even if the
   * constraint graph has an inadvertent cycle.
   */
  def incrementalAdd(c: Constraint) {
    val mark       = newMark()
    var overridden = c.satisfy(mark)
    while (overridden != null) overridden = overridden.satisfy(mark)
  }

  /**
   * Entry point for retracting a constraint. Remove the given
   * constraint and incrementally update the dataflow graph.
   * Details: Retracting the given constraint may allow some currently
   * unsatisfiable downstream constraint to be satisfied. We therefore collect
   * a list of unsatisfied downstream constraints and attempt to
   * satisfy each one in turn. This list is traversed by constraint
   * strength, strongest first, as a heuristic for avoiding
   * unnecessarily adding and then overriding weak constraints.
   * Assume: [c] is satisfied.
   */
  def incrementalRemove(c: Constraint) {
    val out = c.output()
    c.markUnsatisfied()
    c.removeFromGraph()
    val unsatisfied        = removePropagateFrom(out)
    var strength: Strength = REQUIRED
    do {
      for (u <- unsatisfied) {
        if (u.strength == strength) incrementalAdd(u)
      }
      strength = strength.nextWeaker
    } while (strength != WEAKEST)
  }

  /// Select a previously unused mark value.
  def newMark(): Int = {
    currentMark += 1
    currentMark
  }

  /**
   * Extract a plan for resatisfaction starting from the given source
   * constraints, usually a set of input constraints. This method
   * assumes that stay optimization is desired; the plan will contain
   * only constraints whose output variables are not stay. Constraints
   * that do no computation, such as stay and edit constraints, are
   * not included in the plan.
   * Details: The outputs of a constraint are marked when it is added
   * to the plan under construction. A constraint may be appended to
   * the plan when all its input variables are known. A variable is
   * known if either a) the variable is marked (indicating that has
   * been computed by a constraint appearing earlier in the plan), b)
   * the variable is 'stay' (i.e. it is a constant at plan execution
   * time), or c) the variable is not determined by any
   * constraint. The last provision is for past states of history
   * variables, which are not stay but which are also not computed by
   * any constraint.
   * Assume: [sources] are all satisfied.
   */
  def makePlan(sources: Stack[Constraint]) = {
    val mark = newMark()
    val plan = new Plan()
    val todo = sources
    while (!todo.isEmpty) {
      val c = todo.pop()
      if (c.output().mark != mark && c.inputsKnown(mark)) {
        plan.addConstraint(c)
        c.output().mark = mark
        addConstraintsConsumingTo(c.output(), todo)
      }
    }
    plan
  }

  /**
   * Extract a plan for resatisfying starting from the output of the
   * given [constraints], usually a set of input constraints.
   */
  def extractPlanFromConstraints(constraints: Seq[Constraint]) = {
    val sources = new Stack[Constraint]()
    for (c <- constraints) {
      // if not in plan already and eligible for inclusion.
      if (c.isInput && c.isSatisfied()) sources.push(c)
    }
    makePlan(sources)
  }

  /**
   * Recompute the walkabout strengths and stay flags of all variables
   * downstream of the given constraint and recompute the actual
   * values of all variables whose stay flag is true. If a cycle is
   * detected, remove the given constraint and answer
   * false. Otherwise, answer true.
   * Details: Cycles are detected when a marked variable is
   * encountered downstream of the given constraint. The sender is
   * assumed to have marked the inputs of the given constraint with
   * the given mark. Thus, encountering a marked node downstream of
   * the output constraint means that there is a path from the
   * constraint's output to one of its inputs.
   */
  def addPropagate(c: Constraint, mark: Int): Boolean = {
    val todo = new Stack[Constraint]().push(c)
    while (!todo.isEmpty) {
      val d = todo.pop()
      if (d.output().mark == mark) {
        incrementalRemove(c)
        return false
      }
      d.recalculate()
      addConstraintsConsumingTo(d.output(), todo)
    }
    true
  }

  /**
   * Update the walkabout strengths and stay flags of all variables
   * downstream of the given constraint. Answer a collection of
   * unsatisfied constraints sorted in order of decreasing strength.
   */
  def removePropagateFrom(out: Variable): Seq[Constraint] = {
    out.determinedBy = null
    out.walkStrength = WEAKEST
    out.stay = true
    val unsatisfied = new ListBuffer[Constraint]()
    val todo        = new Stack[Variable]().push(out)
    while (!todo.isEmpty) {
      val v = todo.pop()
      for (c <- v.constraints) {
        if (!c.isSatisfied()) unsatisfied += c
      }
      val determining = v.determinedBy
      for (next <- v.constraints) {
        if (next != determining && next.isSatisfied()) {
          next.recalculate()
          todo.push(next.output())
        }
      }
    }
    unsatisfied
  }

  def addConstraintsConsumingTo(v: Variable, coll: Stack[Constraint]) {
    val determining = v.determinedBy
    for (c <- v.constraints) {
      if (c != determining && c.isSatisfied()) coll.push(c)
    }
  }
}

/**
 * A Plan is an ordered list of constraints to be executed in sequence
 * to resatisfy all currently satisfiable constraints in the face of
 * one or more changing inputs.
 */
class Plan {
  private val list = new ListBuffer[Constraint]()

  def addConstraint(c: Constraint) {
    list += c
  }

  def execute() {
    for (constraint <- list) {
      constraint.execute()
    }
  }
}
