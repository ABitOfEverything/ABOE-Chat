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

    private external fun initialise(): Boolean
    private external fun showNotificationNative(logoPath: String, header: String, message: String)

    private val logoPath: String

    init {
        val logger: Logger = LogManager.getLogger(this::class.qualifiedName)

        logger.warn("Running on Java {}", System.getProperty("java.version"))

        val fileName: String = "WindowsToastNotifier-Native.dll"

        logger.info("Loading library: {}", fileName)

        val resourceStream: InputStream = this::class.java.classLoader.getResourceAsStream(fileName)
        val tempFile: File = File.createTempFile("WindowsToastNotifier-Native", ".dll")
        val tempNativeOutputStream: OutputStream = FileOutputStream(tempFile)

        IOUtils.copy(resourceStream, tempNativeOutputStream)

        resourceStream.close()
        tempNativeOutputStream.close()

        System.load(tempFile.absolutePath)

        val logoResourceStream: InputStream = this::class.java.classLoader.getResourceAsStream("assets/aboe_logo.png")
        val tempLogoFile: File = File.createTempFile("aboe-logo", ".png")
        val tempLogoOutputStream: OutputStream = FileOutputStream(tempLogoFile)

        IOUtils.copy(logoResourceStream, tempLogoOutputStream)

        logoResourceStream.close()
        tempLogoOutputStream.close()

        logoPath = tempLogoFile.absolutePath

        logger.info("Initialising native library...")

        initialised = initialise()

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

        showNotificationNative(logoPath, header, message)
    }
}