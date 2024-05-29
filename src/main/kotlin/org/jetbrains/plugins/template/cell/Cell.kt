package org.jetbrains.plugins.template.cell

fun colIdByReference(header: String) = header.fold(0) { acc, char -> (char - 'A' + 1) + acc * 26 } - 1
fun colReferenceById(idx: Int): String =
    if (idx < 26)
        ('A' + idx).toString()
    else
        colReferenceById(idx / 26 - 1) + ('A' + idx % 26).toString()