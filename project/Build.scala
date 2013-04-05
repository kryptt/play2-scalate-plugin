import sbt._
import Keys._

object PluginBuild extends Build {
  import scala.xml._
  import scala.xml.transform._

  resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
  addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.0.0")

  lazy val projVersion = "0.1-SNAPSHOT"
  lazy val projName = "play2-scalate"
  lazy val playVersion = "2.1.0"

  lazy val commonSettings: Seq[Project.Setting[_]] = Defaults.defaultSettings ++ Seq(
    organization := "net.kindleit",
    version := projVersion,

    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += Resolver.file("ivy2-local", new File(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),

    autoScalaLibrary := false,

    pomPostProcess := { (node: Node) =>
      new RuleTransformer(FixExtra)(node)
    },
    pomExtra := (
      <url>https://github.com/kryptt/play2-scalate-plugin</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:kryptt/play2-scalate-plugin</url>
        <connection>scm:git:git@github.com:kryptt/play2-scalate-plugin</connection>
      </scm>
      <developers>
        <developer>
          <id>kryptt</id>
          <name>Rodolfo Hansen</name>
          <url>http://hobbes-log.blogspot.com</url>
        </developer>
      </developers>
    ),
    pomIncludeRepository := { x => false },

    publishMavenStyle := true,
    publishArtifact in Test := false,
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },

    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
  )

  lazy val compilerSettings: Seq[Project.Setting[_]] = commonSettings ++ Seq(
    scalaVersion := "2.9.2",
    scalaBinaryVersion := "2.9.2",
    sbtVersion := "0.12",
    sbtPlugin := true,
    name := "play2-scalate-compiler",
    description := "Play2 SBT plugin that compiles Scalate Templates as Play! Templates",
    libraryDependencies += "org.fusesource.scalate" % "scalate-core_2.9" % "1.6.1",
    addSbtPlugin("play" % "sbt-plugin" % playVersion)
  )

  lazy val pluginSettings: Seq[Project.Setting[_]] = commonSettings ++ Seq(
    scalaVersion := "2.10.1",
    scalaBinaryVersion := "2.10",
    libraryDependencies += "play" %% "play" % playVersion % "provided",
    libraryDependencies += "org.fusesource.scalate" %% "scalate-core" % "1.6.1",
    name := "play2-scalate-plugin",
    description := "Play2 plugin that integrates Scalate with PlayFramework"
  )

  object FixExtra extends RewriteRule {
    override def transform(n: Node): Seq[Node] = n match {
      case <extraDependencyAttributes>{extra}</extraDependencyAttributes> =>
          <extraDependencyAttributes xml:space="preserve">{extra.text.replace(" ", "\n")}</extraDependencyAttributes>
      case _ => n
    }
  }

  //SBT Compiler Project used by play to generate the templates during the compile phase.
  val compiler = Project(id = projName + "-compiler", base = file("compiler"), settings = compilerSettings)

  //Play Plugin used to keep a TemplateEngine during runtime.
  val plugin = Project(id = projName + "-plugin", base = file("plugin"), settings = pluginSettings)

  val main = Project(id = projName + "-base", base = file(".")) aggregate(compiler, plugin)

}
