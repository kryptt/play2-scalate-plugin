package sbt

trait PlayScalateKeys {

  val templateInfo = SettingKey[PartialFunction[String, String]]("scalate-templates-formats")

}
object PlayScalateKeys extends PlayScalateKeys