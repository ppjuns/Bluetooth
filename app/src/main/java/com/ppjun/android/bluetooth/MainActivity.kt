package com.ppjun.android.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Observer
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.View
import cn.com.heaton.blelibrary.ble.Ble
import cn.com.heaton.blelibrary.ble.BleDevice
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.beacon.Beacon
import com.inuker.bluetooth.library.search.SearchRequest
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.*
import top.wuhaojie.bthelper.BtHelperClient
import top.wuhaojie.bthelper.OnSearchDeviceListener
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private val actionRequestEnable = 1
    var receiver: BroadcastReceiver? = null
    var bluetoothAdapter: BluetoothAdapter? = null
    var callback: BluetoothAdapter.LeScanCallback? = null
    var adapter: BlueAdapter? = null
    var scanCallback: ScanCallback? = null

    var gatt: BluetoothGatt? = null


    companion object {
        lateinit var client: BluetoothClient
        lateinit var helper: BtHelperClient

    }

    var list: ArrayList<BluetoothDevice> = ArrayList()
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        client = BluetoothClient(this)
        val observable = NewspaperObservable()//创建消息源
        observable.addObserver(ManObserver())//添加观察者
        observable.notifyAllMan("ckyg")//发送

        adapter = BlueAdapter(list)
        recyclerview.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        recyclerview.adapter = adapter

        //google文档推荐这种
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        // bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        if (bluetoothAdapter == null) {
            //not support
            return
        }




        startDiscover.setOnClickListener {

            if (requireNotNull(bluetoothAdapter?.isEnabled?.not())) {
                startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), actionRequestEnable)
            } else {
                //todo
                startLeScan()
            }
        }

        stopDiscover.setOnClickListener {

            //Log.i("debug=", bluetoothAdapter?.isDiscovering.toString())

            progress.visibility = View.GONE
            bluetoothAdapter?.cancelDiscovery()
            bluetoothAdapter?.stopLeScan(callback)

            if (scanCallback != null) {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            }

            client.stopSearch()
        }
        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)

            }
        }



        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (BluetoothDevice.ACTION_FOUND == intent?.action) {


                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                    //  Log.i("debug=", device.name + "\n" + device.address)


                    if (!TextUtils.isEmpty(device.name) && !TextUtils.isEmpty(device.address)) {


                        if (device.name.contains("ppjun")) {

                            // gatt=device.connectGatt(this@MainActivity,false,)

                        }
                        val blue = BlueTooth(device.name, device.address)

                        if (list.contains(device).not()) {
                            list.add(device)
                            adapter!!.notifyDataSetChanged()
                        }
                    }


                }
            }

        }


        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }


    fun startLeScan() {
        progress.visibility = View.VISIBLE
        val pairedDevices = bluetoothAdapter?.bondedDevices

        if (pairedDevices!!.size > 0) {

            for (device in pairedDevices) {

            }
        }


        callback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
            // Log.i("debug=", device.name ?: "null")
            if (device.name.isNullOrEmpty().not()) {
                val blue = BlueTooth(device.name, device.address)
                if (list.contains(device).not()) {
                    list.add(device)
                    adapter!!.notifyDataSetChanged()
                }

            }

        }


        val rxPermissions = RxPermissions(this)
        // Must be done during an initialization phase like onCreate
        rxPermissions
                .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe { granted ->
                    if (granted) {
                        //第一个库 用库扫描
                        val request = SearchRequest.Builder()
                                .searchBluetoothLeDevice(3000, 3)
                                .searchBluetoothClassicDevice(5000)
                                .searchBluetoothLeDevice(200)
                                .build()
                        client.search(request, object : SearchResponse {
                             override fun onSearchStopped() {
                                 progress.visibility = View.GONE
                             }

                             override fun onSearchStarted() {
                             }

                             override fun onDeviceFounded(device: SearchResult?) {

                                 val beacon = Beacon(device?.scanRecord)
                                 Log.i("debug=", beacon.toString())

                                 if (list.contains(device?.device).not()) {
                                     list.add(device?.device!!)
                                     adapter!!.notifyDataSetChanged()
                                 }


                             }

                             override fun onSearchCanceled() {
                                 progress.visibility = View.GONE
                             }

                         })


                        //第二个库

                        helper = BtHelperClient.from(this)
                     /*   helper.searchDevices(object : OnSearchDeviceListener {
                            override fun onNewDeviceFounded(result: BluetoothDevice?) {

                                if (list.contains(result).not()) {
                                    list.add(result!!)
                                    adapter!!.notifyDataSetChanged()
                                }
                            }

                            override fun onStartDiscovery() {
                            }

                            override fun onSearchCompleted(p0: MutableList<BluetoothDevice>?, p1: MutableList<BluetoothDevice>?) {
                            }

                            override fun onError(p0: Exception?) {
                            }

                        })*/


                        //第一种广播扫描法
                        // bluetoothAdapter?.startDiscovery()
                        //第二种很多设备运行法
                        // bluetoothAdapter?.startLeScan(callback)
                        //第三种 api19+的扫描
                        //ps 同一时间只有一种方法起作用
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            scanCallback =
                                    object : ScanCallback() {
                                        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                                        override fun onScanResult(callbackType: Int, result: ScanResult?) {
                                            super.onScanResult(callbackType, result)

                                            Log.i("debug=", result?.device!!.address + "===" + result?.device?.uuids?.size.toString())
                                            val blue = BlueTooth(result?.device!!.name
                                                    ?: result?.device.address, result?.device!!.address)
                                            if (list.contains(result?.device).not()) {
                                                list.add(result?.device)
                                                adapter!!.notifyDataSetChanged()
                                            }
                                        }

                                        @SuppressLint("NewApi")
                                        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                                        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                                            super.onBatchScanResults(results)


                                            for (name in results!!) {

                                                Log.i("list=", name.device!!.name
                                                        ?: name.device.address)

                                            }

                                        }

                                        override fun onScanFailed(errorCode: Int) {
                                            super.onScanFailed(errorCode)
                                        }
                                    }
                            // bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
                        }
                    } else {

                    }
                }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == actionRequestEnable) {

            if (resultCode == Activity.RESULT_OK) {
                startLeScan()
            }

        }
    }


    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)
    }

}
