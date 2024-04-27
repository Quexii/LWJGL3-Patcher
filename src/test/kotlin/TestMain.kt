import eu.shoroa.lwjglpatcher.Patcher

fun main() {
    val patcher = Patcher.Builder()
        .version("3.3.3")
        .modules("lwjgl", "lwjgl-stb", "lwjgl-nanovg", "lwjgl-nfd", "lwjgl-tinyfd")
        .build()

    patcher.run()
}