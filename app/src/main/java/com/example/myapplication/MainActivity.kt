package com.example.myapplication

import android.content.Context
import android.hardware.usb.UsbManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.xujiaao.android.firmata.board.connectBoardWithLifecycle
import com.xujiaao.android.firmata.board.driver.Led
import com.xujiaao.android.firmata.transport.toTransport

import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectBoardWithLifecycle("bt://HC-06".toTransport(this), lifecycle) {
            onConnecting { Log.v(this::class.java.name, "Connecting...") }

            onConnected { board ->
                Log.v(this::class.java.name,"Connected")

               val led = board.Led(13)
               led.blink(500) // Blink every half second
            }

            onDisconnected { error ->
                if (error != null) {
                    Log.v(this::class.java.name,"Disconnected: ${error.message}")
                }
            }
        }

    }

    @Throws(IOException::class)
    private fun setupFirmata() {
        // Find all available drivers from attached devices.
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            return
        }

        // Open a connection to the first available driver.
        val driver = availableDrivers[0]
        val connection = manager.openDevice(driver.device)
                ?: // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
                return

        // Read some data! Most have just one port (port 0).
        val port = driver.ports[0]
        try {
            port.open(connection)
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

            val buffer = ByteArray(16)
            val numBytesRead = port.read(buffer, 1000)
        } catch (e: IOException) {
            // Deal with error.
        } finally {
            port.close()
        }
    }
}
