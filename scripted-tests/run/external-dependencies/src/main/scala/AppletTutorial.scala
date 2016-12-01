import java.awt.event.MouseListener
import java.awt.event.MouseEvent
import java.applet.Applet
import java.awt.Graphics

// We need linking errors, We will never port java.applet._
object AppletTutorial extends Applet with Mouse {
  def sc = Set(1)

  override def init(): Unit = {
    strBuffer = new StringBuffer()
    addItem("initializing the apple ")
  }

  override def start(): Unit = {
    addItem("starting the applet ")
  }

  override def stop(): Unit = {
    addItem("stopping the applet ")
  }

  override def destroy(): Unit = {
    addItem("unloading the applet")
  }

  override def paint(g: Graphics): Unit = {
    g.drawRect(0, 0, getWidth() - 1, getHeight() - 1)
    g.drawString(strBuffer.toString(), 10, 20)
  }
}

trait Mouse extends MouseListener { self: Applet =>
  protected var strBuffer: StringBuffer = _

  override def init(): Unit = {
    addMouseListener(this);
    strBuffer = new StringBuffer()
    addItem("initializing the apple ")
  }

  override def start(): Unit = {
    addItem("starting the applet ")
  }

  override def stop(): Unit = {
    addItem("stopping the applet ")
  }

  override def destroy(): Unit = {
    addItem("unloading the applet")
  }

  protected def addItem(word: String): Unit = {
    System.out.println(word)
    strBuffer.append(word)
    repaint()
  }

  override def mouseEntered(event: MouseEvent): Unit  = ()
  override def mouseExited(event: MouseEvent): Unit   = ()
  override def mousePressed(event: MouseEvent): Unit  = ()
  override def mouseReleased(event: MouseEvent): Unit = ()
  override def mouseClicked(event: MouseEvent): Unit =
    addItem("mouse clicked! ")
}
