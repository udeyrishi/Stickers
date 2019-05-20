package dev.udey.stickers

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.launch_camera as launchCameraButton
import kotlinx.android.synthetic.main.activity_main.video_view as videoView


class MainActivity : AppCompatActivity() {
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
                true
            }
            false
        } else {
            false
        }

        if (!handled) {
            super.onActivityResult(requestCode, resultCode, intent)
        }
    }
}
