package com.github.zhoufeng1989.tldr

/**
 * Created by zhoufeng on 16/1/2.
 */
import org.yaml.snakeyaml.Yaml
import scala.collection.JavaConverters._
import sys.process._
import scala.util.{Try, Success, Failure}

object Tldr extends App {
  val platformMap = Map("Mac OS X" -> "osx")
  val configFile = s"${System.getProperty("user.home")}/.tldrcc"
  val pagesDir = Try{
    new Yaml().load(scala.io.Source.fromFile(configFile).reader()).asInstanceOf[java.util.Map[String, String]].asScala.toMap
  }.map {
    config => config("pages")
  } match {
    case Failure(_) => println("tldr init first"); sys.exit(1)
    case Success(dir) => dir
  }
  val platforms = platformMap get (System.getProperty("os.name")) match {
    case Some(platform) => List("common", platform)
    case None => List("common")
  }
  val command = "ls"
  val separator = System.getProperty("file.separator")
  val pagePaths = platforms map {
    platform => List(pagesDir, platform, command).mkString(System.getProperty("file.separator")) + ".md"
  }
  println(pagePaths)
  println(pagePaths.map(parse))
  def init(dest: String) = {
    s"git clone https://github.com/tldr-pages/tldr.git ${dest}" !
  }

  def update(dest: String) = {
    s"cd ${dest}; git pull" !
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


