package com.marco.assignmenttwo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ThreadActivity: AppCompatActivity() {

    fun thread(
        start: Boolean = true,
        isDaemon: Boolean = false,
        contextClassLoader: ClassLoader? = null,
        name: String? = null,
        priority: Int = -1,
        block: () -> Unit
    ) {}


    class SimpleThread: Thread() {
        public override fun run() {
            println("${Thread.currentThread()} has run.")
        }
    }


//    class SimpleRunnable(list: List<String>, searchItem: String) : Runnable {
//        public override fun run() {
//            println("${Thread.currentThread()} has run.")
//        }
//    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thread)

//        val thread = ThreadActivity.SimpleThread()
//        thread.start()
//
//        val runnable = ThreadActivity.SimpleRunnable(searchLists[1], searchItem)
//        val thread1 = Thread(runnable)
//        thread1.start()



//
//        thread {
//            println("${Thread.currentThread()} has run.")
//        }
//
//        var counter = 0
//        val numberOfThreads = 1_000_000
//
//        val time = measureTimeMillis {
//            for (i in 1..numberOfThreads) {
//                thread {
//                    counter += 1
//                }
//            }
//        }
//
//        val time = measureTimeMillis {
//            runBlocking {
//                repeat(100_000) {
////                for(i in 1..numberOfThreads) {
//                    launch {
//                        counter += 1
//                    }
//                }
//            }
//        }
//
//        println("Created $numberOfThreads threads in ${time}ms.")

        var counter = 0

        val forTime = measureTimeMillis {
            runBlocking {
                for(i in 1 .. 1_000_000) {
                    launch {
                        counter += 1
                    }
                }
            }
        }

        counter = 0

        val repeatTime = measureTimeMillis {
            runBlocking {
                repeat(1_000_000) {
                    launch(Dispatchers.Default) {
                        counter += 1
                    }
                }
            }
        }

//        println("Repeat func time: $repeatTime")
        println("Repeat func time: $repeatTime vs For loop time: $forTime")
    }
}