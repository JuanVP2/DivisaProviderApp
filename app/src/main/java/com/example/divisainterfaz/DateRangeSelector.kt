package com.example.divisainterfaz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeSelector(
    startDate: String,
    endDate: String,
    currency: String,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onLoadData: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Exchange Rate Viewer",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Currency selection
        OutlinedTextField(
            value = currency,
            onValueChange = onCurrencyChange,
            label = { Text("Currency") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Start date
        DateTimePickerField(
            label = "Start Date",
            dateTimeString = startDate,
            onDateTimeChange = onStartDateChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // End date
        DateTimePickerField(
            label = "End Date",
            dateTimeString = endDate,
            onDateTimeChange = onEndDateChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Load data button
        Button(
            onClick = onLoadData,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Load Data")
        }
    }
}