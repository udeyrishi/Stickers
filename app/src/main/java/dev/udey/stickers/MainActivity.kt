package dev.udey.stickers

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlinx.android.synthetic.main.activity_main.launch_camera as launchCameraButton
import kotlinx.android.synthetic.main.activity_main.video_view as videoView


class MainActivity : AppCompatActivity(), CoroutineScope {
    // Ideally, the job should be scoped to the view model. But we're not using view models for this proof-of-concept demo.
    private var copyJob: Job? = null

    override val coroutineContext: CoroutineContext
        get() = copyJob?.let { Dispatchers.Main + it } ?: Dispatchers.Main

    companion object {
        private const val REQUEST_VIDEO_CAPTURE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launchCameraButton.setOnClickListener {
            dispatchTakeVideoIntent()
        }

        videoView.setOnPreparedListener { it.isLooping = true }
    }

    private fun dispatchTakeVideoIntent() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        val handled = if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            intent?.data?.let {
                videoView.setVideoURI(it)
                videoView.start()

                // Start the copy job on the background thread
                copyJob = async(Dispatchers.Default) {
                    copyVideo(context = this@MainActivity, source = it, destination = createOutputPath("copy.mp4"))
                }.also {
                    launch(Dispatchers.Default) {
                        it.await()
                        launch(Dispatchers.Main) {
                            // Once the "awaiting" finishes in the background, post the toast on the UI thread.
                            Toast.makeText(this@MainActivity, "File copied", LENGTH_LONG).show()
                        }
                    }
                }
                true
            } ?: false
        } else {
            false
        }

        if (!handled) {
            super.onActivityResult(requestCode, resultCode, intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        copyJob?.cancel()
        copyJob = null
    }
}
