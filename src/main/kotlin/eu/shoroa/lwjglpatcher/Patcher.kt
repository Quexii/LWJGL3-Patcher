package eu.shoroa.lwjglpatcher

import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


class Patcher private constructor(private val version: String, private val modules: List<String>) {
    private val urlFunc: (String) -> String = { "https://repo1.maven.org/maven2/org/lwjgl/$it/$version/$it-$version.jar" }
    private val tempDir = File("temp")
    private val files = modules.map { File(tempDir, "$it.jar") }
    fun run() {
        if (tempDir.exists()) tempDir.delete()
        tempDir.mkdir()
        modules.forEach {
            println("Downloading $it to ${tempDir.absolutePath}")
            println(" -> ${urlFunc(it)}")
            val file = File(tempDir, "$it.jar")
            file.writeBytes(java.net.URL(urlFunc(it)).readBytes())
        }

        files.forEach {
            println("Processing ${it.name}")
            process(it, File(tempDir, "${it.name}.patched"))
        }

        combine(File("lwjgl-patched.jar"), *files.map { File(tempDir, "${it.name}.patched") }.toTypedArray())

        files.forEach {
            it.delete()
            File(tempDir, "${it.name}.patched").delete()
        }
        tempDir.delete()
    }

    @Throws(IOException::class)
    private fun combine(outputFile: File, vararg files: File) {
        println("Combining ${files.size} files into ${outputFile.name}")
        val zips = arrayOfNulls<ZipFile>(files.size)

        for (index in files.indices) {
            zips[index] = ZipFile(files[index])
        }

        val seenNames: MutableSet<String> = HashSet()

        ZipOutputStream(FileOutputStream(outputFile)).use { out ->
            for (zip in zips) {
                for (entry in Collections.list(zip!!.entries())) {
                    if (entry.isDirectory || !seenNames.add(entry.name)) {
                        continue
                    }

                    out.putNextEntry(ZipEntry(entry.name))
                    val entryIn = zip.getInputStream(entry)
                    IOUtils.copy(entryIn, out)
                    entryIn.close()
                }
                zip.close()
            }
        }
    }

    @Throws(IOException::class)
    private fun process(inFile: File, outFile: File) {
        println("Processing ${inFile.name} to ${outFile.name}")
        ZipFile(inFile).use { `in` ->
            ZipOutputStream(FileOutputStream(outFile)).use { out ->
                for (entry in Collections.list(`in`.entries())) {
                    if (entry.isDirectory) {
                        continue
                    }
                    if (entry.name.startsWith("META-INF/")) {
                        continue
                    }

                    val entryIn = `in`.getInputStream(entry)
                    if (entry.name.endsWith(".class")) {
                        var data: ByteArray = IOUtils.toByteArray(entryIn)
                        val reader = ClassReader(data)
                        val writer = ClassWriter(0)

                        reader.accept(
                            ClassRemapper(
                                writer,
                                object : Remapper() {
                                    override fun map(internalName: String): String {
                                        return replace(internalName)
                                    }
                                }), 0
                        )
                        data = writer.toByteArray()
                        out.putNextEntry(
                            ZipEntry(
                                replace(entry.name.substring(0, entry.name.lastIndexOf('.'))) + ".class"
                            )
                        )
                        out.write(data)
                    } else {
                        out.putNextEntry(ZipEntry(entry.name))
                        IOUtils.copy(entryIn, out)
                    }
                    entryIn.close()
                }
            }
        }
    }

    private fun replace(string: String): String {
        if (string.endsWith("org/lwjgl/BufferUtils") || string.endsWith("org/lwjgl/PointerBuffer")) {
            return string.replace("lwjgl", "lwjgl3")
        }
        return string
    }

    class Builder {
        private val _modules: List<String> = mutableListOf()
        private var _version = "3.3.3"

        fun version(version: String) = apply {
            this._version = version
        }

        fun modules(vararg modules: String) = apply {
            (_modules as MutableList).addAll(modules)
        }

        fun build() = Patcher(_version, _modules)
    }
}