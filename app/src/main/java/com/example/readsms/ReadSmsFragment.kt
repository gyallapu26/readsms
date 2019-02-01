package com.example.readsms


import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.example.readsms.adapter.CustomExpandableListAdapter
import kotlinx.android.synthetic.main.fragment_blank.*
import android.content.Intent
import com.example.readsms.services.ReadSMSForgroundService
import com.example.readsms.viewmodel.ReadSmsViewModel


class ReadSmsFragment : Fragment() {

    private lateinit var readSmsViewModel: ReadSmsViewModel
    private val  REQUEST_CODE_ASK_PERMISSIONS = 123
    private val recieve_sms_permission = 26
    private val customExpandableListAdapter : CustomExpandableListAdapter by lazy {
        CustomExpandableListAdapter(context!!, mutableListOf(), hashMapOf())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        schduleJob()
        initExpandableListView()
        readSmsViewModel = ViewModelProviders.of(this).get(ReadSmsViewModel::class.java)

        readSmsViewModel.getMessages().observe(this , Observer {

            it?.forEach { (i, mutableList) ->
                Log.d("sos", "hashmap  for key $i is $mutableList" )
                it?.let {
                    customExpandableListAdapter.updateList(it.keys, it)
                    expandGroup()
                }

            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(activity?.baseContext?.let { ContextCompat.checkSelfPermission(it, android.Manifest.permission.RECEIVE_SMS) } == PackageManager.PERMISSION_GRANTED)
            {

            }
            else requestPermissions(arrayOf(android.Manifest.permission.RECEIVE_SMS), recieve_sms_permission)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(activity?.baseContext?.let { ContextCompat.checkSelfPermission(it, android.Manifest.permission.READ_SMS) } == PackageManager.PERMISSION_GRANTED)
                loadSmsFromProvider()
            else requestPermissions(arrayOf(android.Manifest.permission.READ_SMS), REQUEST_CODE_ASK_PERMISSIONS)
        }else loadSmsFromProvider()
    }

    private fun schduleJob() {


    /*    val component = ComponentName(context, ExampleJobService::class.java)
        val uri = Uri.parse("content://sms")

        var triggerContentUri: JobInfo.TriggerContentUri? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            triggerContentUri = JobInfo.TriggerContentUri(
                uri,
                JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS
            )
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            JobInfo.Builder(1, component).addTriggerContentUri(triggerContentUri!!)
        } else {
            TODO("VERSION.SDK_INT < N")
        }

        val jobScheduler = context?.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val resultCode  = jobScheduler.schedule(builder.build())
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("sos", "Job scheduled")
        } else {
            Log.d("sos", "Job scheduling failed")
        }  */


        val serviceIntent = Intent(context, ReadSMSForgroundService::class.java)
       // serviceIntent.putExtra("inputExtra", "My input")

        ContextCompat.startForegroundService(context!!, serviceIntent)

    }

    private fun initExpandableListView() {
        expandableListView.setAdapter(customExpandableListAdapter)
    }
    private fun expandGroup(){
        for (i in 0 until customExpandableListAdapter.groupCount){
            expandableListView.expandGroup(i)
        }
    }


    private fun loadSmsFromProvider() {
        Log.d("sos" , "")
        readSmsViewModel.loadSmsFromProvider()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSmsFromProvider()
            }
            recieve_sms_permission -> {}
        }
    }


}