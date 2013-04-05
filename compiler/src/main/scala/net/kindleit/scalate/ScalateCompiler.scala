package net.kindleit.scalate

import org.fusesource.scalate.{ TemplateEngine, TemplateSource, Binding }
import org.fusesource.scalate.util.IOUtil
import java.io.File
import java.util.regex.Pattern.quote

class ScalateCompiler(val sources: List[File], val sourceDirectory: File, val targetDirectory: File,
    val packagePrefix: String = "views/html", val scalateImports: Seq[String] = Nil,
    val overwrite: Boolean = true, val scalateBindings: List[Binding] = Nil,
    val logConfig: Option[File] = None) {

    val playTemplate = """
object %className% extends BaseScalaTemplate[play.api.templates.Html,Format[play.api.templates.Html]](play.api.templates.HtmlFormat) with play.api.templates.Template0[play.api.templates.Html] {

    def apply():play.api.templates.Html = _display_ { val (ctx, out) = renderContext("%uri%"); %scalateObj%.$_scalate_$render(ctx); Seq[Any](format.raw(out.toString())) }
    def render() = apply()
    def f() = () => apply()
    def ref: this.type = this

}

object $_scalate_$"""

  lazy val engine = {
    val e = new TemplateEngine
    // initialize template engine
    e.importStatements = scalateImports.toList
    e.bindings = scalateBindings
    e.packagePrefix = packagePrefix.replaceAll("/", ".")
    e
  }

  def execute: List[File] = {
    logConfig map { logConfig =>
      System.setProperty("logback.configurationFile", logConfig.toString)
    }

    targetDirectory.mkdirs

    sources map { src =>
      val className = src.getName().split("\\.").dropRight(1).reduce(_+"_"+_)
      val scalaFile = new File(targetDirectory, packagePrefix + "/" + className + ".scalate.scala")
      val template = TemplateSource.fromFile(src)

      val code = processScalaCode(engine.generateScala(template).source, src, className)

      scalaFile.getParentFile().mkdirs()
      IOUtil.writeBinaryFile(scalaFile, code.getBytes("UTF-8"))
      scalaFile
    }
  }

  def processScalaCode(code: String, src: File, className: String) = {
    val (srcName, srcDir, srcPath) = srcNameAndPath(src)
    val scalateGeneratedPackage = srcDir.replaceAll("\\.", "_").replaceAll("/", ".").dropRight(1) + "s"
    val playTemplatePrefix = playTemplate
      .replaceAll("%className%", className)
      .replaceAll("%uri%", srcPath + "/" + className)
      .replaceAll("%scalateObj%", "\\$_scalate_\\$" + srcName)

    code
      .replaceAll(quote(scalateGeneratedPackage), srcPath.replaceAll("\\$", "\\\\\\$"))
      .replaceAll(quote("object $_scalate_$"), playTemplatePrefix.replaceAll("\\$", "\\\\\\$"))
  }

  private def srcNameAndPath(src: File) = {
    val srcDir = sourceDirectory.toURI.normalize.toString.substring(5) + "view/"
    val srcName = src.toURI.normalize.toString.substring(6+srcDir.length).replaceAll("\\.", "_").replaceAll("/", ".")
    val subF = srcName.lastIndexOf('.')
    (srcName, srcDir, if (subF > 0 ) srcName.substring(0, subF) else "")
  }

}