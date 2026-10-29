package com.example.wordcounter;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText edtInputText;
    private Spinner spnMetric;
    private TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtInputText = findViewById(R.id.edtInputText);
        spnMetric = findViewById(R.id.spnMetric);
        txtResult = findViewById(R.id.txtResult);
        Button btnCount = findViewById(R.id.btnCount);

        setUpMetricSpinner();
        btnCount.setOnClickListener(view -> onCountButtonClick());
    }

    private void setUpMetricSpinner() {
        ArrayAdapter<CharSequence> metricAdapter = ArrayAdapter.createFromResource(
                this, R.array.metric_options, android.R.layout.simple_spinner_item);
        metricAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMetric.setAdapter(metricAdapter);
    }

    private void onCountButtonClick() {
        String inputText = edtInputText.getText().toString();

        if (TextCounter.isBlank(inputText)) {
            Toast.makeText(this, R.string.toast_empty_text, Toast.LENGTH_SHORT).show();
            txtResult.setText("");
            return;
        }

        TextMetric selectedMetric = TextMetric.fromPosition(spnMetric.getSelectedItemPosition());
        int resultCount = TextCounter.count(inputText, selectedMetric);
        String metricName = spnMetric.getSelectedItem().toString();

        txtResult.setText(getString(R.string.result_format, metricName, resultCount));
    }
}
