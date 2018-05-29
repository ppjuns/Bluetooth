# Bluetooth
蓝牙扫描demo


![](https://s1.ax1x.com/2018/05/29/C4vhZD.png)

这个demo 包括 判断蓝牙 是否可用，判断蓝牙权限是否授权，判断蓝牙是否开启。
然后扫描蓝牙 返回蓝压设备。
使用系统的三种方式扫描获取蓝牙设备，包括
1.  bluetoothAdapter?.startDiscovery()（广播方式扫描常规蓝牙和低功耗蓝牙）
1.  bluetoothAdapter?.startLeScan(callback)（常规蓝牙和低功耗蓝牙，最常用的）
1.  bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback) （api21+）

然后通过gatt连接设备，最后在回调的 onConnectionStateChange获取链接回调。
再gatt?.discoverServices() 获取服务 ，然后遍历服务获取特征uuid 或者 描述，之后做read 或者notify的操作。
