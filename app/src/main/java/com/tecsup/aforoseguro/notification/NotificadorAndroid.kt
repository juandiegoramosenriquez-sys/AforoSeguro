package com.tecsup.aforoseguro.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tecsup.aforoseguro.MainActivity
import com.tecsup.aforoseguro.R

class NotificadorAndroid(private val context: Context) : Notificador {

    init {
        val canal = NotificationChannel(
            CANAL,
            context.getString(R.string.canal_alertas),
            NotificationManager.IMPORTANCE_HIGH
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    override fun avisarAforoLleno(personas: Int, maximo: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("¡Aforo completo!")
            .setContentText("Hay $personas de $maximo personas. No se debe permitir más ingresos.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(ID_LLENO, notificacion)
    }

    companion object {
        private const val CANAL = "alertas_aforo"
        private const val ID_LLENO = 1
    }
}
