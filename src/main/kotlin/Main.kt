import java.math.BigInteger
import java.util.*

val VARIABLES = mutableMapOf<String, BigInteger>()

fun main() {
    while (true) {
        val input = readln()
        when {
            input.startsWith("/") -> {
                when (input) {
                    "/exit" -> {
                        println("Bye!")
                        break
                    }

                    "/help" -> println("The program calculates the sum of numbers")
                    else -> println("Unknown command")
                }
            }

            else -> {
                if (input.contains("=")) {
                    assignVariables(input)
                } else if (input.matches(" *[a-zA-Z]+ *".toRegex())) {
                    printVarValue(input)
                } else {
                    calculateExpression(input)
                }
            }
        }
    }
}

fun assignVariables(input: String) {
    try {
        val inputArray = input.split("=").map { it.trim() }
        when {
            inputArray.size != 2 -> println("Invalid assignment")
            inputArray[0].contains("\\d".toRegex()) -> println("Invalid identifier")
            inputArray[1].contains("[a-zA-Z]\\d.*]".toRegex()) -> println("Invalid identifier")
            inputArray[1].contains("[a-zA-Z]+".toRegex()) && !VARIABLES.containsKey(inputArray[1]) -> {
                if (!inputArray[0].matches("[a-zA-Z]+".toRegex())) {
                    println("Invalid identifier")
                } else {
                    println("Invalid assignment")
                }
            }

            inputArray[1].matches("-?\\d+".toRegex()) ->
                if (inputArray[0].matches("[a-zA-Z]+".toRegex())) {
                    VARIABLES[inputArray[0]] = BigInteger(inputArray[1])
                } else {
                    println("Invalid identifier")
                }

            inputArray[1].matches("[a-zA-Z]+".toRegex()) -> {
                val value = VARIABLES[inputArray[1]]
                if (value != null) {
                    VARIABLES[inputArray[0]] = value
                }
            }
        }
    } catch (_: Exception) {
    }
}

fun printVarValue(input: String) {
    val value = VARIABLES[input.trim()]
    if (value == null) {
        println("Unknown variable")
    } else {
        println(value)
    }
}

fun calculateExpression(input: String) {
    if (input.isNotBlank()) {
        var noSpacesInput = input.filter {
            it in '0'..'9' || it == '+' || it == '-' || it in 'a'..'z' || it in 'A'..'Z' || it == '*' || it == '/' ||
                it == '(' || it == ')'
        }
        if (hasErrors(noSpacesInput)) {
            println("Invalid expression")
        } else {
            if (noSpacesInput.matches("-?\\d+".toRegex())) {
                println(noSpacesInput)
            } else {
                noSpacesInput = optimizeSigns(noSpacesInput)
                val postfix = getPostfix(noSpacesInput)
                val result = getResult(postfix)
                println(result)
            }
        }
    }
}

fun hasErrors(noSpacesInput: String): Boolean {
    if (noSpacesInput.isBlank()) {
        return true
    }
    if (
        noSpacesInput.matches("\\d+\\+".toRegex()) ||
        noSpacesInput.matches("\\d+-".toRegex()) ||
        noSpacesInput.matches("\\d+ (\\d+)*".toRegex()) ||
        noSpacesInput.matches("(\\+)+".toRegex()) ||
        noSpacesInput.contains("\\*{2,}".toRegex()) ||
        noSpacesInput.contains("/{2,}".toRegex())
    ) {
        return true
    }
    var sum = 0
    for (ch in noSpacesInput) {
        if (ch == '(') {
            sum += 1
        } else if (ch == ')') {
            sum -= 1
        }
    }
    if (sum != 0) {
        return true
    }
    return false
}

fun optimizeSigns(input: String): String {
    var result = input.replace("\\+{2,}".toRegex(), "+")
    while (result.contains("-{2,}".toRegex())) {
        result = result.replaceFirst("--", "+")
    }
    result = result.replace("+-", "-")
    result = result.replace("-+", "-")
    result = result.replace("\\+{2,}".toRegex(), "+")
    return result
}

fun getPostfix(input: String): String {
    val stack = Stack<Char>()
    var result = ""
    var currentString = ""
    for (ch in input) {
        if (ch in '0'..'9' || ch in 'a'..'z' || ch in 'A'..'Z') { // 1
            currentString += ch
        } else {
            if (currentString.isNotBlank()) {
                result += "$currentString "
                currentString = ""
            }
            when {
                stack.empty() || stack.last() == '(' -> stack.push(ch) // 2
                ch.hasHigherPrecedence(stack.last()) -> stack.push(ch) // 3
                !ch.hasHigherPrecedence(stack.last()) && ch != '(' && ch != ')' -> { // 4
                    while (true) {
                        if (stack.empty()) {
                            break
                        } else {
                            if (!ch.hasHigherPrecedence(stack.last()) && stack.last() != '(') {
                                result += "${stack.pop()} "
                            } else {
                                break
                            }
                        }
                    }
                    stack.push(ch)
                }

                ch == '(' -> stack.push(ch) // 5
                ch == ')' -> { // 6
                    while (true) {
                        val fromStack = stack.pop()
                        if (fromStack == '(') {
                            break
                        } else {
                            result += "$fromStack "
                        }
                    }
                }
            }
        }
    }
    if (currentString.isNotBlank()) {
        result += "$currentString "
    }
    while (stack.isNotEmpty()) {
        val fromStack = stack.pop()
        result += "$fromStack "
    }
    return result.substring(0, result.length - 1)
}

private fun Char.hasHigherPrecedence(last: Char) = (this == '*' || this == '/') && (last == '+' || last == '-')

fun getResult(postfix: String): BigInteger {
    val stack = Stack<String>()
    var currentString = ""
    for (ch in postfix) {
        if (ch in '0'..'9' || ch in 'a'..'z' || ch in 'A'..'Z') {
            currentString += ch
        } else {
            if (currentString.isNotBlank()) {
                stack.push(currentString)
                currentString = ""
            }
            if (ch != ' ') {
                val second = getVarOrNumber(stack.pop())
                val first = getVarOrNumber(stack.pop())
                when (ch) {
                    '+' -> stack.push((first + second).toString())
                    '-' -> stack.push((first - second).toString())
                    '*' -> stack.push((first * second).toString())
                    '/' -> stack.push((first / second).toString())
                }
            }
        }
    }
    return BigInteger(stack.last())
}

fun getVarOrNumber(string: String) =
    if (string.matches("-?\\d+".toRegex())) {
        BigInteger(string)
    } else {
        val currentVar =
            if (string.startsWith("-")) {
                VARIABLES[string.substring(1)]
            } else {
                VARIABLES[string]
            }
        if (currentVar != null) {
            if (string.startsWith("-")) {
                -currentVar
            } else {
                currentVar
            }
        } else {
            BigInteger("0")
        }
    }