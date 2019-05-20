/**
 * Copyright (c) 2019 Udey Rishi. All rights reserved.
 */
package dev.udey.stickers

import android.app.Application
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initFFmpeg()
    }

    private fun initFFmpeg() {
        val ffmpeg = FFmpeg.getInstance(applicationContext)
        ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
            override fun onSuccess() {
                // FFmpeg is supported by device
            }
        })
        // This might throw FFmpegNotSupportedException if the device does not support FFMpeg
    }
}
