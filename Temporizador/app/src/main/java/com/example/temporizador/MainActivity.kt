package com.example.temporizador

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var topTimer: TextView
    private lateinit var bottomTimer: TextView
    private lateinit var tapToStartTop: TextView
    private lateinit var tapToStartBottom: TextView
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var resetButton: Button
    private lateinit var mainLayout: ConstraintLayout // Layout principal

    private var topTimerInstance: CountDownTimer? = null
    private var bottomTimerInstance: CountDownTimer? = null
    private var timeLeftInMillisTop: Long = 300000 // 5 minutos en milisegundos
    private var timeLeftInMillisBottom: Long = 300000 // 5 minutos para el temporizador inferior
    private var isTopTimerRunning = false
    private var isBottomTimerRunning = false

    // Variable para guardar el temporizador que estaba corriendo antes de la pausa
    private var lastRunningTimer: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Aplicar insets del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Vincular vistas
        mainLayout = findViewById(R.id.main) // Obtener referencia al layout principal
        topTimer = findViewById(R.id.top_timer)
        bottomTimer = findViewById(R.id.bottom_timer)
        tapToStartTop = findViewById(R.id.tap_to_start_top)
        tapToStartBottom = findViewById(R.id.tap_to_start_bottom)
        startButton = findViewById(R.id.button_1_top)
        pauseButton = findViewById(R.id.button_2_top)
        resetButton = findViewById(R.id.button_3_top)

        // Mostrar el tiempo inicial en ambos temporizadores
        updateTimersText()

        // Configurar botón de inicio
        startButton.setOnClickListener { startTopTimer() }

        // Configurar botón de pausa/play
        pauseButton.setOnClickListener { togglePausePlay() }

        // Configurar eventos de toque en los TextView "Tap to start"
        tapToStartTop.setOnClickListener {
            changeScreenColor(ContextCompat.getColor(this, R.color.lime_green)) // Cambiar a verde lima
            pauseTopAndStartBottomTimer()
        }

        tapToStartBottom.setOnClickListener {
            changeScreenColor(ContextCompat.getColor(this, R.color.orange)) // Cambiar a naranja usando el valor hexadecimal
            pauseBottomAndStartTopTimer()
        }

        // Configurar botón de reinicio
        resetButton.setOnClickListener { resetTimers() }
    }

    // Función para cambiar el color de fondo de la pantalla
    private fun changeScreenColor(color: Int) {
        mainLayout.setBackgroundColor(color) // Cambiar el color de fondo del layout
    }

    private fun startTopTimer() {
        if (isTopTimerRunning) return // Evitar múltiples inicios simultáneos

        topTimerInstance = object : CountDownTimer(timeLeftInMillisTop, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillisTop = millisUntilFinished
                updateTimersText()
            }

            override fun onFinish() {
                isTopTimerRunning = false
            }
        }.start()

        isTopTimerRunning = true
        lastRunningTimer = "top" // Guardar el estado del temporizador que está corriendo
        pauseButton.text = "Pause" // Cambiar el texto del botón a "Pause"
    }

    private fun startBottomTimer() {
        if (isBottomTimerRunning) return // Evitar múltiples inicios simultáneos

        bottomTimerInstance = object : CountDownTimer(timeLeftInMillisBottom, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillisBottom = millisUntilFinished
                updateTimersText()
            }

            override fun onFinish() {
                isBottomTimerRunning = false
            }
        }.start()

        isBottomTimerRunning = true
        lastRunningTimer = "bottom" // Guardar el estado del temporizador que está corriendo
        pauseButton.text = "Pause" // Cambiar el texto del botón a "Pause"
    }

    private fun pauseTopAndStartBottomTimer() {
        if (isTopTimerRunning) {
            topTimerInstance?.cancel()
            isTopTimerRunning = false
            pauseButton.text = "Play" // Cambiar texto a "Play"
            startBottomTimer() // Iniciar el temporizador inferior
        }
    }

    private fun pauseBottomAndStartTopTimer() {
        if (isBottomTimerRunning) {
            bottomTimerInstance?.cancel()
            isBottomTimerRunning = false
            pauseButton.text = "Play" // Cambiar texto a "Play"
            startTopTimer() // Iniciar el temporizador superior
        }
    }

    private fun resetTimers() {
        topTimerInstance?.cancel()
        bottomTimerInstance?.cancel()

        timeLeftInMillisTop = 300000 // Reiniciar a 5 minutos
        timeLeftInMillisBottom = 300000 // Reiniciar a 5 minutos

        updateTimersText()
        isTopTimerRunning = false
        isBottomTimerRunning = false
        lastRunningTimer = null // Reiniciar el estado de último temporizador
        pauseButton.text = "Pause" // Reiniciar el texto del botón a "Pause"

        // Restaurar color de fondo al color original
        mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white)) // Cambiar a blanco
    }

    private fun updateTimersText() {
        val minutesTop = (timeLeftInMillisTop / 1000) / 60
        val secondsTop = (timeLeftInMillisTop / 1000) % 60
        val timeFormattedTop = String.format("%02d:%02d", minutesTop, secondsTop)

        val minutesBottom = (timeLeftInMillisBottom / 1000) / 60
        val secondsBottom = (timeLeftInMillisBottom / 1000) % 60
        val timeFormattedBottom = String.format("%02d:%02d", minutesBottom, secondsBottom)

        // Actualizar ambos temporizadores
        topTimer.text = timeFormattedTop
        bottomTimer.text = timeFormattedBottom
    }

    // Toggle entre pausa y play para ambos temporizadores
    private fun togglePausePlay() {
        if (isTopTimerRunning) {
            pauseTopTimer()
        } else if (isBottomTimerRunning) {
            pauseBottomTimer()
        } else {
            // Reanudar el temporizador correcto según el último que estuvo corriendo
            when (lastRunningTimer) {
                "top" -> resumeTopTimer()
                "bottom" -> resumeBottomTimer()
            }
        }
    }

    // Pausar temporizador superior
    private fun pauseTopTimer() {
        topTimerInstance?.cancel()
        isTopTimerRunning = false
        pauseButton.text = "Play" // Cambiar el texto del botón a "Play"
    }

    // Pausar temporizador inferior
    private fun pauseBottomTimer() {
        bottomTimerInstance?.cancel()
        isBottomTimerRunning = false
        pauseButton.text = "Play" // Cambiar el texto del botón a "Play"
    }

    // Reanudar temporizador superior
    private fun resumeTopTimer() {
        topTimerInstance = object : CountDownTimer(timeLeftInMillisTop, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillisTop = millisUntilFinished
                updateTimersText()
            }

            override fun onFinish() {
                isTopTimerRunning = false
            }
        }.start()

        isTopTimerRunning = true
        pauseButton.text = "Pause" // Cambiar el texto del botón a "Pause"
    }

    // Reanudar temporizador inferior
    private fun resumeBottomTimer() {
        bottomTimerInstance = object : CountDownTimer(timeLeftInMillisBottom, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillisBottom = millisUntilFinished
                updateTimersText()
            }

            override fun onFinish() {
                isBottomTimerRunning = false
            }
        }.start()

        isBottomTimerRunning = true
        pauseButton.text = "Pause" // Cambiar el texto del botón a "Pause"
    }
}
