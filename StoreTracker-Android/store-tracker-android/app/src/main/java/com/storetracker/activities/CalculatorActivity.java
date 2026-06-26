package com.storetracker.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.storetracker.R;

/**
 * A built-in basic calculator — accessible from the home screen at any time.
 * Supports +, −, ×, ÷ with a standard two-operand model.
 * No library required — pure Java arithmetic.
 */
public class CalculatorActivity extends AppCompatActivity {

    private TextView tvDisplay;

    private StringBuilder currentInput  = new StringBuilder();
    private double         firstOperand  = 0;
    private String         pendingOp     = null;
    private boolean        freshInput    = false; // true after an operator is pressed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        tvDisplay = findViewById(R.id.tvCalcDisplay);

        // ── Digit & decimal buttons ────────────────────────────────────────
        int[] digitIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDot
        };
        for (int id : digitIds) {
            Button btn = findViewById(id);
            btn.setOnClickListener(v -> appendDigit(((Button) v).getText().toString()));
        }

        // ── Operator buttons ───────────────────────────────────────────────
        findViewById(R.id.btnPlus).setOnClickListener(v  -> handleOperator("+"));
        findViewById(R.id.btnMinus).setOnClickListener(v -> handleOperator("−"));
        findViewById(R.id.btnMul).setOnClickListener(v   -> handleOperator("×"));
        findViewById(R.id.btnDiv).setOnClickListener(v   -> handleOperator("÷"));

        // ── Equals ─────────────────────────────────────────────────────────
        findViewById(R.id.btnEquals).setOnClickListener(v -> calculate());

        // ── Clear / Backspace ──────────────────────────────────────────────
        findViewById(R.id.btnClear).setOnClickListener(v -> clear());
        findViewById(R.id.btnBackspace).setOnClickListener(v -> backspace());

        // ── Percent ────────────────────────────────────────────────────────
        findViewById(R.id.btnPercent).setOnClickListener(v -> percent());

        updateDisplay();
    }

    private void appendDigit(String digit) {
        if (freshInput) {
            currentInput.setLength(0);
            freshInput = false;
        }
        // Prevent multiple decimal points
        if (digit.equals(".") && currentInput.toString().contains(".")) return;
        // Prevent leading zeros (except "0.")
        if (digit.equals("0") && currentInput.toString().equals("0")) return;
        if (!digit.equals(".") && currentInput.toString().equals("0")) currentInput.setLength(0);

        currentInput.append(digit);
        updateDisplay();
    }

    private void handleOperator(String op) {
        if (currentInput.length() == 0 && pendingOp == null) return;

        if (pendingOp != null && !freshInput) {
            // Chain operations: evaluate the pending one first
            calculate();
        }

        if (currentInput.length() > 0) {
            firstOperand = parseCurrentInput();
        }

        pendingOp  = op;
        freshInput = true;
        tvDisplay.setText(firstOperand + " " + op);
    }

    private void calculate() {
        if (pendingOp == null || currentInput.length() == 0) return;

        double secondOperand = parseCurrentInput();
        double result;

        switch (pendingOp) {
            case "+": result = firstOperand + secondOperand; break;
            case "−": result = firstOperand - secondOperand; break;
            case "×": result = firstOperand * secondOperand; break;
            case "÷":
                if (secondOperand == 0) {
                    tvDisplay.setText("Error");
                    reset();
                    return;
                }
                result = firstOperand / secondOperand;
                break;
            default: return;
        }

        // Show integer result without trailing ".0" when possible
        if (result == Math.floor(result) && !Double.isInfinite(result)) {
            currentInput = new StringBuilder(String.valueOf((long) result));
        } else {
            currentInput = new StringBuilder(String.valueOf(result));
        }

        pendingOp    = null;
        firstOperand = result;
        freshInput   = true;
        updateDisplay();
    }

    private void clear() {
        reset();
        updateDisplay();
    }

    private void reset() {
        currentInput  = new StringBuilder();
        firstOperand  = 0;
        pendingOp     = null;
        freshInput    = false;
    }

    private void backspace() {
        if (currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
            updateDisplay();
        }
    }

    private void percent() {
        if (currentInput.length() > 0) {
            double val = parseCurrentInput() / 100.0;
            currentInput = new StringBuilder(String.valueOf(val));
            updateDisplay();
        }
    }

    private double parseCurrentInput() {
        try {
            return Double.parseDouble(currentInput.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateDisplay() {
        tvDisplay.setText(currentInput.length() == 0 ? "0" : currentInput.toString());
    }
}
