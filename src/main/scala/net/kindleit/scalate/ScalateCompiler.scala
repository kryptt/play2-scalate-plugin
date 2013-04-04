package net.kindleit.scalate

import org.fusesource.scalate.{ TemplateEngine, TemplateSource, Binding }
import org.fusesource.scalate.util.IOUtil

import java.io.File

class ScalateCompiler(val sources: List[File], val targetDirectory: File,
    val packagePrefix: String = "views/html", val scalateImports: Seq[String] = Nil,
    val overwrite: Boolean = true, val scalateBindings: List[Binding] = Nil,
    val logConfig: Option[File] = None) {

  lazy val engine = {
    val e = new TemplateEngine
    // initialize template engine
    e.importStatements = scalateImports.toList
    e.bindings = scalateBindings
    e.packagePrefix = packagePrefix
    e
  }

  def execute: List[File] = {
    logConfig map { logConfig =>
      System.setProperty("logback.configurationFile", logConfig.toString)
    }

    targetDirectory.mkdirs

    sources map { src =>
      val template = TemplateSource.fromFile(src)
      val code = engine.generateScala(template).source
      val prefix = src.getName().split("/").last.split(".scalate.")(0)
      val scalaFile = new File(targetDirectory, packagePrefix.replace(".", "/") + "/" + prefix + ".scalate.scala")
      scalaFile.getParentFile().mkdirs()
      IOUtil.writeBinaryFile(scalaFile, code.getBytes("UTF-8"))
      scalaFile
    }
  }
}