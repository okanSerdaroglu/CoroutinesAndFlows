package com.example.coroutinesandflows

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

/**
 * -- CompletableJob extends from Job. Technically it is a job, but it has more features added to it.
 *   these are complete and completeExceptionally. With this methods you can decide when the job ends
 *
 * -- the aim of this example, start a job in context and cancel only this job in the context
 *
 * CoroutineScope(IO).launch works in background thread, but
 *
 */

class MainActivity : AppCompatActivity() {
    private val TAG: String = "AppDebug"

    private val PROGRESS_MAX = 100
    private val PROGRESS_START = 0
    private val JOB_TIME = 4000 // ms
    private lateinit var job: CompletableJob
    private lateinit var jobProgressBar: ProgressBar
    private lateinit var jobButton: Button
    private lateinit var jobCompleteText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        jobProgressBar = findViewById(R.id.job_progress_bar)
        jobButton = findViewById(R.id.job_button)
        jobCompleteText = findViewById(R.id.job_complete_text)

        jobButton.setOnClickListener {
            if(!::job.isInitialized){
                initJob()
            }
            jobProgressBar.startJobOrCancel(job)
        }
    }

    private fun resetJob(){
        if(job.isActive || job.isCompleted){
            job.cancel(CancellationException("Resetting job"))
        }
        initJob()
    }

    private fun initJob(){
        jobButton.text = "Start Job #1"
        updateJobCompleteTextView("")
        job = Job()
        job.invokeOnCompletion {
            it?.message.let{
                var msg = it
                if(msg.isNullOrBlank()){
                    msg = "Unknown cancellation error."
                }
                Log.e(TAG, "$job was cancelled. Reason: $msg")
                showToast(msg)
            }
        }
        jobProgressBar.max = PROGRESS_MAX
        jobProgressBar.progress = PROGRESS_START
    }


    private fun ProgressBar.startJobOrCancel(job: Job){
        if(this.progress > 0){
            Log.d(TAG, "${job} is already active. Cancelling...")
            resetJob()
        }
        else{
            jobButton.text = "Cancel Job #1"
            CoroutineScope(IO + job).launch{
                Log.d(TAG, "coroutine $this is activated with job ${job}.")

                for(i in PROGRESS_START..PROGRESS_MAX){
                    delay((JOB_TIME / PROGRESS_MAX).toLong())
                    this@startJobOrCancel.progress = i
                }
                updateJobCompleteTextView("Job is complete!")
            }
        }
    }

    private fun updateJobCompleteTextView(text: String){
        GlobalScope.launch (Main){
            jobCompleteText.text = text
        }
    }

    private fun showToast(text: String){
        GlobalScope.launch (Main){
            Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}