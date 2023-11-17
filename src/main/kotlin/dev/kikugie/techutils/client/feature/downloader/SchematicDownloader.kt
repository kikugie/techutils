package dev.kikugie.techutils.client.feature.downloader

import dev.kikugie.techutils.client.util.litematica.InGameNotifier
import fi.dy.masa.litematica.data.DataManager
import fi.dy.masa.malilib.gui.Message
import kotlinx.coroutines.*
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.net.MalformedURLException
import java.net.URL

object SchematicDownloader {
    private val destination
        get() = DataManager.getSchematicsBaseDirectory().resolve("downloads")
    private val currentDownloads = mutableMapOf<String, Job>()

    @Throws(DownloadException::class)
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun download(link: String, force: Boolean = false, action: (File) -> Unit) {
        val file = verifyDownload(link, force)
        destination.mkdirs()
        val job = GlobalScope.launch {
            try {
                val content = downloadFile(link)
                file.writeBytes(content)
                action(file)
            } catch (e: Exception) {
                InGameNotifier.addMessage(Message.MessageType.ERROR, e.message ?: "techutils.download.downloadFail")
            } finally {
                currentDownloads.remove(link)
            }
        }
        currentDownloads[link] = job
    }

    private suspend fun downloadFile(url: String): ByteArray {
        return withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection()
            connection.connect()

            val inputStream = connection.getInputStream()
            inputStream.readBytes()
        }
    }

    private fun verifyDownload(link: String, force: Boolean): File {
        val url = verifyLink(link) ?: throw DownloadException("Invalid URL: $link")

        val filename = FilenameUtils.getName(url.path)
        val file = destination.resolve(filename)
        if (currentDownloads.containsKey(filename)) {
            throw DownloadException("File is already downloading: $file")
        }
        if ((file.exists() && !force)) {
            throw DownloadException("File already exists: $file")
        }
        return file
    }

    fun verifyLink(link: String): URL? {
        return try {
            URL(link)
        } catch (e: MalformedURLException) {
            null
        }
    }

    class DownloadException(message: String) : Exception(message)
}