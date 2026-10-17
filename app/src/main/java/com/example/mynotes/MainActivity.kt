package com.example.mynotes

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val SEP = "\u0001"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("notes", Context.MODE_PRIVATE)
        val saved = prefs.getString("items", "") ?: ""
        val initial = if (saved.isEmpty()) emptyList() else saved.split(SEP)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NotesScreen(initial) { list ->
                        prefs.edit().putString("items", list.joinToString(SEP)).apply()
                    }
                }
            }
        }
    }
}

@Composable
fun NotesScreen(initial: List<String>, onChange: (List<String>) -> Unit) {
    val notes = remember { mutableStateListOf<String>().apply { addAll(initial) } }
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("یادداشت‌ها", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("یادداشت جدید") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (text.isNotBlank()) {
                    notes.add(0, text.trim())
                    text = ""
                    onChange(notes.toList())
                }
            }) { Text("افزودن") }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            itemsIndexed(notes) { index, note ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(note, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            notes.removeAt(index)
                            onChange(notes.toList())
                        }) { Text("حذف") }
                    }
                }
            }
        }
    }
}
