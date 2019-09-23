package com.marco.assignmenttwo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_homer.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
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
sealed class CounterMsg
object IncCounter : CounterMsg() // one-way message to increment counter
class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg() // a request with reply

//internal val Background = newFixedThreadPoolContext(2, "bg")

// This function launches a new counter actor
fun CoroutineScope.counterActor() = actor<CounterMsg> {
    var counter = 0 // actor state
    for (msg in channel) { // iterate over incoming messages
        when (msg) {
            is IncCounter -> counter++
            is GetCounter -> msg.response.complete(counter)
        }
    }
}

//End of actor requirements.

class HomerReadActivity : AppCompatActivity() {

    /**
     * Core Logic for counting occurrences.
     */
    fun countWord(needle: String, hayStack: String): Int {
        return hayStack.windowed (needle.length)
            .filter {it == needle}
            .count()
    }

    /**
     * Actor stuff
     */
    suspend fun coroutineSearchForActor(needle: String, hayStack: List<String>, action: suspend () -> Unit) {
        coroutineScope { // scope for coroutines
            for (stringItem in hayStack) {
                launch {
                    val count = countWord(needle, stringItem)
                    repeat(count) {
                        action()
                    }
                }
            }

        }
    }

    /**
     * button count functions
     */
    fun countUsingActors(homerList: List<String>, searchItem: String) {
        result.text = result.text as String + "Beginning count using actors\n"
        val time = measureTimeMillis {
            runBlocking<Unit> {
                val counter = counterActor()
                withContext(Dispatchers.Default) {
                    coroutineSearchForActor(searchItem, homerList) {
                        counter.send(IncCounter)
                    }
                }
                // send a message to get a counter value from an actor
                val response = CompletableDeferred<Int>()
                counter.send(GetCounter(response))
                result.text = result.text as String + "Found: ${response.await()} in "
                counter.close() // shutdown the actor
            }
        }
        result.text = result.text as String + "${time}ms\n"
    }

    fun countUsingCoroutine(homerList: List<String>, searchItem: String, scope: CoroutineScope) {
        result.text = result.text as String + "Beginning count using co-routines\n"

        val deferred = (homerList.indices).map {
            scope.async {
                countWord(searchItem, homerList[it])
            }
        }

        var count = -1
        val time = measureTimeMillis {
            runBlocking {
                count = deferred.sumBy {
                    it.await()
                }
            }
        }

        result.text = result.text as String + "Found: $count in ${time}ms\n"
    }

    fun countUsingThreads(homerList: List<String>, searchItem: String) {
        result.text = result.text as String + "Beginning count using threads\n"
        var counter = -1

        val time = measureTimeMillis {
            thread {
                counter = 0
                repeat (homerList.size - 1) {i ->
                    counter += countWord(searchItem, homerList[i])
                }
            }.join()
        }

        result.text = result.text as String + "Found $counter in ${time}ms\n"
    }

    fun countUsingUIThread(homerList: List<String>, searchItem: String) {
        result.text = result.text as String + "Beginning count using UI thread\n"

        var counter = 0
        val time = measureTimeMillis {
            repeat (homerList.size - 1) {i ->
                counter += countWord(searchItem, homerList[i])
            }
        }

        result.text = result.text as String + "Found: $counter in ${time}ms\n"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homer)

        val homerList: List<String> =
            application.assets.open("Illiad-Homer.txt").bufferedReader().readLines()

        buttonRunAll.setOnClickListener {
            val searchItem = homerSearchInput.text.toString()
            countUsingUIThread(homerList, searchItem)
            countUsingThreads(homerList, searchItem)
            countUsingCoroutine(homerList, searchItem, GlobalScope)
            countUsingActors(homerList, searchItem)
        }

        buttonCountUIThread.setOnClickListener {
            val searchItem = homerSearchInput.text.toString()
            countUsingUIThread(homerList, searchItem)
        }

        buttonCountThread.setOnClickListener {
            val searchItem = homerSearchInput.text.toString()
            countUsingThreads(homerList, searchItem)
        }

        buttonCountCoRoutine.setOnClickListener {
            val searchItem = homerSearchInput.text.toString()
            countUsingCoroutine(homerList, searchItem, GlobalScope)
        }

        buttonCountActor.setOnClickListener {
            val searchItem = homerSearchInput.text.toString()
            countUsingActors(homerList, searchItem)
        }
    }
}


