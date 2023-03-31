package com.example.coroutinesandflows

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

/**
 * think that a network request is too long, we can cancel it with timeout handling in coroutines
 * --
 *
 */

class MainActivity : AppCompatActivity() {

    private val RESULT_1 = "RESULT #1"
    private val RESULT_2 = "RESULT #2"
    private val JOB_TIMEOUT = 1900L

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
        withContext(Main) {
            setNewText(input)
        }
    }

    private fun setNewText(input: String) {
        val newText = textView.text.toString() + "\n$input"
        textView.text = newText
    }

    private suspend fun fakeApiRequest() {
        withContext(IO) {
            val job = withTimeoutOrNull(JOB_TIMEOUT) {

                val result1 = getResult1FromApi() // wait
                setTextOnMainThread("Got $result1")

                val result2 = getResult2FromApi() // wait
                setTextOnMainThread("Got $result2")
                /**
                 * we need 2 seconds for 2 methods because they waits each other. but we have 1900 ms timeout
                 * if job gets more then timeout job will be null
                 */
                /**
                 * we need 2 seconds for 2 methods because they waits each other. but we have 1900 ms timeout
                 * if job gets more then timeout job will be null
                 */
            }

            if (job == null){
                val cancelMessage = "Cancelling job... Job took longer than $JOB_TIMEOUT ms"
                setTextOnMainThread(cancelMessage)
            }
        }
    }

    private suspend fun getResult1FromApi():String {
        logThread("getResult1FromApi")
        delay(1000)
        return RESULT_1
    }

    private suspend fun getResult2FromApi():String {
        logThread("getResult2FromApi")
        delay(1000)
        return RESULT_2
    }

    private fun logThread(methodName: String) {
        println("debug: ${methodName}: ${Thread.currentThread().name}")
    }
}