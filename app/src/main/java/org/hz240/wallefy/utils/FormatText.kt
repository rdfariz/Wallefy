package org.hz240.wallefy.utils

class FormatText {
    fun printStatusLock(lock: Boolean): String {
        if (lock == true) {
            return "Komunitas Terkunci"
        }else {
            return "Komunitas Tidak Terkunci"
        }
    }
}