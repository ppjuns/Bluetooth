package com.ppjun.android.bluetooth

import android.util.Log
import java.util.*

class ManObserver :Observer {
    override fun update(o: Observable?, arg: Any?) {
        Log.i("debug=",arg.toString())
    }
}