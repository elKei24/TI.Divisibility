/**
 * An implementation of a DFA that decides if a binary number (read from left to right) is dividable by a given number.
 */
class BinaryDivisibilityDFA(val divisionBy : Int) {
    /**
     * rest describes the rest class of the number read so far.
     * It represents the state in which the DFA would be.
     * rest can have values between 0 and (divisionBy - 1)
     */
    var rest = 0 //state R_0 at beginning

    init {
        if (divisionBy <= 0) throw IllegalArgumentException("divisionBy be positive")
    }

    fun read(symbol : Boolean) : Boolean {
        rest = (rest * 2 + if (symbol) 1 else 0) % divisionBy //change state to something in {R_0, ..., R_divisionBy}
        return isAccepted()
    }

    fun read(word : BooleanArray) : Boolean {
        for (symbol in word) read(symbol)
        return isAccepted()
    }

    fun isAccepted() : Boolean {
        return rest == 0
    }
}