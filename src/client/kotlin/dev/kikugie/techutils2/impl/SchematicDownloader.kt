package dev.kikugie.techutils2.impl

import com.mojang.datafixers.util.Either
import dev.kikugie.techutils2.TechUtilsClient
import fi.dy.masa.litematica.data.DataManager
import kotlinx.coroutines.*
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.outputStream

typealias DownloadResult = Either<Path, Exception>

object SchematicDownloader {
    val timeout = 10000L
    private val destination: Path
        get() = DataManager.getSchematicsBaseDirectory().resolve("downloads").toPath()
    private val currentDownloads = ConcurrentHashMap<String, Job>()

    @OptIn(DelicateCoroutinesApi::class)
    fun download(link: String, overwrite: Boolean, action: (DownloadResult) -> Unit) {
        val dest = try {
            Files.createDirectories(destination)
            verifyDownload(link, overwrite)
        } catch (e: Exception) {
            action(DownloadResult.right(e))
            return
        }
        currentDownloads[link] = GlobalScope.launch(Dispatchers.IO) {
            val res: DownloadResult = try {
                TechUtilsClient.LOGGER.info("Downloading $link")
                withTimeout(timeout) {
                    downloadImpl(link).use { i ->
                        dest.outputStream(StandardOpenOption.CREATE).use { o -> i.copyTo(o) }
                    }
                }
                DownloadResult.left(dest)
            } catch (e: Exception) {
                TechUtilsClient.LOGGER.info("Failed to download $link:\n", e)
                DownloadResult.right(e)
            }
            action(res)
            currentDownloads.remove(link)
        }
    }

    private fun downloadImpl(link: String): InputStream {
        val connection = URL(link).openConnection().apply { connect() }
        return connection.getInputStream()
    }

    private fun verifyDownload(link: String, force: Boolean): Path {
        require(!currentDownloads.containsKey(link)) { "File is already downloading: $link" }
        val file = Path(URL(link).path)
        requireNotNull(file.fileName) { "Unable to identify the file: $file" }
        val name = file.fileName.name
        val dest = destination.resolve(name)
        require(!dest.exists() || force) { "File already exists: $name" }
        return dest
    }

    fun verifyLink(link: String): Boolean = try {
        URL(link)
        true
    } catch (e: MalformedURLException) {
        false
    }
}