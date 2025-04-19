package com.example.textresumeapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ResumeApi {
    @GET("resume")
    suspend fun getResume(@Query("name") name: String): Resume
}

data class Resume(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String,
    @SerializedName("twitter") val twitter: String,
    @SerializedName("address") val address: String,
    @SerializedName("skills") val skills: List<String>,
    @SerializedName("projects") val projects: List<Project>
)

data class Project(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                ResumeApp()
            }
        }
    }
}

@Composable
fun ResumeApp() {
    var resume by remember { mutableStateOf<Resume?>(null) }
    var fontSize by remember { mutableStateOf(16f) }
    var fontColor by remember { mutableStateOf(Color.White) }
    var bgColor by remember { mutableStateOf(Color(0xFFB2FFB2)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Ensure only one of the pickers is open at a time
    var activePicker by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://expressjs-api-resume-random.onrender.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ResumeApi::class.java)

        try {
            resume = withContext(Dispatchers.IO) {
                api.getResume("Vivek Sachan")
            }
            Log.d("ResumeApp", "Fetched resume: $resume")
        } catch (e: Exception) {
            errorMessage = "Failed to load resume: ${e.localizedMessage}"
            Log.e("ResumeApp", "Error fetching resume", e)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 140.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(16.dp))
            when {
                errorMessage != null -> Text(errorMessage!!, color = Color.Red, fontSize = fontSize.sp)
                resume == null -> Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color.White) }
                else -> ResumeCard(resume!!, fontSize, fontColor, bgColor)
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color(0xFF121212))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Font Size: ${fontSize.toInt()}sp", color = Color.LightGray)
            Slider(
                value = fontSize,
                onValueChange = { fontSize = it },
                valueRange = 12f..40f,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (activePicker == "font") {
                    ExpandableColorPicker(
                        title = "Choose Font Color",
                        isExpanded = true,
                        onToggle = { activePicker = null },
                        onColorSelected = {
                            fontColor = it
                            activePicker = null
                        }
                    )
                }

                if (activePicker == "background") {
                    ExpandableColorPicker(
                        title = "Choose Background Color",
                        isExpanded = true,
                        onToggle = { activePicker = null },
                        onColorSelected = {
                            bgColor = it
                            activePicker = null
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    Button(
                        onClick = {
                            activePicker = if (activePicker == "font") null else "font"
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f)
                    ) {
                        Icon(Icons.Filled.FormatColorText, contentDescription = "Font Color")
                        Spacer(Modifier.width(8.dp))
                        Text("Font Color")
                    }

                    Button(
                        onClick = {
                            activePicker = if (activePicker == "background") null else "background"
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f)
                    ) {
                        Icon(Icons.Filled.FormatColorFill, contentDescription = "Background Color")
                        Spacer(Modifier.width(8.dp))
                        Text("Bg Color")
                    }
                }
            }
        }
    }
}

@Composable
fun ResumeCard(resume: Resume, fontSize: Float, fontColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(12.dp))
            .shadow(4.dp, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text("PERSONAL DETAILS\n------------------------", style = sectionStyle(fontSize, fontColor))
            Text("Name: ${resume.name}", style = textStyle(fontSize, fontColor))
            Text("Phone: ${resume.phone}", style = textStyle(fontSize, fontColor))
            Text("Email: ${resume.email}", style = textStyle(fontSize, fontColor))
            Text("Twitter: ${resume.twitter}", style = textStyle(fontSize, fontColor))
            Text("Address: ${resume.address}", style = textStyle(fontSize, fontColor))

            Spacer(Modifier.height(8.dp))
            Text("SKILLS\n------------------------", style = sectionStyle(fontSize, fontColor))
            resume.skills.forEach { Text("• $it", style = textStyle(fontSize, fontColor)) }

            Spacer(Modifier.height(8.dp))
            Text("PROJECTS\n------------------------", style = sectionStyle(fontSize, fontColor))
            resume.projects.forEach {
                Text("• ${it.title} - ${it.description}", style = textStyle(fontSize, fontColor))
            }
        }
    }
}

fun sectionStyle(fontSize: Float, fontColor: Color) = TextStyle(
    fontSize = (fontSize + 2).sp,
    fontWeight = FontWeight.Bold,
    color = fontColor
)

fun textStyle(fontSize: Float, fontColor: Color) = TextStyle(
    fontSize = fontSize.sp,
    color = fontColor,
    fontFamily = FontFamily.SansSerif
)

@Composable
fun ExpandableColorPicker(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = Color.White
            )
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(4.dp))
            val colors = listOf(
                Color.Black, Color.White, Color.Gray, Color.LightGray,
                Color.Red, Color.Green, Color.Blue, Color.Cyan,
                Color.Magenta, Color.Yellow, Color(0xFF6A1B9A), Color(0xFFFF9800)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(48.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(colors) { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color)
                            .border(1.dp, Color.DarkGray)
                            .clickable { onColorSelected(color) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

