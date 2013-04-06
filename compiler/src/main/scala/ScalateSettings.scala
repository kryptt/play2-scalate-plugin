package sbt

import Keys._
import PlayKeys.{templatesTypes => playTemplateTypes, _}
import PlayScalateKeys._
import net.kindleit.scalate.ScalateCompiler

object ScalateSettings {

  private val basicImports = Seq[String](
    "net.kindleit.scalate.ScalatePlugin._",
    "play.templates._",
    "play.templates.TemplateMagic._"
  )

  val ScalateTemplates = (state: State, sourceDirectory: File, generatedDir: File,
      templateInfo: PartialFunction[String, String], additionalImports: Seq[String],
      streams: TaskStreams) => withSbtClassLoader( _ => {
    import play.templates._

    object TemplateType {
      def unapply(p:File):Option[(File, String)] = {
        val extension = p.name.split('.').last
        if (templateInfo.isDefinedAt(extension))
          Some(p, templateInfo(extension))
        else None
      }
    }

    (sourceDirectory ** "*.*").get.collect {
      case TemplateType(template, format) => try {
        streams.log.debug("Compiling " + template)
        val imports = (basicImports ++ additionalImports).map("import " + _.replace("%format%", format))
        new ScalateCompiler(List(template), sourceDirectory, generatedDir, "views/" + format, imports).execute
      } catch {
        case e:Throwable => throw new TemplateCompilationError(template, e.getMessage(), 0, 0)
      }
    }

    (generatedDir ** "*.scalate.scala").get.map(_.getAbsoluteFile)
  })

  val sbtLoader = this.getClass.getClassLoader

  val scalateSettings = Seq[Setting[_]](
    sourceGenerators in Compile <+= (state, sourceDirectory in Compile, sourceManaged in Compile, templateInfo, templatesImport,
        streams) map ScalateTemplates,

    templateInfo := {
      case "scaml" => "html"
      case "jade" => "html"
      case "mustache" => "html"
      case "ssp" => "html"
      case "squery" => "html"
    }
  )

  protected def withSbtClassLoader[A](f: ClassLoader => A): A = {
    val oldLoader = Thread.currentThread.getContextClassLoader
    Thread.currentThread.setContextClassLoader(sbtLoader)
    try {
      f(sbtLoader)
    } finally {
      Thread.currentThread.setContextClassLoader(oldLoader)
    }
  }

}