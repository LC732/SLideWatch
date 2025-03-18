package com.example.sliderwatch.presentation

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import java.io.IOException
import java.util.UUID

class MainActivity : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    var bluetoothSocket: BluetoothSocket? = null
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val requestBluetoothPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            connectToExistingDevice()
        } else {
            Toast.makeText(this, "Permissão de Bluetooth negada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BluetoothConnectScreen(
                onConnect = { checkPermissionsAndConnect() }
            )
        }
    }

    private fun checkPermissionsAndConnect() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            connectToExistingDevice()
        }
    }

    private fun connectToExistingDevice() {
        try {
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            val device = pairedDevices?.firstOrNull()

            if (device != null) {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()
                Toast.makeText(this, "Conectado ao ${device.name}!", Toast.LENGTH_SHORT).show()
                startControlActivity()
            } else {
                Toast.makeText(this, "Nenhum dispositivo emparelhado encontrado", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Erro de permissão de Bluetooth", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: IOException) {
            Toast.makeText(this, "Falha na conexão", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun startControlActivity() {
        val intent = Intent(this, ControlActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun BluetoothConnectScreen(onConnect: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onConnect) {
            Text("Conectar Bluetooth")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BluetoothConnectScreenPreview() {
    BluetoothConnectScreen(onConnect = {})
}

class ControlActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ControlScreen(
                onLeft = { sendCommand("LEFT") },
                onRight = { sendCommand("RIGHT") }
            )
        }
    }

    private fun sendCommand(command: String) {
        try {
            (applicationContext as? MainActivity)?.bluetoothSocket?.outputStream?.write(command.toByteArray())
        } catch (e: IOException) {
            Toast.makeText(this, "Erro ao enviar comando", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}

@Composable
fun ControlScreen(onLeft: () -> Unit, onRight: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onLeft) {
            Text("Esquerda")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRight) {
            Text("Direita")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ControlScreenPreview() {
    ControlScreen(onLeft = {}, onRight = {})
}