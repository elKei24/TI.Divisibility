import java.io.File
import java.text.DateFormat
import java.text.ParseException
import java.util.*
import kotlin.collections.ArrayList

private enum class ParamState {
    none,
    born,
    fileIn,
    divisionBy
}

fun main(args: Array<String>) {
    var state = ParamState.none
    var born = Calendar.getInstance()
    born.set(1998, Calendar.FEBRUARY, 24)
    var fileIn: String? = null
    var input: String? = null
    var divisionBy = 5

    //read parameters
    try {
        for (arg in args) when (state) {
            ParamState.none -> state = when (arg.toLowerCase()) {
                "-b", "--born" -> ParamState.born
                "-i", "--in" -> ParamState.fileIn
                "-d", "--divby" -> ParamState.divisionBy
                "-h", "--help" -> {
                    printHelp()
                    return
                }
                else -> {
                    if (input == null) input = "" else input += '\n'
                    input += arg.toUpperCase()
                    ParamState.none
                }
            }
            ParamState.fileIn -> {
                fileIn = arg
                state = ParamState.none
            }
            ParamState.born -> {
                born = Calendar.getInstance()
                born.time = DateFormat.getDateInstance().parse(arg)
                state = ParamState.none
            }
            ParamState.divisionBy -> {
                divisionBy = try {
                    Integer.parseInt(arg)
                } catch (nfe : NumberFormatException) {
                    -1
                }
                if (divisionBy <= 0) {
                    System.err.println("The divisionBy parameter has to be a postive integer.")
                    return
                }
                state = ParamState.none
            }
        }
    } catch (pe: ParseException) {
        System.err.println("Incorrect birth date: ${pe.localizedMessage}")
        return
    }

    //get input
    val inputLines: List<String> =
            if (input != null) {
                if (fileIn != null) System.out.println("Ignoring input file as there is an input string given.")
                input.split('\n')
            } else if (fileIn != null) {
                try {
                    readInputFile(fileIn)
                } catch (e: RuntimeException) {
                    System.err.println("Failed to read file: ${e.localizedMessage}")
                    return@main
                }
            } else {
                System.err.println("No input string or input file given.")
                return@main
            }

    //convert birthday
    val bornDigits = ByteArray(8)
    toDigits(born.get(Calendar.DAY_OF_MONTH), 2).forEachIndexed { index, digit -> bornDigits[index] = digit }
    toDigits(born.get(Calendar.MONTH) + 1, 2).forEachIndexed { index, digit -> bornDigits[index + 2] = digit }
    toDigits(born.get(Calendar.YEAR), 4).forEachIndexed { index, digit -> bornDigits[index + 4] = digit }
    val bornDigitsString = bornDigits.map { d -> d % 2 }.joinToString("", "", "")

    //replace X in input
    val inputLinesStrings = inputLines.map { l -> l.toUpperCase().replace("X", bornDigitsString) }

    //print result
    inputLinesStrings.forEach { l ->
        val inputBools = try {
            l.mapIndexed {
                index, c ->
                if (c == '0') false
                else if (c == '1') true
                else throw ParseException("Character $c was neither 0 nor 1", index)
            }.toBooleanArray()
        } catch (pe: ParseException) {
            System.err.println("Failed to parse input: ${pe.localizedMessage}")
            return@forEach
        }
        System.out.println("$l ${if (BinaryDivisibilityDFA(divisionBy).read(inputBools)) "" else "not"} accepted")
    }

}

private fun readInputFile(filename: String) : List<String>  {
    return File(filename).readLines()
}

private fun toDigits(num: Int, minDigits: Int = 0): ByteArray {
    var n = num
    if (n < 0) n *= -1
    val digits = ArrayList<Byte>()

    while (n > 0) {
        digits.add((n % 10).toByte())
        n /= 10
    }
    while (minDigits > digits.size) digits.add(0)

    return digits.reversed().toByteArray()
}

private fun printHelp() {
    System.out.println("Parameters:\n\t[-options] <word1> [<word2> ...]\n" +
            "or: \n\t[-options] -i <inputfile>\t(for reading words separated line by line from file)\n" +
            "or: \n\t-h\t(for this message)\n\n" +
            "options can contain the following:\n" +
            "\t(-b | --born) <birthday>\tEnter in your local format. " +
            "Will replace X in input by ddmmyyyy (each modulo 2). Defaults to 24.02.1998\n" +
            "\t(-d | --divby) <d>\tWill test divisibility by d. Defaults to 5.\n")
}