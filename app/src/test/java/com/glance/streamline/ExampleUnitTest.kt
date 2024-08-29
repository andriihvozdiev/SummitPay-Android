package com.glance.streamline

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testMaybe() {
        Maybe.empty<Any>()
            .subscribe(
                { x: Any -> print("Emitted item: $x") },
                { ex: Throwable ->
                    println(
                        "Error: " + ex.message
                    )
                },
                { println("Completed. No items.") }
            )
    }

    @Test
    fun testRx() {
        println("Start")
        Observable.interval(1L, TimeUnit.SECONDS, Schedulers.io())
//            .map{ timeoutSeconds - it }
            .take(10)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { println("On next: $it") }
            .subscribe({
                println("Emitted item: $it")
            }, {
                println("Error: $it")
            })
    }
}
