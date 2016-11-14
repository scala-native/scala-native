package cd

final class RedBlackTree[K <: Comparable[K], V] {
  import RedBlackTree._

  var root: Node[K, V] = null

  def put(key: K, value: V): V = {
    val insertionResult = treeInsert(key, value)
    if (!insertionResult.isNewEntry) {
      return insertionResult.oldValue
    }
    var x = insertionResult.newNode

    while (x != root && x.parent.color == Color.RED) {
      if (x.parent == x.parent.parent.left) {
        val y = x.parent.parent.right
        if (y != null && y.color == Color.RED) {
          // Case 1
          x.parent.color = Color.BLACK
          y.color = Color.BLACK
          x.parent.parent.color = Color.RED
          x = x.parent.parent
        } else {
          if (x == x.parent.right) {
            // Case 2
            x = x.parent
            leftRotate(x)
          }
          // Case 3
          x.parent.color = Color.BLACK
          x.parent.parent.color = Color.RED
          rightRotate(x.parent.parent)
        }
      } else {
        // Same as "then" clause with "right" and "left" exchanged.
        val y = x.parent.parent.left
        if (y != null && y.color == Color.RED) {
          // Case 1
          x.parent.color = Color.BLACK
          y.color = Color.BLACK
          x.parent.parent.color = Color.RED
          x = x.parent.parent
        } else {
          if (x == x.parent.left) {
            // Case 2
            x = x.parent
            rightRotate(x)
          }
          // Case 3
          x.parent.color = Color.BLACK
          x.parent.parent.color = Color.RED
          leftRotate(x.parent.parent)
        }
      }
    }

    root.color = Color.BLACK
    null.asInstanceOf[V]
  }

  def remove(key: K): V = {
    val z = findNode(key)
    if (z == null) {
      return null.asInstanceOf[V]
    }

    // Y is the node to be unlinked from the tree.
    var y: Node[K, V] = null
    if (z.left == null || z.right == null) {
      y = z
    } else {
      y = z.successor()
    }

    // Y is guaranteed to be non-null at this point.
    var x: Node[K, V] = null
    if (y.left != null) {
      x = y.left
    } else {
      x = y.right
    }

    // X is the child of y which might potentially replace y in the tree. X might be null at
    // this point.
    var xParent: Node[K, V] = null
    if (x != null) {
      x.parent = y.parent
      xParent = x.parent
    } else {
      xParent = y.parent
    }
    if (y.parent == null) {
      root = x
    } else {
      if (y == y.parent.left) {
        y.parent.left = x
      } else {
        y.parent.right = x
      }
    }

    if (y != z) {
      if (y.color == Color.BLACK) {
        removeFixup(x, xParent)
      }

      y.parent = z.parent
      y.color = z.color
      y.left = z.left
      y.right = z.right

      if (z.left != null) {
        z.left.parent = y
      }
      if (z.right != null) {
        z.right.parent = y
      }
      if (z.parent != null) {
        if (z.parent.left == z) {
          z.parent.left = y
        } else {
          z.parent.right = y
        }
      } else {
        root = y
      }
    } else if (y.color == Color.BLACK) {
      removeFixup(x, xParent)
    }

    z.value
  }

  def get(key: K): V = {
    val node = findNode(key);
    if (node == null) {
      return null.asInstanceOf[V]
    }
    node.value
  }

  def forEach(fn: Entry[K, V] => Unit): Unit = {
    if (root == null) {
      return;
    }
    var current = treeMinimum(root);
    while (current != null) {
      fn(new Entry(current.key, current.value))
      current = current.successor()
    }
  }

  def findNode(key: K): Node[K, V] = {
    var current = root
    while (current != null) {
      val comparisonResult = key.compareTo(current.key)
      if (comparisonResult == 0) {
        return current
      }
      if (comparisonResult < 0) {
        current = current.left
      } else {
        current = current.right
      }
    }
    null
  }

  def treeInsert(key: K, value: V): InsertResult[K, V] = {
    var y: Node[K, V] = null
    var x: Node[K, V] = root

    while (x != null) {
      y = x
      val comparisonResult = key.compareTo(x.key)
      if (comparisonResult < 0) {
        x = x.left
      } else if (comparisonResult > 0) {
        x = x.right
      } else {
        val oldValue = x.value
        x.value = value
        return new InsertResult(false, null, oldValue)
      }
    }

    var z = new Node[K, V](key, value)
    z.parent = y
    if (y == null) {
      root = z
    } else {
      if (key.compareTo(y.key) < 0) {
        y.left = z
      } else {
        y.right = z
      }
    }
    new InsertResult(true, z, null.asInstanceOf[V]);
  }

  def leftRotate(x: Node[K, V]): Node[K, V] = {
    var y = x.right

    // Turn y's left subtree into x's right subtree.
    x.right = y.left
    if (y.left != null) {
      y.left.parent = x
    }

    // Link x's parent to y.
    y.parent = x.parent
    if (x.parent == null) {
      root = y
    } else {
      if (x == x.parent.left) {
        x.parent.left = y
      } else {
        x.parent.right = y
      }
    }

    // Put x on y's left.
    y.left = x
    x.parent = y

    y
  }

  def rightRotate(y: Node[K, V]): Node[K, V] = {
    var x = y.left

    // Turn x's right subtree into y's left subtree.
    y.left = x.right
    if (x.right != null) {
      x.right.parent = y
    }

    // Link y's parent to x;
    x.parent = y.parent
    if (y.parent == null) {
      root = x
    } else {
      if (y == y.parent.left) {
        y.parent.left = x
      } else {
        y.parent.right = x
      }
    }

    x.right = y
    y.parent = x

    return x
  }

  def removeFixup(_x: Node[K, V], _xParent: Node[K, V]): Unit = {
    var x       = _x
    var xParent = _xParent
    while (x != root && (x == null || x.color == Color.BLACK)) {
      if (x == xParent.left) {
        // Note: the text points out that w cannot be null. The reason is not obvious from
        // simply looking at the code; it comes about from the properties of the red-black
        // tree.
        var w = xParent.right
        if (w.color == Color.RED) {
          // Case 1
          w.color = Color.BLACK
          xParent.color = Color.RED
          leftRotate(xParent)
          w = xParent.right
        }
        if ((w.left == null || w.left.color == Color.BLACK)
            && (w.right == null || w.right.color == Color.BLACK)) {
          // Case 2
          w.color = Color.RED
          x = xParent
          xParent = x.parent
        } else {
          if (w.right == null || w.right.color == Color.BLACK) {
            // Case 3
            w.left.color = Color.BLACK
            w.color = Color.RED
            rightRotate(w)
            w = xParent.right
          }
          // Case 4
          w.color = xParent.color
          xParent.color = Color.BLACK
          if (w.right != null) {
            w.right.color = Color.BLACK
          }
          leftRotate(xParent)
          x = root;
          xParent = x.parent
        }
      } else {
        // Same as "then" clause with "right" and "left" exchanged.
        var w = xParent.left
        if (w.color == Color.RED) {
          // Case 1
          w.color = Color.BLACK
          xParent.color = Color.RED
          rightRotate(xParent)
          w = xParent.left
        }
        if ((w.right == null || w.right.color == Color.BLACK)
            && (w.left == null || w.left.color == Color.BLACK)) {
          // Case 2
          w.color = Color.RED
          x = xParent
          xParent = x.parent
        } else {
          if (w.left == null || w.left.color == Color.BLACK) {
            // Case 3
            w.right.color = Color.BLACK
            w.color = Color.RED
            leftRotate(w)
            w = xParent.left
          }
          // Case 4
          w.color = xParent.color
          xParent.color = Color.BLACK
          if (w.left != null) {
            w.left.color = Color.BLACK
          }
          rightRotate(xParent)
          x = root
          xParent = x.parent
        }
      }
    }
    if (x != null) {
      x.color = Color.BLACK
    }
  }
}

object RedBlackTree {
  type Color = Int
  object Color {
    val RED   = 1
    val BLACK = 2
  }

  def treeMinimum[K, V](x: Node[K, V]): Node[K, V] = {
    var current = x
    while (current.left != null) {
      current = current.left
    }
    current
  }

  final class Node[K, V](val key: K, var value: V) {
    var left: Node[K, V]   = null
    var right: Node[K, V]  = null
    var parent: Node[K, V] = null
    var color: Color       = Color.RED

    def successor(): Node[K, V] = {
      var x = this
      if (x.right != null) {
        return treeMinimum(x.right)
      }
      var y = x.parent
      while (y != null && x == y.right) {
        x = y
        y = y.parent
      }
      y
    }
  }

  final class Entry[K, V](val key: K, val value: V)

  final class InsertResult[K, V](val isNewEntry: Boolean,
                                 val newNode: Node[K, V],
                                 val oldValue: V)
}
