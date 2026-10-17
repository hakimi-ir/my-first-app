package com.example.mynotes

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

data class Note(val title: String, val body: String, val time: Long)

private fun toJson(list: List<Note>): String {
    val arr = JSONArray()
    list.forEach {
        arr.put(JSONObject().put("t", it.title).put("b", it.body).put("d", it.time))
    }
    return arr.toString()
}

private fun loadNotes(prefs: SharedPreferences): List<Note> {
    val json = prefs.getString("notes_json", null) ?: return emptyList()
    val arr = JSONArray(json)
    return List(arr.length()) { i ->
        val o = arr.getJSONObject(i)
        Note(o.getString("t"), o.getString("b"), o.getLong("d"))
    }
}

private fun formatTime(ms: Long): String {
    val locale = android.icu.util.ULocale("fa_IR@calendar=persian")
    val f = android.icu.text.SimpleDateFormat("yyyy/MM/dd HH:mm", locale)
    return f.format(Date(ms))
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("notes", Context.MODE_PRIVATE)
        val initial = loadNotes(prefs)
        setContent {
            val colors = if (isSystemInDarkTheme()) {
                darkColorScheme(primary = Color(0xFF4DB6AC))
            } else {
                lightColorScheme(primary = Color(0xFF00796B))
            }
            MaterialTheme(colorScheme = colors) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NotesScreen(initial) { list ->
                        prefs.edit().putString("notes_json", toJson(list)).apply()
                    }
                }
            }
        }
    }
}

@Composable
fun NotesScreen(initial: List<Note>, onChange: (List<Note>) -> Unit) {
    val notes = remember { mutableStateListOf<Note>().apply { addAll(initial) } }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf(-1) }

    fun clearForm() {
        title = ""
        body = ""
        editing = -1
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("یادداشت‌ها", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("عنوان") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("متن یادداشت") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                if (title.isNotBlank() || body.isNotBlank()) {
                    val note = Note(title.trim(), body.trim(), System.currentTimeMillis())
                    if (editing >= 0) notes[editing] = note else notes.add(0, note)
                    onChange(notes.toList())
                    clearForm()
                }
            }) { Text(if (editing >= 0) "ذخیره تغییرات" else "افزودن") }
            if (editing >= 0) {
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { clearForm() }) { Text("انصراف") }
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            itemsIndexed(notes) { index, note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            title = note.title
                            body = note.body
                            editing = index
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (note.title.isNotBlank()) {
                                Text(note.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            if (note.body.isNotBlank()) {
                                Text(note.body)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                formatTime(note.time),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        TextButton(onClick = {
                            notes.removeAt(index)
                            if (editing == index) {
                                clearForm()
                            } else if (editing > index) {
                                editing -= 1
                            }
                            onChange(notes.toList())
                        }) { Text("حذف") }
                    }
                }
            }
        }
    }
}
