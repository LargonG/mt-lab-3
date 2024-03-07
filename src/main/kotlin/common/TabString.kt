package common

interface TabString {
    fun tabString(builder: StringBuilder, tabs: Int)
    fun tabString(tabs: Int): String {
        val builder = StringBuilder()
        tabString(builder, tabs)
        return builder.toString()
    }
}