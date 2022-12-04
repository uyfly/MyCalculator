package eu.tutorials.mycalculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var expressionTextView: TextView
    private var lastNumeric = false
    private var lastOperator = false
    private var hasDecimalPoint = false

    private var pattern = "#,##0.########"
    private var decimalPlace = pattern.indexOf('.')

    private var exp1 = BigDecimal(0)
    private var exp2 = BigDecimal(0)
    private var op = "+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    fun onDigit(view: View) {
        if (expressionTextView.text.toString() == "NaN") {
            expressionTextView.text = "0"
        }

        lastNumeric = true
        expressionTextView.append((view as Button).text)
        val expression = expressionTextView.text.split(' ')
        val index = expressionTextView.text.lastIndexOf(' ')

        if (hasDecimalPoint && pattern.count { it == '0' } <= 8) {
            decimalPlace += 1
            val sb = StringBuilder(pattern).also {
                it.setCharAt(decimalPlace, '0')
            }
            pattern = sb.toString()
        }

        if (lastOperator) {
            lastOperator = false
            hasDecimalPoint = false
        } else {
            val size = expression.size
            if (size == 1) {
                val firstNumber = numberWithoutComma(expression[0])
                expressionTextView.text = getDecimalFormat(firstNumber)
            } else {
                val expressionBeforeLastNumber = expressionTextView.text.substring(0, index + 1)
                val lastNumber = numberWithoutComma(expression[size - 1])
                (expressionBeforeLastNumber + getDecimalFormat(lastNumber)).also {
                    expressionTextView.text = it
                }
            }
        }
    }

    fun onOperator(view: View) {
        if (expressionTextView.text.toString() == "NaN") {
            expressionTextView.text = "0"
            return
        }

        if (!lastOperator) {
            expressionTextView.append(" " + (view as Button).text + " ")
            lastNumeric = false
            lastOperator = true
            hasDecimalPoint = false
            resetDecimalFormat()
        } else {
            expressionTextView.text.dropLast(3).toString().also { expressionTextView.text = it }
            expressionTextView.append(" " + (view as Button).text + " ")
        }
    }

    fun onDecimalPoint(view: View) {
        if (expressionTextView.text.toString() == "NaN") {
            expressionTextView.text = "0"
            return
        }

        if (lastNumeric && !hasDecimalPoint) {
            expressionTextView.append((view as Button).text)
            hasDecimalPoint = true
        }
    }

    fun onCancel(view: View) {
        if (expressionTextView.text.toString() == "NaN") {
            expressionTextView.text = "0"
            return
        }

        if (lastOperator) {
            expressionTextView.text.dropLast(3).toString().also { expressionTextView.text = it }
            lastOperator = false
            return
        }

        if (hasDecimalPoint && pattern.count { it == '0' } <= 9) {
            val sb = StringBuilder(pattern).also {
                it.setCharAt(decimalPlace, '#')
            }
            decimalPlace -= 1
            pattern = sb.toString()

            if (decimalPlace == pattern.indexOf('.'))
                hasDecimalPoint = false
        }

        expressionTextView.text =
            when (expressionTextView.text.length) {
                1 -> "0"
                2 -> if (expressionTextView.text.first() == '-') {
                    "0"
                } else {
                    dropLastExpressionText()
                }
                else -> dropLastExpressionText()
            }
    }

    fun onEqual(view: View) {
        if (expressionTextView.text.toString() == "NaN") {
            expressionTextView.text = "0"
            return
        }

        val expression = expressionTextView.text.split(' ')
        exp1 = numberWithoutComma(expression[0]).toBigDecimal()

        when (expression.size) {
            1 -> {
                expressionTextView.text = calculateExpression()
            }
            3 -> {
                exp2 = numberWithoutComma(
                    if (lastOperator)
                        expression[0]
                    else
                        expression[2]
                ).toBigDecimal()
                op = expression[1]
                expressionTextView.text = calculateExpression()
            }
        }

        resetDecimalFormat()

        lastOperator = false
    }

    fun onSignSwitch(view: View) {
        Toast.makeText(this, "지원하지 않는 기능입니다.", Toast.LENGTH_SHORT).show()
    }

    fun onPercentage(view: View) {
        Toast.makeText(this, "지원하지 않는 기능입니다.", Toast.LENGTH_SHORT).show()
    }

    fun onClear(view: View) {
        expressionTextView.text = "0"
        lastNumeric = false
        lastOperator = false
        hasDecimalPoint = false
        op = "+"
        exp2 = BigDecimal(0)
        resetDecimalFormat()
    }

    private fun init() {
        lastNumeric = true
        expressionTextView = findViewById(R.id.expressionTextView)
    }

    private fun dropLastExpressionText(): String {
        val expression = expressionTextView.text.split(' ')
        val index = expressionTextView.text.lastIndexOf(' ')
        val size = expression.size
        if (size == 1) {
            val firstNumber = numberWithoutComma(expression[0].dropLast(1))
            return getDecimalFormat(firstNumber)
        } else {
            val expressionBeforeLastNumber = expressionTextView.text.substring(0, index + 1)
            val lastNumber = numberWithoutComma(expression[size - 1].dropLast(1))
            (expressionBeforeLastNumber + getDecimalFormat(lastNumber)).also {
                return it
            }
        }
    }

    private fun calculateExpression(): String {
        return when (op) {
            "+" -> getDecimalFormat((exp1.add(exp2)).toString())
            "-" -> getDecimalFormat((exp1.subtract(exp2)).toString())
            "*" -> getDecimalFormat((exp1.multiply(exp2)).toString())
            "/" -> try {
                getDecimalFormat(
                    (exp1.divide(
                        exp2,
                        8,
                        RoundingMode.HALF_EVEN
                    )).toString()
                )
            } catch (e: ArithmeticException) {
                op = "+"
                exp2 = BigDecimal(0)
                "NaN"
            }
            else -> ""
        }
    }

    private fun resetDecimalFormat() {
        pattern = "#,##0.########"
        decimalPlace = pattern.indexOf('.')
    }

    private fun numberWithoutComma(number: String): String {
        return number.replace(",", "")
    }

    private fun getDecimalFormat(number: String): String {
        return if (number != "") {
            val df = DecimalFormat(pattern)
            df.format(number.toBigDecimal())
        } else {
            lastOperator = true
            ""
        }
    }

}