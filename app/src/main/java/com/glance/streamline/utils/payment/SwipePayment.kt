package com.glance.streamline.utils.payment

import com.SafeWebServices.PaymentGateway.PGSwipeController
import com.SafeWebServices.PaymentGateway.PGSwipeDevice
import com.SafeWebServices.PaymentGateway.PGSwipedCard

sealed class SwipeState
data class DeviceReady(val device: PGSwipeDevice) : SwipeState()
data class DeviceUnready(
    val device: PGSwipeDevice,
    val reason: PGSwipeDevice.ReasonUnreadyForSwipe
) : SwipeState()

data class DeviceConnected(val device: PGSwipeDevice) : SwipeState()
data class DeviceDisconnected(val device: PGSwipeDevice) : SwipeState()
data class DeviceActivated(val device: PGSwipeDevice) : SwipeState()
data class DeviceDeactivated(val device: PGSwipeDevice) : SwipeState()
data class SwipedCard(val card: PGSwipedCard, val device: PGSwipeDevice) : SwipeState()

class SwipePayment : PGSwipeController.SwipeListener {

    var onSwipeEvent: (SwipeState) -> Unit = {}

    /**
     * Is called when the swipe device is ready to accept a card swipe.
     */
    override fun onDeviceReadyForSwipe(device: PGSwipeDevice) {
        onSwipeEvent(DeviceReady(device))
    }

    /**
     * Is called when the swipe device is no longer ready to accept a card swipe.
     */
    override fun onDeviceUnreadyForSwipe(
        device: PGSwipeDevice,
        reason: PGSwipeDevice.ReasonUnreadyForSwipe
    ) {
        onSwipeEvent(DeviceUnready(device, reason))
    }

    /**
     * Is called when the swipe device is connected to the android device.
     */
    override fun onDeviceConnected(device: PGSwipeDevice) {
        onSwipeEvent(DeviceConnected(device))
    }

    /**
     * Is called when the swipe device is unplugged from the android device.
     */
    override fun onDeviceDisconnected(device: PGSwipeDevice) {
        onSwipeEvent(DeviceDisconnected(device))
    }

    /**
     * Is called when the swipe device is activated.
     * At this point, a swipe can be requested.
     */
    override fun onDeviceActivationFinished(device: PGSwipeDevice) {
        onSwipeEvent(DeviceActivated(device))
    }

    /**
     * Called when a swipe can no longer be requested.
     */
    override fun onDeviceDeactivated(device: PGSwipeDevice) {
        onSwipeEvent(DeviceDeactivated(device))
    }

    /**
     * Handles a card swipe event.
     */
    override fun onSwipedCard(card: PGSwipedCard, device: PGSwipeDevice) {
        onSwipeEvent(SwipedCard(card, device))
    }
}