package util

fun List<Int>.adjustElement(index: Int, offset: Int) =
    mapIndexed { idx, value -> if (index == idx) value + offset else value }