package com.glance.streamline.domain.tcp


import android.os.Handler
import android.util.Log
import java.io.*
import java.net.InetAddress
import java.net.Socket

class TCPClient(
    private var mHandler: Handler?,
    private var command: String?,
    private var ipNumber: String?,
    private var portNumber: Int,
    private var listener: MessageCallback?
) {
    private val TAG = "TCPClient"
    private  var incomingMessage: String? = null
    var input: BufferedReader? = null
    var out: PrintWriter? = null
    private var mRun = false

    companion object {
        const val CONNECTING = 0
        const val SENDING = 1
        const val SENT = 2
        const val OPEN = 3
        const val CLOSE = 3
        const val ERROR = -1
    }

    /**
     * Public method for sending the message via OutputStream object.
     * @param message Message passed as an argument and sent via OutputStream object.
     */
    fun sendMessage(message: String) {
        if (out != null && !out!!.checkError()) {
            out!!.println(message)
            out!!.flush()
            mHandler!!.sendEmptyMessageDelayed(SENDING, 1000)
            Log.d(TAG, "Sent Message: $message")
        }
    }

    /**
     * Public method for stopping the TCPClient object ( and finalizing it after that ) from AsyncTask
     */
    fun stopClient() {
        Log.d(TAG, "Client stopped!")
        mRun = false
    }

    fun isRunning(): Boolean {
        return mRun
    }


    fun run() {
        mRun = true
        try {
            // Creating InetAddress object from ipNumber passed via constructor from IpGetter class.
            val serverAddress: InetAddress = InetAddress.getByName(ipNumber)
            Log.d(TAG, "Connecting...")
            /**
             * Sending empty message with static int value from MainActivity
             * to update UI ( 'Connecting...' ).
             *
             * @see com.example.turnmeoff.MainActivity.CONNECTING
             */
            mHandler!!.sendEmptyMessageDelayed(CONNECTING, 1000)
            /**
             * Here the socket is created with hardcoded port.
             * Also the port is given in IpGetter class.
             *
             * @see com.example.turnmeoff.IpGetter
             */
            val socket = Socket(serverAddress, portNumber)
            try {

                // Create PrintWriter object for sending messages to server.
                out =
                    PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)

                //Create BufferedReader object for receiving messages from server.
                input = BufferedReader(InputStreamReader(socket.getInputStream()))
                Log.d(TAG, "In/Out created")

                //Sending message with command specified by AsyncTask
                sendMessage(command!!)

                //
                mHandler!!.sendEmptyMessageDelayed(SENDING, 2000)

                //Listen for the incoming messages while mRun = true
                while (mRun) {
                    incomingMessage = input!!.readLine()
                    if (incomingMessage != null && listener != null) {
                        /**
                         * Incoming message is passed to MessageCallback object.
                         * Next it is retrieved by AsyncTask and passed to onPublishProgress method.
                         *
                         */
                        listener!!.callbackMessageReceiver(incomingMessage)
                    }
                    incomingMessage = null
                }
                Log.d(TAG, "Received Message: $incomingMessage")
            } catch (e: Exception) {
                Log.d(TAG, "Error", e)
                mHandler!!.sendEmptyMessageDelayed(ERROR, 2000)
            } finally {
                out!!.flush()
                out!!.close()
                input?.close()
                socket.close()
                mHandler!!.sendEmptyMessageDelayed(SENT, 3000)
                Log.d(TAG, "Socket Closed")
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error", e)
            mHandler!!.sendEmptyMessageDelayed(ERROR, 2000)
        }
    }

}