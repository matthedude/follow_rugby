package models

object Linker {

  def link(str: String) = {
    str.replace(" ", "").toLowerCase
  }
  
  def linkVideo(str: String) = {
    str.replace(" ", "-").replace(".", "")
  }
}