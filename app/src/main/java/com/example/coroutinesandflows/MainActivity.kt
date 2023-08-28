package com.example.coroutinesandflows

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * in this example we get a result from background thread ( this can be both network and room request )
 * then we send this result to main thread and show it in main thread
 * --
 * coroutines are not threads, they are some tasks on threads,in a thread there can be more than a coroutine
 * --
 * a suspend function should be called in a coroutine or another suspend function.suspend means a method can called from a coroutine
 * --
 * coroutine scope means, organize bunch of jobs together. For example, think that there are 5 different jobs
 * ( coroutines ) in a scope and we want to cancel all if one of them fails. if one of them fails we can
 * cancel all scope then all coroutines will be cancelled. This is scoping
 * --
 * IO - input output. Worker thread
 * Main - main thread
 * Default - heavy computational work ( filter a large list etc )
 * --
 * we can say that CoroutineScope.launch fires up a new coroutine and withContext not
 * starts a new coroutine just change the context ( Main, IO, Default )
 */

class MainActivity : AppCompatActivity() {

    private val RESULT_1 = "RESULT #1"
    private val RESULT_2 = "RESULT #2"

    lateinit var button: Button
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.button)
        textView = findViewById(R.id.textView)

        button.setOnClickListener {
            CoroutineScope(IO).launch {
                fakeApiRequest()
            }
        }
    }

    private suspend fun setTextOnMainThread(input: String) {
        /**
         * withContext switches the context what we mention. This means that a coroutine
         * start it's work in a thread, then it can work another thread, then continue
         * in another thread.
         */
        withContext(Main) {
            setNewText(input)
        }
    }

    private fun setNewText(input: String) {
        val newText = textView.text.toString() + "\n$input"
        textView.text = newText
    }

    /**
     * in fakeApiRequest method as you can see there are 2 different suspend function.
     * If you call them like this, they wait each other. Flow is like this
     *  1- getResult1FromApi
     *  2- print it
     *  3- set it on main thread
     *  4- send second request ( getResult2FromApi )
     *  5- set it on main thread
     */
    private suspend fun fakeApiRequest() {
        val result1 = getResult1FromApi()
        println("debug: $result1")
        /** textView.text = result1 -> this makes crash the app, because we are in worker thread and
         *  can not set text in a textView in worker thread, we need to change our work as
         *  UI thread
         */
        setTextOnMainThread(result1)
        val result2 = getResult2FromApi(result1) /** it waits for result1 and uses it */
        setTextOnMainThread(result2)
        /**
         * suspend functions in a same coroutine waits each other default
         * in here there are 2 different suspend function in a coroutine and they wait each other
         */
    }

    /**
     * suspend means that, it can be use in a coroutine, but it is not required to use it in a coroutine
     */
    private suspend fun getResult1FromApi():String {
        logThread("getResult1FromApi")
        delay(1000)
        /** delay and thread.sleep are totally different. if you make thread.sleep
         *  all coroutines in the thread will sleep. If you use delay only the current
         *  thread will sleep.
         */
        return RESULT_1
    }

    /** log the method name and current thread of method */
    private fun logThread(methodName: String) {
        println("debug: ${methodName}: ${Thread.currentThread().name}")
    }

    /** think that we send a request, get a result and send a new request according to this result
     *  for this we create the second method to simulate our second api request
     */

    private suspend fun getResult2FromApi(result1:String): String {
        logThread("getResult2FromApi")
        delay(1000)
        return RESULT_2
    }
}