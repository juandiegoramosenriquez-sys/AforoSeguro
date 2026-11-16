# Aforo Seguro: guía de puesta en marcha

```
[2 sensores HC-SR04] → ESP32 ◄──── Bluetooth ────► App Android "Aforo Seguro"
                         │
                         └─ LCD 16x2 I2C · LED verde · LED rojo · buzzer
```

Todo funciona **sin internet y sin cuentas**: el celular habla directo con el ESP32 por el Bluetooth que el ESP32 trae integrado.

| Carpeta | Contenido |
|---|---|
| `esp32/AforoSeguro/` | Código del ESP32 (`AforoSeguro.ino`), comentado |
| `app/`, `gradle/` (raíz) | Proyecto de Android Studio (Kotlin + Jetpack Compose) |
| `docs/` | Esta guía |

---

## Paso 1: Programar el ESP32

1. Abre `esp32/AforoSeguro/AforoSeguro.ino` con el **Arduino IDE**.
2. Necesitas la placa **esp32 de Espressif** y la librería **LiquidCrystal I2C**. Ya están instaladas en esta PC.
3. **Herramientas → Placa**: `ESP32 Dev Module`. Elige el puerto COM y pulsa **Subir**.
4. Abre el **Monitor serie** a `115200` baudios. Debe decir `Bluetooth listo: AforoSeguro`.

> El ESP32 debe ser el modelo clásico (ESP32-WROOM / DevKit). Los modelos ESP32-S3 y ESP32-C3 no tienen Bluetooth clásico.

### ¿Usan un módulo HC-05 en vez del Bluetooth integrado?
Al inicio del `.ino` cambia `#define USAR_HC05 false` por `true` y conecta:

| HC-05 | ESP32 |
|---|---|
| VCC / GND | VIN (5V) / GND |
| TXD | GPIO 16 |
| RXD | GPIO 17 (con divisor de voltaje 1 kΩ / 2 kΩ) |

El HC-05 aparece en el celular como **HC-05** (clave de emparejamiento `1234` o `0000`). La app funciona igual con los dos.

### Conexiones (pines)

| Componente | Pin del componente | Pin del ESP32 |
|---|---|---|
| HC-SR04 **exterior** | VCC / GND | VIN (5V) / GND |
| | TRIG | GPIO 5 |
| | ECHO | GPIO 18 (con divisor de voltaje) |
| HC-SR04 **interior** | VCC / GND | VIN (5V) / GND |
| | TRIG | GPIO 19 |
| | ECHO | GPIO 23 (con divisor de voltaje) |
| LCD 16x2 I2C | VCC / GND | VIN (5V) / GND |
| | SDA / SCL | GPIO 21 / GPIO 22 |
| LED verde (+ resistencia 220 Ω) | Ánodo | GPIO 25 |
| LED rojo (+ resistencia 220 Ω) | Ánodo | GPIO 26 |
| Buzzer | + | GPIO 27 |
| Bluetooth | — | Integrado en el ESP32 (no lleva cables; ver abajo si usan HC-05) |

- **Divisor de voltaje en cada ECHO:** el HC-SR04 responde con 5 V y el ESP32 trabaja a 3.3 V. Conecta ECHO → resistencia de **1 kΩ** → pin del ESP32, y desde ese pin una resistencia de **2 kΩ** a GND.
- **Alimentación:** el ESP32 se alimenta por USB (5 V). VIN entrega esos 5 V a los sensores y al LCD. Todos los GND van juntos.
- **Ubicación:** el sensor *exterior* va del lado de afuera de la puerta y el *interior* del lado de adentro, separados unos 15–20 cm, apuntando al paso de la gente.
- Si el LCD no muestra texto, cambia `0x27` por `0x3F` en el código o ajusta el contraste con el potenciómetro de atrás.

## Paso 2: Emparejar el celular con el ESP32 (solo la primera vez)

1. Con el ESP32 encendido, en el celular ve a **Ajustes → Bluetooth**.
2. Busca dispositivos y toca **AforoSeguro**. Si pide confirmar un código, acéptalo.
3. Debe quedar en la lista de **dispositivos emparejados**.

## Paso 3: Instalar la app en el celular

**Para instalarla en otros celulares:** en Android Studio, **Build → Generate App Bundles or APKs → Build APK(s)**.
El APK queda en `app/build/outputs/apk/debug/app-debug.apk`. Pásalo al celular (WhatsApp, Drive o cable), ábrelo y permite *instalar apps desconocidas*.

**Desde Android Studio:**
1. **File → Open** y elige la carpeta `AforoSeguro` (la raíz del proyecto). Espera el *Gradle Sync*.
2. En el celular: **Ajustes → Acerca del teléfono** → toca 7 veces **Número de compilación**. Luego activa **Opciones de desarrollador → Depuración USB**.
3. Conecta el celular por USB, elígelo en la lista de dispositivos de arriba y pulsa **Run ▶**.

> El emulador de Android Studio **no tiene Bluetooth**. Ahí usa el **modo demostración** (botón *Probar modo demostración*), que simula el ESP32 con el mismo protocolo. Para la conexión real usa un celular.

## Paso 4: Usar la app

1. Abre la app y acepta el permiso de notificaciones.
2. En **Inicio**, pulsa **Conectar por Bluetooth**, acepta el permiso de *dispositivos cercanos* y elige **AforoSeguro**.
3. Cuando el indicador de arriba diga **En línea**, verás el aforo en tiempo real. En el LCD aparece `BT` a la derecha.

| Pestaña | Qué hace |
|---|---|
| **Inicio** | Personas dentro, % de ocupación, espacios libres, estado (Seguro / Alerta / Lleno), entradas, salidas y gráfico de la evolución |
| **Historial** | Cada entrada y salida con su hora, con filtros (desde que la app se conectó) |
| **Ajustes** | Conectar o desconectar, cambiar el aforo máximo y reiniciar el conteo; el ESP32 responde al instante |

**Modo demostración:** si el prototipo no está disponible (o falla en la presentación), pulsa *Probar modo demostración*. La app simula entradas y salidas, y responde a los cambios de aforo y al reinicio igual que el ESP32.

## Paso 5: Probar el sistema completo (y grabar el video)

| Prueba | Qué hacer | Resultado esperado |
|---|---|---|
| Conexión | Pulsar Conectar y elegir AforoSeguro | "Conectado" en la app y `BT` en el LCD |
| Entrada | Pasar la mano por el sensor exterior y luego el interior | +1 en el LCD y en la app |
| Salida | Pasar la mano por el interior y luego el exterior | −1 en el LCD y en la app |
| Historial | Abrir la pestaña Historial | Aparecen las entradas y salidas con hora |
| Cambiar aforo | En Ajustes, poner 3 y guardar | El LCD cambia a `x/3` y suena un pitido |
| Aforo lleno | Hacer 3 entradas | LCD "LLENO!", LED rojo, 3 pitidos, aviso rojo y notificación en la app |
| Reiniciar | En Ajustes, pulsar Reiniciar | El LCD y la app vuelven a 0 |

**Para el video** graba el celular y el prototipo **en la misma toma**: cambia el aforo o reinicia desde la app y muestra cómo responde el LCD, los LEDs y el buzzer.

## Cómo se comunican (protocolo Bluetooth)

Cada mensaje es una línea de texto:

| Sentido | Mensaje | Significado |
|---|---|---|
| ESP32 → App | `ESTADO;8;20;12;4` | 8 personas, máximo 20, 12 entradas, 4 salidas (se envía al cambiar y cada 2 s) |
| ESP32 → App | `EVENTO;ENTRADA;9` | Alguien entró; ahora hay 9 |
| ESP32 → App | `EVENTO;SALIDA;8` | Alguien salió; ahora hay 8 |
| App → ESP32 | `MAX;15` | Cambiar el aforo máximo a 15 (queda guardado aunque se apague) |
| App → ESP32 | `RESET` | Poner el conteo en 0 |

## Problemas comunes

| Problema | Solución |
|---|---|
| "AforoSeguro" no aparece al buscar | Revisa que el ESP32 esté encendido y que el monitor serie diga `Bluetooth listo` |
| La lista de la app está vacía | Primero empareja AforoSeguro en los ajustes de Bluetooth (paso 2) |
| "No se pudo conectar" | Acerca el celular, reinicia el ESP32 y vuelve a pulsar Conectar |
| Los botones de Ajustes están apagados | La app no está conectada: conéctate en Inicio o en Ajustes |
| Cuenta dos veces o no cuenta | Ajusta `DISTANCIA_DETECCION_CM` y `TIEMPO_MAX_CRUCE_MS` en el `.ino` |
