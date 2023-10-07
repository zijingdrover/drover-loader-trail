package ai.drover.loader

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.segway.robot.service.AiBoxServiceManager
import com.segway.robot.service.BindStateListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private val url = "https://www.dropbox.com/scl/fi/qibsnhykidk97e3i87jx4/app-debug.apk?rlkey=zq8fa4le079q73r36hh1npu74&dl=1"
    private val outputFileName = "downloadedFile.apk"
    private var mIsBind = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AiBoxServiceManager.getInstance().bindService(this, object : BindStateListener {
            override fun onBind() {
                mIsBind = true
                Toast.makeText(this@MainActivity, "Service Bind", Toast.LENGTH_SHORT).show()
            }

            override fun onUnbind(reason: String) {
                mIsBind = false
                Toast.makeText(this@MainActivity, "Service Unbind", Toast.LENGTH_SHORT).show()
            }
        })
        downloadApk()
    }
    override fun onDestroy() {
        super.onDestroy()
        AiBoxServiceManager.getInstance().unbindService()
    }
    private fun downloadApk() = lifecycleScope.launch {
        try {
            val file = withContext(Dispatchers.IO) {
                Log.d(TAG, "Download started")
                val url = URL(this@MainActivity.url)
                Log.d(TAG, "Connecting to: ${url}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP $responseCode ${connection.responseMessage}")
                    return@withContext null
                }

                val file = File(getExternalFilesDir(null), outputFileName)
                Log.d(TAG, "File will be written to: ${file.absolutePath}")
                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(file)

                val data = ByteArray(4096)
                var count: Int
                while (inputStream.read(data).also { count = it } != -1) {
                    outputStream.write(data, 0, count)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                Log.d(TAG, "Download completed")
                file
            }

            if (file != null) {
                Toast.makeText(this@MainActivity, "Download successfully", Toast.LENGTH_LONG).show()
                installApk(file.absolutePath)
            } else {
                Log.e(TAG, "File is null, download was not successful")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error downloading APK", e)
        }
    }

    private fun installApk(path: String) {
        try {
            if (mIsBind) {
                val ret = AiBoxServiceManager.getInstance().packageManager.installApp(path)
                Log.d(TAG, "install ret: $ret")
            } else {
                Log.e(TAG, "Missing necessary permissions to install APK")
            }
        } catch (se: SecurityException) {
            Log.e(TAG, "Security Exception during installation", se)
        } catch (e: Exception) {
            Log.e(TAG, "Unknown Exception during installation", e)
        }
    }



}