play2-scalate-plugin
====================

Plugin that integrates Scalate with PlayFramework

The plugin is divided in two parts:

    "net.kindleit" %% "play2-scalate-compiler" % "0.1-SNAPSHOT"
    "net.kindleit" %% "play2-scalate-plugin" % "0.1-SNAPSHOT"
    
The compiler adds sbt tasks to precompile scalate templates into Play! compatible template files, and the plugin exports 
the required TemplateEngine so that the compiled code can render properly <small>(this is due to a current limitation in scalate with help we can make this plugin go away all together)</small>

The plugin is published via OSS Sonatype to maven central.

To use the compiler, add the following lines to *plugins.sbt* (or the *Build.scala*):

    addSbtPlugin("net.kindleit" %% "play2-scalate-compiler" % "0.1-SNAPSHOT")

And add

    val project = play.Project(...).settings(ScalateSettings.scalateSettings)

to register the required source Generators.


For the plugin, add the following dependency in your *Build.scala*

    val appDependencies = Seq(
      "net.kindleit" %% "play2-scalate-plugin" % "0.1-SNAPSHOT"
    )

And register the plugin via *conf/play.plugins*

    300:net.kindleit.scalate.ScalatePlugin

And your all set.

You can have the following infrastructure:

    /app/views/index.jade
    /app/views/school.scaml

etc...

Happy hacking!
