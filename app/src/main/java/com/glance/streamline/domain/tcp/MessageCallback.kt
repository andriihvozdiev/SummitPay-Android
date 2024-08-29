package com.glance.streamline.domain.tcp

interface MessageCallback {
    /**
     * Method overriden in AsyncTask 'doInBackground' method while creating the TCPClient object.
     * @param message Received message from server app.
     */
    fun callbackMessageReceiver(message: String?)
}