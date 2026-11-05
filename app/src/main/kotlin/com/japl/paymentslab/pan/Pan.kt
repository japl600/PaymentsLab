package com.japl.paymentslab.pan

private const val MIN_PAN_LENGTH = 12
private const val MAX_PAN_LENGTH = 19

// ASCII digits only: isDigit() would also accept Unicode digits.
private fun isPanShaped(pan: String): Boolean =
    pan.length in MIN_PAN_LENGTH..MAX_PAN_LENGTH && pan.all { it in '0'..'9' }

/** "4111111111111111" -> "411111******1111". Throws IllegalArgumentException if it does not look like a PAN. */
fun maskPan(pan: String): String {
    // Never put the PAN in the message: exceptions end up in logs.
    require(isPanShaped(pan)) { "Invalid PAN: expected $MIN_PAN_LENGTH-$MAX_PAN_LENGTH digits" }
    return pan.take(6) + "*".repeat(pan.length - 10) + pan.takeLast(4)
}

/** Luhn check. Returns false (does not throw) if the input is not shaped like a PAN. */
fun isValidLuhn(pan: String): Boolean {
    if (!isPanShaped(pan)) return false

    var sum = 0
    var doubleIt = false                      // the last digit (check digit) is not doubled
    for (i in pan.indices.reversed()) {
        var d = pan[i] - '0'
        if (doubleIt) {
            d *= 2
            if (d > 9) d -= 9
        }
        sum += d
        doubleIt = !doubleIt
    }
    return sum % 10 == 0
}
