package io.github.z3r0c00l_2k.aquadroid.utils

import java.text.SimpleDateFormat
import java.util.*

fun calculateIntake(weight: Int, workTime: Int): Double {
    return ((weight * 100 / 3.0) + (workTime / 6 * 7))
}
fun getCurrentDate(): String? {
    val c = Calendar.getInstance().time
    val df = SimpleDateFormat("dd-MM-yyyy")
    return df.format(c)
}
