package net.kindleit.scalate

import play.api._
import org.fusesource.scalate._
import org.fusesource.scalate.util.FileResourceLoader
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import java.io.PrintWriter
import java.io.StringWriter

class ScalatePlugin(app: Application) extends Plugin {

  lazy val engine = {
    val e = new TemplateEngine(null, null)
    e.resourceLoader = new FileResourceLoader(Some(app.getFile("app/views")))
    e.workingDirectory = app.getFile("tmp")
    e.combinedClassPath = true
    e.classLoader = app.classloader

    app.configuration.getString("scalate.layoutStrategy") foreach { layout =>
      e.layoutStrategy = new DefaultLayoutStrategy(e, app.getFile(layout).getAbsolutePath)
    }
    e
  }

  override def onStop() = { engine.shutdown() }

}

object ScalatePlugin {

  def engine() =
    play.Play.application().plugin(classOf[ScalatePlugin]).engine

  def renderContext(requestUri: String) = {
    val writer = new StringWriter
    (new DefaultRenderContext(requestUri, engine, new PrintWriter(writer)), writer.getBuffer())
  }

}