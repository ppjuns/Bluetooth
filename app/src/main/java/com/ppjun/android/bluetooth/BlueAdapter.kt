package com.ppjun.android.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inuker.bluetooth.library.Code.REQUEST_SUCCESS
import com.inuker.bluetooth.library.connect.options.BleConnectOptions
import com.inuker.bluetooth.library.connect.response.BleConnectResponse
import com.inuker.bluetooth.library.model.BleGattProfile
import kotlinx.android.synthetic.main.blue_item.view.*
import top.wuhaojie.bthelper.MessageItem
import top.wuhaojie.bthelper.OnSendMessageListener
import java.lang.Exception

class BlueAdapter(var list: ArrayList<BluetoothDevice>) : RecyclerView.Adapter<BlueAdapter.BlueViewHolder>() {

    var gattConnect: BluetoothGatt? = null
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlueViewHolder {
        context = parent.context
        return BlueViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.blue_item, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }


    fun close(gatt: BluetoothGatt?) {
        gatt?.disconnect()
        gatt?.close()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: BlueViewHolder, position: Int) {

        holder.blueName.text = list[position].name ?: list[position].address
        holder.blueMac.text = list[position].address


        val gattCallback = object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        gatt?.discoverServices()
                        Log.i("debug=", "Start to discover services.")
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.i("debug=", "Connection is broken.")
                        close(gatt)
                        gattConnect?.disconnect()
                        gattConnect?.close()
                        gattConnect = null
                    }
                    else -> {
                        close(gatt)
                    }
                }
            }


            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        for (service in gatt?.services!!) {
                            for (characteristic in service.characteristics) {
                                Log.i("debug=", characteristic.uuid.toString())
                            }
                        }
                    }

                }
            }


        }

        val option = BleConnectOptions.Builder()
                .setConnectRetry(3)
                .setConnectTimeout(30000)
                .setServiceDiscoverRetry(3)
                .setServiceDiscoverTimeout(20000)
                .build()
        holder.itemView.setOnClickListener {


//            MainActivity.client.connect(list[position].address) { code, data ->
//                if (code == REQUEST_SUCCESS) {
//                    Log.i("debug=", "success")
//                } else {
//                    Log.i("debug=", "else")
//                }
//            }

            val item=MessageItem("asd")

            MainActivity.helper.sendMessage(list[position].address,item,true,object:OnSendMessageListener{
                override fun onSuccess(p0: Int, p1: String?) {
                    Log.i("debug=",p1.toString()+p0)

                }

                override fun onConnectionLost(p0: Exception?) {
                    Log.i("debug=",p0?.message)
                }

                override fun onError(p0: Exception?) {
                    Log.i("debug=",p0?.message)
                }

            })




            //  gattConnect = list[position].connectGatt(context, false, gattCallback)
        }
    }

    class BlueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val blueName = itemView.blueName
        val blueMac = itemView.blueMac
    }
}