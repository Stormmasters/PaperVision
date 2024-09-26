package io.github.deltacv.papervision.plugin

import com.github.serivesmejia.eocvsim.util.JavaProcess
import com.github.serivesmejia.eocvsim.util.SysUtil
import com.github.serivesmejia.eocvsim.util.loggerForThis
import com.google.gson.JsonElement
import io.github.deltacv.papervision.plugin.ipc.EOCVSimIpcEngine
import io.github.deltacv.papervision.serialization.PaperVisionSerializer
import io.github.deltacv.papervision.util.event.PaperVisionEventHandler
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor

/**
 * Manages and executes the PaperVision process in a dedicated daemon thread.
 *
 * This object is responsible for starting PaperVision within a static daemon thread, allowing it
 * to run in the background without blocking the main thread. The daemon thread will remain active
 * as long as PaperVision is running and will exit when PaperVision stops or the application shuts down.
 *
 * It is expected that the PaperVision process will be initialized and controlled through this object.
 */
object PaperVisionProcessRunner {

    val logger by loggerForThis()

    val paperVisionEngine = EOCVSimIpcEngine()

    val onPaperVisionExit = PaperVisionEventHandler("PaperVisionProcessRunner-OnPaperVisionExit")

    private val pool = Executors.newFixedThreadPool(1)

    var isRunning = false
        private set

    private var currentJob: Future<*>? = null

    fun execPaperVision(pluginJar: File) {
        if(isRunning) return

        isRunning = true

        currentJob = pool.submit {
            logger.info("Starting PaperVision process...")

            val classpath = pluginJar.absolutePath + File.pathSeparator + System.getProperty("java.class.path")
            val programParams = listOf("-q", "-p=${paperVisionEngine.server.port}")

            if(SysUtil.OS == SysUtil.OperatingSystem.MACOS) {
                logger.info("Running on macOS, adding -XstartOnFirstThread flag")
                JavaProcess.execClasspath(
                    EOCVSimIpcPaperVisionMain::class.java,
                    classpath,
                    listOf("-XstartOnFirstThread"),
                    programParams
                )
            } else {
                JavaProcess.execClasspath(
                    EOCVSimIpcPaperVisionMain::class.java,
                    classpath,
                    null,
                    programParams,
                )
            }

            onPaperVisionExit.run()

            logger.warn("PaperVision process has exited")

            isRunning = false
        }
    }

    fun stopPaperVision() {
        if(!isRunning) return

        currentJob?.cancel(true)
        isRunning = false
        currentJob = null
    }

}