package com.marco.assignmenttwo

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_homer.*
import kotlinx.android.synthetic.main.activity_homer.result
import kotlinx.android.synthetic.main.activity_write_homer.*
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis


/**
 * Actor requirements, based on: https://github.com/kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/test/guide/example-sync-07.kt
 * The CounterMsg is wholly "copied" as the task of counter incrementation is what was required.
 *
 * Do note that this implementations of actors is regarded obsolete.
 *
 */

// Message types for counterActor
//sealed class CounterMsg
//object IncCounter : CounterMsg() // one-way message to increment counter
//class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg() // a request with reply

//internal val Background = newFixedThreadPoolContext(2, "bg")

// This function launches a new counter actor
//fun CoroutineScope.counterActor() = actor<CounterMsg> {
//    var counter = 0 // actor state
//    for (msg in channel) { // iterate over incoming messages
//        when (msg) {
//            is IncCounter -> counter++
//            is GetCounter -> msg.response.complete(counter)
//        }
//    }
//}

//End of actor requirements.

class HomerWriteActivity : AppCompatActivity() {

    /**
     * Actor stuff
     */

    suspend fun coroutineSearchForActor(hayStack: List<String>, action: suspend () -> Unit) {
        coroutineScope { // scope for coroutines
            repeat(hayStack.size) {
                launch {
                    action()
                }
            }
        }
    }


    /**
     * button count functions
     */
    fun writeUsingActors(homerList: List<String>) {
        result.text = result.text as String + "Beginning count using actors\n"
        val time = measureTimeMillis {
            runBlocking<Unit> {
                val counter = counterActor()
                withContext(Dispatchers.Default) {
//                    coroutineSearchForActor(searchItem, homerList) {
                    coroutineSearchForActor(homerList) {
                        val response = CompletableDeferred<Int>()
                        var currentResponse = response.await()
                        counter.send(IncCounter)
                        openFileOutput("$currentResponse.txt", Context.MODE_PRIVATE).use {
                            it.write(homerList[currentResponse].toByteArray())
                        }
                        deleteFile("$currentResponse.txt")
                    }
                }
                // send a message to get a counter value from an actor
                val response = CompletableDeferred<Int>()
                counter.send(GetCounter(response))
                result.text = result.text as String + "Created and deleted ${response.await()} files in "
                counter.close() // shutdown the actor
            }
        }
        result.text = result.text as String + "${time}ms\n"
    }

    fun writeUsingCoroutine(homerList: List<String>, scope: CoroutineScope) {
        result.text = result.text as String + "Beginning write using co-routines\n"

        val deferred = (homerList.indices).map {index ->
            scope.async {
                openFileOutput("$index.txt", Context.MODE_PRIVATE).use {
                    it.write(homerList[index].toByteArray())
                }
                deleteFile("$index.txt")
            }
        }

        val time = measureTimeMillis {
            runBlocking {
                deferred
            }
        }

        result.text = result.text as String + "Finished co-routines write in ${time}ms\n"
    }

    fun writeUsingThreads(homerList: List<String>) {
        result.text = result.text as String + "Beginning write using threads\n"

        var availableProcessors = Runtime.getRuntime().availableProcessors()
//        if(availableProcessors > 2) availableProcessors-- //since don't want to crowd UI thread.

        var writeList = homerList.chunked(availableProcessors)

        val time = measureTimeMillis {
            val executor = Executors.newFixedThreadPool(availableProcessors)

            writeList.forEachIndexed { outerIndex, outerElement ->
                executor.execute(
                    thread {
                        outerElement.forEachIndexed {index, element ->
                            openFileOutput("$outerIndex$index.txt", Context.MODE_PRIVATE).use {
                                it.write(element.toByteArray())
                            }
                        }
                    }
                )
            }
            writeList.forEachIndexed { outerIndex, outerElement ->
                executor.execute(
                    thread {
                        outerElement.forEachIndexed {index, _ ->
                            deleteFile("$outerIndex$index.txt")
                        }
                    }
                )
            }
            executor.shutdown()
            while(!executor.isTerminated) { /* Do nothing while waiting on shutdown.*/}
        }

        result.text = result.text as String + "Completed Thread write and delete in ${time}ms\n"
    }

    fun writeUsingUIThread(homerList: List<String>) {
        result.text = result.text as String + "Beginning write on UI thread\n"

        val time = measureTimeMillis {
            homerList.forEachIndexed {index, element ->
                openFileOutput("$index.txt", Context.MODE_PRIVATE).use {
                    it.write(element.toByteArray())
                }
            }
            //delete
            homerList.forEachIndexed {index, _ ->
                deleteFile("$index.txt")
            }
        }

        result.text = result.text as String + "Completed UI write and delete in ${time}ms\n"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_homer)

        val homerList: List<String> =
            application.assets.open("Illiad-Homer.txt").bufferedReader().readLines()

        buttonWriteAll.setOnClickListener {
            writeUsingUIThread(homerList)
            writeUsingThreads(homerList)
            writeUsingCoroutine(homerList, GlobalScope)
//            writeUsingActors(homerList)
        }

        buttonWriteUIThread.setOnClickListener {
            writeUsingUIThread(homerList)
        }

        buttonWriteThread.setOnClickListener {
            writeUsingThreads(homerList)
        }

        buttonWriteCoRoutine.setOnClickListener {
            writeUsingCoroutine(homerList, GlobalScope)
        }

        buttonWriteActor.setOnClickListener {
//            result.text = "Actors is disabled as couldn't get it running nicely."
            writeUsingActors(homerList)
        }
    }
}


