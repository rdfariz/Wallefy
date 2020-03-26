package org.hz240.wallefy.utils

import java.text.NumberFormat
import java.util.*

class Converter {
    companion object {
        fun rupiah(number: Any?): String{
            val localeID =  Locale("in", "ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID)
            var print = number.toString()
            if (number is Long && number != null) {
                print = numberFormat.format(number.toDouble()).toString()
            }
            return print
        }
    }
}