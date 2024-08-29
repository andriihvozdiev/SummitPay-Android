package com.glance.streamline.domain.tcp


import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import com.glance.streamline.domain.tcp.TCPClient.Companion.ERROR
import com.glance.streamline.domain.tcp.TCPClient.Companion.SENT
import com.glance.streamline.domain.tcp.TCPClient.Companion.OPEN

class OpenTillDrawerAsyncTask(
    private var mHandler: Handler?,
    private var ipAddress: String,
    private var port: Int,
    private var command: String
): AsyncTask<String, String, TCPClient>() {

    private var tcpClient: TCPClient? = null
    private val TAG = "OpenTillDrawerAsyncTask"

    /**
     * Overriden method from AsyncTask class. There the TCPClient object is created.
     * @param params From MainActivity class empty string is passed.
     * @return TCPClient object for closing it in onPostExecute method.
     */
    override fun doInBackground(vararg params: String?): TCPClient? {
        Log.d(TAG, "In do in background")
        try {
            tcpClient = TCPClient(
                mHandler,
                command,
                ipAddress,
                port,
                object : MessageCallback {
                    override fun callbackMessageReceiver(message: String?) {
                        publishProgress(message)
                    }
                })
        } catch (e: NullPointerException) {
            Log.d(TAG, "Caught null pointer exception")
            e.printStackTrace()
        }
        tcpClient?.run()
        return null
    }


    /**
     * Overriden method from AsyncTask class. Here we're checking if server answered properly.
     * @param values If "restart" message came, the client is stopped and computer should be restarted.
     * Otherwise "wrong" message is sent and 'Error' message is shown in UI.
     */
    override fun onProgressUpdate(vararg values: String) {
        super.onProgressUpdate(*values)
        Log.d(TAG, "In progress update, values: $values")
        if (values[0] == "shutdown") {
            tcpClient!!.sendMessage(command)
            tcpClient!!.stopClient()
            mHandler!!.sendEmptyMessageDelayed(OPEN, 2000)
        } else {
            tcpClient!!.sendMessage("wrong")
            mHandler!!.sendEmptyMessageDelayed(ERROR, 2000)
            tcpClient!!.stopClient()
        }
    }


    override fun onPostExecute(result: TCPClient?) {
        super.onPostExecute(result)
        Log.d(TAG, "In on post execute")
        if (result != null && result.isRunning()) {
            result.stopClient()
        }
        mHandler!!.sendEmptyMessageDelayed(SENT, 4000)
    }

}