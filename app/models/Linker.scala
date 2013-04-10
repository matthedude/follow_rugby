package models

object Linker {

	def link(str: String) = {
		str.replaceAll(" ", "").toLowerCase
	}
}