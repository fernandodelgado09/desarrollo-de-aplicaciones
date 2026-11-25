package com.example.myapplication

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val tag = "MainActivity"
    private var keepSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplash }

        super.onCreate(savedInstanceState)
        showToast("")
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ViewContainer()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        showToast("")
        Log.i(tag, "onStart")
        keepSplash = false
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
        Log.i(tag, message)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    locale = "es-rMX"
)
@Composable
fun ViewContainer() {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_screen)) },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { /* Acción navegación */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_btn_desc),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Acción de me gusta */ }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Marcar como favorito",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        val intent = Intent(context, SecondActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = Color.White
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Más opciones",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Calendario") },
                                onClick = {
                                    menuExpanded = false
                                    try {
                                        val intent = Intent(Intent.ACTION_MAIN).apply {
                                            addCategory(Intent.CATEGORY_APP_CALENDAR)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No se encontró la app de Calendario", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Alarma") },
                                onClick = {
                                    menuExpanded = false
                                    var launched = false

                                    try {
                                        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                                            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                        launched = true
                                    } catch (_: Exception) {}

                                    if (!launched) {
                                        try {
                                            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                            launched = true
                                        } catch (_: Exception) {}
                                    }

                                    if (!launched) {
                                        val clockPackages = listOf(
                                            "com.google.android.deskclock",
                                            "com.android.deskclock",
                                            "com.sec.android.app.clockpackage",
                                            "com.coloros.alarmclock",
                                            "com.asus.clock"
                                        )
                                        val pm = context.packageManager
                                        for (pkg in clockPackages) {
                                            val launchIntent = pm.getLaunchIntentForPackage(pkg)
                                            if (launchIntent != null) {
                                                context.startActivity(launchIntent)
                                                launched = true
                                                break
                                            }
                                        }
                                    }

                                    if (!launched) {
                                        Toast.makeText(context, "No se encontró la app de Reloj/Alarma", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Descargas") },
                                onClick = {
                                    menuExpanded = false
                                    try {
                                        val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No se encontró la carpeta de Descargas", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scope.launch {
                    snackbarHostState.showSnackbar("intente de nuevo porfavor")
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar"
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ContentView()
        }
    }
}

@Composable
fun ContentView() {
    var counter by rememberSaveable { mutableIntStateOf(value = 0) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.android),
                    contentDescription = "Ilustración de Android",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                )
                Row(modifier = Modifier.padding(top = 2.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.rounded_5g_24),
                        contentDescription = "ilustracion android",
                        modifier = Modifier
                            .clickable { counter++ }
                            .padding(start = 12.dp)
                    )
                    Text(
                        text = counter.toString(),
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = stringResource(id = R.string.nombre_materia),
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(id = R.string.nombre_estudiante),
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(id = R.string.apellido),
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(all = 8.dp)
            ) {
                item {
                    Image(
                        painter = painterResource(id = R.drawable.r),
                        contentDescription = "imagen tics 1",
                        modifier = Modifier
                            .height(100.dp)
                            .padding(all = 4.dp)
                    )
                }
                item {
                    Image(
                        painter = painterResource(id = R.drawable.imagen3),
                        contentDescription = "imagen tics 2",
                        modifier = Modifier
                            .height(100.dp)
                            .padding(all = 4.dp)
                    )
                }
                item {
                    Image(
                        painter = painterResource(id = R.drawable.imagen4),
                        contentDescription = "imagen tics 3",
                        modifier = Modifier
                            .height(100.dp)
                            .padding(all = 4.dp)
                    )
                }
                item {
                    Image(
                        painter = painterResource(id = R.drawable.j),
                        contentDescription = "imagen tics 4",
                        modifier = Modifier
                            .height(100.dp)
                            .padding(all = 4.dp)
                    )
                }
            }
        }
    }
}
