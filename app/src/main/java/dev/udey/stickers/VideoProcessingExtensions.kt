/**
 * Copyright (c) 2019 Udey Rishi. All rights reserved.
 */
package dev.udey.stickers

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File

private const val LOG_TAG = "VIDEO_PROCESSING_EXTENSIONS"

private val Context.ffmpeg: FFmpeg
    get() = FFmpeg.getInstance(this)

/**
 * Simply creates a file path for the given file name in the root of the external storage.
  */
fun createOutputPath(fileName: String): String {
    val root = Environment.getExternalStorageDirectory().toString()
    val myDir = File(root)
    myDir.mkdirs()
    return File(myDir, fileName).toString()
}

/**
 * Ensure that [source] is actually a Video content URI, else it'll throw an exception.
 */
suspend fun CoroutineScope.copyVideo(context: Context, source: Uri, destination: String) {
    executeCmd(
        scope = this,
        context = context,
        command =  "-y -i ${source.getVideoPath(context)} $destination"
    )
}

private suspend fun executeCmd(scope: CoroutineScope, context: Context, command: String) {
    val cmd = command
        .split(" ")
        .map { it.trim() }
        .filterNot { it.isEmpty() }
        .toTypedArray()

    if (command.isEmpty()) {
        throw IllegalArgumentException("Not a valid ffmpeg command: $command")
    }

    val resultChannel = Channel<FfmpegCommandResult>()

    try {
        Log.i(LOG_TAG, "Starting command: $command")
        context.ffmpeg.execute(cmd, object : ExecuteBinaryResponseHandler() {
            override fun onSuccess(s: String?) {
                Log.i(LOG_TAG, "Command succeeded: $command")
                Log.i(LOG_TAG, "$s")
                scope.launch(Dispatchers.Default) {
                    resultChannel.send(FfmpegCommandResult.Success)
                }
            }

            override fun onFailure(s: String?) {
                Log.i(LOG_TAG, "Command failed: $command")
                Log.i(LOG_TAG, "$s")
                scope.launch(Dispatchers.Default) {
                    resultChannel.send(FfmpegCommandResult.Failure(RuntimeException("Command $command failed.")))
                }
            }
        })
    } catch (e: FFmpegCommandAlreadyRunningException) {
        Log.i(LOG_TAG, "Command failed because ffmpeg was already running a command: $command")
        scope.launch(Dispatchers.Default) {
            resultChannel.send(FfmpegCommandResult.Failure(e))
        }
    }

    return when (val result = resultChannel.receive()) {
        FfmpegCommandResult.Success -> Unit
        is FfmpegCommandResult.Failure -> throw result.e
    }
}

private sealed class FfmpegCommandResult {
    object Success : FfmpegCommandResult()
    data class Failure(val e: Throwable) : FfmpegCommandResult()
}

private fun Uri.getVideoPath(context: Context): String {
    val projection = arrayOf(MediaStore.Video.Media.DATA)
    return context.contentResolver.query(this, projection, null, null, null).use {
        it?.let { cursor ->
            val columnIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
            if (columnIndex >= 0) {
                cursor.moveToFirst()
                cursor.getString(columnIndex)
            } else {
                null
            }
        }
    } ?: throw IllegalArgumentException("Failed to get the file path for video with URI: $this")
}
