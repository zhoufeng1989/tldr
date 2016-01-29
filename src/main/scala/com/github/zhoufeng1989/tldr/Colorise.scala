package com.github.zhoufeng1989.tldr

/**
 * Created by zhoufeng on 16/1/29.
 */
object Colorise {

  implicit class StringColorise(val content: String) extends AnyVal {

    import Console.{RED, BLUE, GREEN}

    def red = s"$RED$content"

    def blue = s"$BLUE$content"

    def green = s"$GREEN$content"

  }

}
