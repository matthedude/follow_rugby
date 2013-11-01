package models

object Linker {

  def link(str: String) = {
    str.replace(" ", "").toLowerCase
  }
  
  def linkVideo(str: String) = {
    str.trim.replaceAll("(\\s)\\1","").replaceAll("[^a-zA-Z0-9\\s]", "").replace(" ", "-")
  }
  
  def strip(str: String) = {
    str.replace(" ", "").replace(",", "")
  }
}