/*
 * PaperVision
 * Copyright (C) 2024 Sebastian Erives, deltacv

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.deltacv.papervision.plugin.ipc.eocvsim

import com.github.serivesmejia.eocvsim.EOCVSim
import com.github.serivesmejia.eocvsim.pipeline.PipelineManager
import com.github.serivesmejia.eocvsim.pipeline.PipelineSource
import com.github.serivesmejia.eocvsim.util.loggerForThis
import io.github.deltacv.eocvsim.pipeline.StreamableOpenCvPipelineInstantiator
import io.github.deltacv.eocvsim.stream.ImageStreamer
import io.github.deltacv.papervision.plugin.PaperVisionProcessRunner
import io.github.deltacv.papervision.plugin.eocvsim.SinglePipelineCompiler

class EOCVSimPrevizSession(
    val sessionName: String,
    val eocvSim: EOCVSim,
    val streamer: ImageStreamer = NoOpEngineImageStreamer
) {

    var previzRunning = false
        private set

    private var latestClass: Class<*>? = null
    private var latestSourceCode: String? = null

    val logger by loggerForThis()

    init {
        var isChangingPipeline = false

        eocvSim.pipelineManager.onPipelineChange {
            if(latestClass == null) return@onPipelineChange

            if(isChangingPipeline) {
                isChangingPipeline = false
                return@onPipelineChange
            }

            val current = eocvSim.pipelineManager.currentPipeline ?: return@onPipelineChange

            if(previzRunning && current::class.java != latestClass) {
                // Temporarily disable the listener
                isChangingPipeline = true

                eocvSim.pipelineManager.forceChangePipeline(eocvSim.pipelineManager.getIndexOf(latestClass!!, PipelineSource.COMPILED_ON_RUNTIME))

                // Re-enable the listener after the change
                isChangingPipeline = false
            }
        }

        eocvSim.pipelineManager.onPause {
            if(previzRunning) {
                eocvSim.pipelineManager.setPaused(false, PipelineManager.PauseReason.NOT_PAUSED)
            }
        }

        PaperVisionProcessRunner.onPaperVisionExit.doOnce {
            stopPreviz()
        }
    }

    fun startPreviz(sourceCode: String) {
        previzRunning = true

        logger.info("Starting previz session $sessionName with streamer ${streamer.javaClass.simpleName}")

        refreshPreviz(sourceCode)
    }

    fun refreshPreviz(sourceCode: String) {
        if(!previzRunning) return

        eocvSim.pipelineManager.onUpdate.doOnce {
            logger.info("Refreshing previz session $sessionName with new source code")

            val beforeClass = latestClass

            latestClass = SinglePipelineCompiler.compilePipeline(sessionName, sourceCode)

            latestSourceCode = sourceCode

            eocvSim.pipelineManager.pipelines.removeAll { it.clazz == beforeClass }
            eocvSim.pipelineManager.refreshGuiPipelineList()

            eocvSim.pipelineManager.onUpdate.doOnce {
                eocvSim.pipelineManager.addPipelineClass(latestClass!!, PipelineSource.COMPILED_ON_RUNTIME)
                eocvSim.pipelineManager.addInstantiator(latestClass!!, StreamableOpenCvPipelineInstantiator(streamer))

                eocvSim.pipelineManager.forceChangePipeline(
                    eocvSim.pipelineManager.getIndexOf(
                        latestClass!!,
                        PipelineSource.COMPILED_ON_RUNTIME
                    )
                )
            }
        }
    }

    fun ensurePrevizPipelineRunning() {
        eocvSim.onMainUpdate.doOnce {
            if(eocvSim.pipelineManager.currentPipeline?.javaClass?.name != sessionName && latestSourceCode != null) {
                refreshPreviz(latestSourceCode!!)
            }
        }
    }

    fun stopPreviz() {
        if(!previzRunning) return
        previzRunning = false

        logger.info("Stopping previz session $sessionName")

        if(streamer is EOCVSimEngineImageStreamer) {
            streamer.stop()
        }

        eocvSim.onMainUpdate.doOnce {
            eocvSim.pipelineManager.pipelines.removeAll { it.clazz == latestClass }
            eocvSim.pipelineManager.refreshGuiPipelineList()

            eocvSim.pipelineManager.forceChangePipeline(0)
        }
    }

}