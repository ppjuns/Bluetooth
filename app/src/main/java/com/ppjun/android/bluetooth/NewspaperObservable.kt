package com.ppjun.android.bluetooth

import java.util.*

class NewspaperObservable:Observable() {

   fun  notifyAllMan(info:String){
       setChanged()
       notifyObservers(info)
   }
}