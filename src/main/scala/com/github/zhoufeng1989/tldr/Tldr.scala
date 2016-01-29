package com.github.zhoufeng1989.tldr

/**
 * Created by zhoufeng on 16/1/2.
 */
import org.yaml.snakeyaml.Yaml
import scala.collection.JavaConverters._
import sys.process._
import scala.util.{Try, Success, Failure}
import Colorise.StringColorise

sealed abstract class Command {
  def exec: Unit
}

case object Init extends Command {
  override def exec = {
    val configFile = s"${System.getProperty("user.home")}/.tldrc"
    val rootDir = {
      val defaultRoot = s"${System.getProperty("user.home")}/.tldr"
      val inputDir = scala.io.StdIn.readLine(s"Please enter tldr root dir [${defaultRoot}]:")
      if (inputDir.isEmpty) defaultRoot else inputDir
    }
    Process(Seq("bash", "-c", s"mkdir ${rootDir} && git clone https://github.com/tldr-pages/tldr.git ${rootDir}")) !
    val writer = new java.io.PrintWriter(new java.io.File(configFile))
    writer.write(List(s"root: ${rootDir}", s"pages: ${rootDir}/pages") mkString("\n"))
    writer.close()
  }
}

case class Update(destRepo: String) extends Command {
  override def exec = {
    println("update tldr...")
    Process(Seq("bash", "-c", s"cd ${destRepo} && git pull")) !;
    println("Done")
  }
}

case class Find(command: String, platforms: List[String], pagesDir: String) extends Command {
  override def exec = {
    val resultStream = {
      platforms.toStream.foldLeft(List.empty[String]){
        (result, platform) => {
          val pagePath = s"${List(pagesDir, platform, command) mkString System.getProperty("file.separator")}.md"
          parse(pagePath) match {
            case Some(x) => result ++ List(x)
            case _ => result
          }
        }
      }
    }
    Try(resultStream.head) match {
      case Success(x) => println(render(x))
      case Failure(_) => println(s"Command ${command} not found")
    }
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

  def render(content: String) = {
    val lines = content.split("\n")
    lines map {
      case str if str.startsWith("#") => str.red
      case str if str.startsWith("`") => str.green
      case str if str.startsWith("-") => str.blue
      case str => str
    } mkString "\n"

  }
}

case object Help extends Command {
  override def exec = {
    println("help command")
  }
}

case object WrongCommand extends Command {
  override def exec = {
    println("wrong command")
    Help.exec
  }
}

object Command {
  def apply(args: Array[String]) = args match {
    case Array("init") => Init
    case Array("help") => Help
    case Array("update") => {
      val env = Env()
      Update(env.rootDir)
    }
    case Array("find", c) => {
      val env = Env()
      Find(c, env.platforms, env.pagesDir)
    }
    case _ => WrongCommand
  }
}


class Env(val platforms: List[String], val pagesDir: String, val rootDir: String)

object Env {
  val platformMap = Map("Mac OS X" -> "osx")
  val configFile = s"${System.getProperty("user.home")}/.tldrc"

  def apply() = {
    val (pagesDir, rootDir) = Try {
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
    new Env(platforms, pagesDir, rootDir)
  }
}

object Tldr extends App {
  val command = Command(args)
  command.exec
}


