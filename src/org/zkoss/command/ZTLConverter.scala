package org.zkoss.command

import java.io.File
import org.zkoss.io.RichFiles._
import org.streum.configrity.Configuration

object ZTLConverter extends App {

  val conf = Configuration.load("config.properties")
  val workspace = conf[String]("workspace")
  val version = conf[String]("version")

  val zulPattern = ("""([ZFB]\d\d)-([a-zA-Z]{2,4})-(\d{3,4})""").r
  val ztlPattern = ("""([ZFB]\d\d)_([a-zA-Z]{2,4})_(\d{3,4})""").r

  val prjPath = workspace + "ztltest/"
  val zulPath = workspace + "zk/zktest/src/archive/test2/"

  val pkgPath = "org/zkoss/zktest/test2/"
  val funcTestPath = prjPath + "zstl/test/" + pkgPath
  val visionTestPath = prjPath + "zstl/vision-test/" + pkgPath
  val tabletFuncTestPath = prjPath + "zstl_tablet/test/" + pkgPath
  val codegenPath = prjPath + "codegen/" + pkgPath

  val versionPattern = """(\d\d)""".r

  val src = args.headOption.getOrElse(version)
  val ztlPkgPaths = List("B", "F", "Z").map(category => funcTestPath + category + version + "/")
  val prefixs = List("B", "F", "Z") map (category => category + version)

  //  val visionAndroidPath = prjPath + "vision_tablet/" + pkgPath + "theme/"
  //  val visionIOSPath = prjPath + "vision_tablet/" + pkgPath + "theme/ipad/"

  val template =
    """package org.zkoss.zktest.test2.%1$s

import org.zkoss.ztl.Tags
import org.zkoss.zstl.ZTL4ScalaTestCase
import org.junit.Test

@Tags(tags = "%1$s-%2$s-%3$s.zul")
class %1$s_%2$s_%3$sTest extends ZTL4ScalaTestCase {

@Test
def testClick() = {
  val zscript = """ + ("\"\"\"%4$s\"\"\"") +
      """  
  runZTL(zscript,
    () => {
      verifyTrue("", jq(".").exists)
    })
    
  }
}"""

  def isIgnore(zulName: String) = getIgnoreCases().exists(ztlName => ztlName.contains(renameToZtl(zulName)))

  def getIgnoreCases(): List[String] = {
    val ls = List(visionTestPath, tabletFuncTestPath /*, codegenPath*/ ) flatMap { path =>
      prefixs flatMap { prefix: String =>
        val pkg = new File(path + prefix + "/")
        if (pkg.exists())
          pkg.listFiles() map { file =>
            file.getName
          } filter (_.matches(prefix))
        else List[String]()
      }
    }

    // chinese encoding case
    "B50_ZK_647Test.scala" +: "B60_ZK_1341Test.scala" +: "F60_ZK_1047Test.scala" +: ls
  }

  def renameToZtl(zul: String) = zul.replace(".zul", "") match {
    case zulPattern(version, category, issueid) => "%s_%s_%s".format(version, category, issueid)
    case z => z
  }

  def genZtlFile(ztlDir: File, zulFile: File) = {
    val ztlName = renameToZtl(zulFile.getName())

    if (ztlDir.getAbsolutePath().endsWith(ztlName.substring(0, 3)))
      ztlName match {
        case ztlPattern(version, category, issueid) =>
          val ztl = new File(ztlDir, ztlName + "Test.scala")
          if (ztl.createNewFile()) {
            println("Generating " + ztl.getName)
            ztl.text = template format (version, category, issueid, zulFile.text)
            println("Content:  \n" + ztl.text)
          } else println(ztlName + " exists")

        case _ =>
      }
  }

  new File(zulPath).listFiles() filter { zulFile =>
    zulFile.getName().take(3).contains(version) && !isIgnore(zulFile.getName())
  } foreach { zulFile =>
    ztlPkgPaths foreach { ztlPkgPath =>
      val ztlPkgDir = new File(ztlPkgPath)
      if (!ztlPkgDir.exists())
        ztlPkgDir.mkdir()
      genZtlFile(ztlPkgDir, zulFile)
    }
  }

  println("Generating Finish")
  println("Src Path: " + zulPath)
  println("Trg Path: " + ztlPkgPaths.mkString(", "))

}