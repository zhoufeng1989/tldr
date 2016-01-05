package com.github.zhoufeng1989.tldr

/**
 * Created by zhoufeng on 16/1/2.
 */
import org.yaml.snakeyaml.Yaml
import scala.collection.JavaConverters._
import sys.process._
import scala.util.{Try, Success, Failure}

sealed abstract class Command {
  def exec: Unit
}
case class Find(command: String, platforms: List[String], pagesDir: String) extends Command {
  override def exec() = ""
}
//case class Update(destRepo: String) extends Command
//case object Init extends Command
//case object Help extends Command
//case object WrongCommand extends Command
object Command {
  //def apply(args: Array[String]) = args match {
  //  case Array("init") => Init
  //  case Array("help") => Help
  //  case Array("update") => {
  //    val env = getEnv()
  //    Update(env("pagesDir"))
  //  }
  //  case Array("find", c) => {
  //    val env = getEnv()
  //    Find(c, env("platforms"), env("pagesDir"))
  //  }
  //  case _ => WrongCommand
  //}

  def getEnv() = {
    val platformMap = Map("OSX" -> "osx")
    val configFile = s"${System.getProperty("user.home")}/.tldrcc"

    val pagesDir, rootDir = Try{
      val yaml = new Yaml()
      val content = yaml.load(scala.io.Source.fromFile(configFile).reader())
      content.asInstanceOf[java.util.Map[String, String]].asScala.toMap
    }.map {
      config => (config("pages"), config("root"))
    } match {
      case Failure(_) => throw new RuntimeException("Run tldr init first")
      case Success((p, r)) => (p, r)
    }

    val platforms = platformMap get (System.getProperty("os.name")) match {
      case Some(platform) => List("common", platform)
      case None => List("common")
    }
    val separator = System.getProperty("file.separator")
    //val pagePaths = platforms map {
    //  platform => List(pagesDir, platform, command).mkString(System.getProperty("file.separator")) + ".md"
    //}
    Map("platforms" -> platforms, "pagesDir" -> pagesDir, "rootDir" -> rootDir)
  }
}


object Tldr extends App {
  println(Command.getEnv())
  //println(pagePaths)
  //println(pagePaths.map(parse))
  def init(dest: String) = {
    s"git clone https://github.com/tldr-pages/tldr.git ${dest}" !
  }

  def update(dest: String) = {
    s"cd ${dest}; git pull" !
  }

  def help():String = {
    "this is help document"
  }


  def parse(pagePath: String): Option[String] = {
    try {
      val page = scala.io.Source.fromFile(pagePath).getLines()
      Some(page.mkString("\n"))
    }
    catch {
      case _: Exception => None
    }
  }
}


