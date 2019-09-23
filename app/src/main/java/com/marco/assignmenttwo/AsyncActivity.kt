package com.marco.assignmenttwo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
//import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class AsyncActivity: AppCompatActivity() {

    private fun meaninglessCounter(): Int {
        var counter = 0
        for (i in 1..100_000) {
            counter += 1
        }

        return counter
    }

//    private fun meaninglessCounterThread(): Int {
//        var counter = 0
//        for (i in 1..100_000) {
//            thread {
//                counter += 1
//            }
//        }
//
//        return counter
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_async)

        //Thread execution

//        val time1 = measureTimeMillis {
//
//            val time = measureTimeMillis {
//                val one = meaninglessCounterThread()
//                val two = meaninglessCounterThread()
//                println("The answer is ${one + two}")
//            }
//        }

        //Sequential execution.
        val time2 = measureTimeMillis {
            val one = meaninglessCounter()
            val two = meaninglessCounter()
            println("The answer is ${one + two}")
        }

        // Concurrent execution.
        val time3 = measureTimeMillis {
            val one = GlobalScope.async { meaninglessCounter() }
            val two = GlobalScope.async { meaninglessCounter() }
            runBlocking {
                println("The answer is ${one.await() + two.await()}")
            }
        }
        println("Sequential completed in $time2 ms. Concurrent completed in $time3 ms\n")
    }
}