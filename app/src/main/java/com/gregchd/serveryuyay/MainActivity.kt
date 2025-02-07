package com.gregchd.serveryuyay

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

@Serializable
data class ClientInformation(
    val client_id: String,
    val num_lvls: Int,
    val lvl_names: List<String>
)

@Serializable
data class ClientMessage(
    val type: String,
    val information: ClientInformation
)

class MainActivity : AppCompatActivity() {

    private lateinit var txtStatus: TextView
    private lateinit var btnStartServer: Button
    private lateinit var txtReceivedData: TextView
    private var serverSocket: ServerSocket? = null
    private val executor = Executors.newCachedThreadPool() // Pool de hilos para manejar clientes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtStatus = findViewById(R.id.txtStatusServer)
        btnStartServer = findViewById(R.id.btnStartServer)
        txtReceivedData = findViewById(R.id.txtReceivedData)

        btnStartServer.setOnClickListener { startServer() }
    }

    private fun startServer() {
        Thread {
            try {
                serverSocket = ServerSocket(9999) // Escucha en el puerto 9999
                runOnUiThread {
                    txtStatus.text = "Servidor Activo ✅\nEsperando clientes..."
                }

                while (true) { // Mantiene el servidor activo para siempre
                    val clientSocket = serverSocket!!.accept() // Acepta una conexión
                    executor.execute(ClientHandler(clientSocket)) // Ejecuta un hilo para cada cliente
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    txtStatus.text = "Error al iniciar servidor ❌"
                }
            }
        }.start()
    }

    // Clase para manejar cada cliente en un hilo separado
    inner class ClientHandler(private val clientSocket: Socket) : Runnable {
        override fun run() {
            try {
                val input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                val receivedJson = input.readLine()

                if (receivedJson != null) {
                    val clientMessage = Json.decodeFromString<ClientMessage>(receivedJson)
                    val displayText =
                        "📌 Cliente ID: ${clientMessage.information.client_id}\n" +
                                "🎮 Niveles: ${clientMessage.information.lvl_names.joinToString(", ")}"

                    runOnUiThread {
                        txtReceivedData.text = "${txtReceivedData.text}\n\n$displayText"
                    }
                }
                clientSocket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
