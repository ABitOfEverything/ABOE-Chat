package me.glorantq.aboe.chat.client.notification

import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class WindowsToastNotifier : INotifier {
    private val initialised: Boolean

    private external fun initialise(logoPath: String): Boolean
    private external fun showNotificationNative(header: String, message: String)

    init {
        val logger: Logger = LogManager.getLogger(this::class.qualifiedName)

        logger.warn("Running on Java {}", System.getProperty("java.version"))

        val arch: String = if(System.getProperty("os.arch") == "amd64") { "64" } else { "86" }
        val fileName: String = "WindowsToastNotifier-Native-x$arch.dll"

        logger.info("Loading library: {}", fileName)

        val resourceStream: InputStream = this::class.java.classLoader.getResourceAsStream(fileName)
        val tempFile: File = File.createTempFile("WindowsToastNotifier-Native", ".dll")
        val tempNativeOutputStream: OutputStream = FileOutputStream(tempFile)

        IOUtils.copy(resourceStream, tempNativeOutputStream)

        resourceStream.close()
        tempNativeOutputStream.close()

        System.load(tempFile.absolutePath)

        val logoResourceStream: InputStream = this::class.java.classLoader.getResourceAsStream("assets/aboe.png")
        val tempLogoFile: File = File.createTempFile("aboe-logo", ".png")
        val tempLogoOutputStream: OutputStream = FileOutputStream(tempLogoFile)

        IOUtils.copy(logoResourceStream, tempLogoOutputStream)

        logoResourceStream.close()
        tempLogoOutputStream.close()

        logger.info("Initialising native library...")

        initialised = initialise(tempLogoFile.absolutePath)

        if(!initialised) {
            logger.error("Failed to initialise native library!")
        } else {
            logger.info("Initialised native library!")
        }
    }

    override fun showNotification(header: String, message: String) {
        if(!initialised) {
            return
        }

        showNotificationNative(header, message)
    }
}