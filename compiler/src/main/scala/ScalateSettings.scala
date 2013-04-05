package sbt

import Keys._
import PlayKeys.{templatesTypes => playTemplateTypes, _}
import PlayScalateKeys._
import net.kindleit.scalate.ScalateCompiler

object ScalateSettings {

  private val basicImports = Seq[String](
    "play.templates._",
    "play.templates.TemplateMagic._"
  )

  val ScalateTemplates = (state: State, sourceDirectory: File, generatedDir: File,
      templateInfo: PartialFunction[String, String], additionalImports: Seq[String],
      streams: TaskStreams) => {
    import play.templates._

    object TemplateType {
      def unapply(p:File):Option[(File, String)] = {
        val extension = p.name.split('.').last
        if (templateInfo.isDefinedAt(extension))
          Some(p, templateInfo(extension))
        else None
      }
    }

    (sourceDirectory ** "*.scalate.*").get.collect {
      case TemplateType(template, format) => try {
        streams.log.debug("Compiling " + template)
        val imports = (basicImports ++ additionalImports).map("import " + _.replace("%format%", format))
        new ScalateCompiler(List(template), generatedDir, "views/" + format, imports).execute
      } catch {
        case e:Throwable => throw new TemplateCompilationError(template, e.getMessage(), 0, 0)
      }
    }

    (generatedDir ** "*.scalate.scala").get.map(_.getAbsoluteFile)
  }

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
}