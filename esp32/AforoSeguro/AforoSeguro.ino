// =====================================================================
//  AFORO SEGURO - Sistema de conteo de aforo con ESP32 + Bluetooth
//  Equipo 4 - Investigación e Innovación Tecnológica - Tecsup 2026
//
//  Funcionamiento:
//   - Dos sensores ultrasónicos HC-SR04 en la puerta (uno afuera y otro
//     adentro). El orden en que se activan indica si la persona entra o sale.
//   - El conteo se muestra en el LCD 16x2 I2C y se indica con LEDs y buzzer.
//   - Por Bluetooth (el que trae el ESP32) se conecta la app móvil
//     "Aforo Seguro": el ESP32 le envía el aforo y la app le envía órdenes
//     (cambiar el aforo máximo o reiniciar el conteo).
//
//  Mensajes por Bluetooth (una línea de texto por mensaje):
//   ESP32 -> App:  ESTADO;personas;maximo;entradas;salidas
//                  EVENTO;ENTRADA;personas   o   EVENTO;SALIDA;personas
//   App -> ESP32:  MAX;numero     (cambia el aforo máximo)
//                  RESET          (pone el conteo en 0)
// =====================================================================

// ------------------- ¿QUÉ BLUETOOTH SE USA? -------------------------
// false = Bluetooth integrado del ESP32 (no lleva cables).
// true  = módulo HC-05 conectado así: TX del HC-05 -> GPIO 16,
//         RX del HC-05 -> GPIO 17 (con divisor de voltaje), VCC 5V, GND.
#define USAR_HC05 false

#include <Preferences.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

#if USAR_HC05
HardwareSerial& bluetooth = Serial2;                 // UART2 del ESP32 hacia el HC-05
bool appConectada() { return true; }                 // El HC-05 no avisa si hay un celular conectado
void iniciarBluetooth(const char*) { Serial2.begin(9600, SERIAL_8N1, 16, 17); }  // 9600 = velocidad de fábrica
#else
#include <BluetoothSerial.h>
BluetoothSerial bluetooth;                           // Bluetooth clásico integrado
bool appConectada() { return bluetooth.hasClient(); }
void iniciarBluetooth(const char* nombre) { bluetooth.begin(nombre); }
#endif

// ---------------------------- PINES ---------------------------------
const int TRIG_EXTERIOR = 5;    // HC-SR04 del lado de afuera
const int ECHO_EXTERIOR = 18;
const int TRIG_INTERIOR = 19;   // HC-SR04 del lado de adentro
const int ECHO_INTERIOR = 23;
const int LED_VERDE     = 25;   // Encendido mientras hay espacio
const int LED_ROJO      = 26;   // Encendido cuando el local está lleno
const int BUZZER        = 27;   // Alarma sonora
// El LCD I2C usa los pines por defecto del ESP32: SDA = 21, SCL = 22

// --------------------------- AJUSTES --------------------------------
const char* NOMBRE_BLUETOOTH            = "AforoSeguro";  // Nombre que verá el celular
const int DISTANCIA_DETECCION_CM        = 50;    // Menos de esto = hay alguien
const unsigned long TIEMPO_MAX_CRUCE_MS = 1500;  // Tiempo máximo entre un sensor y el otro
const int PORCENTAJE_ALERTA             = 70;    // Desde este % se muestra "ALERTA"
const unsigned long INTERVALO_ESTADO_MS = 2000;  // Cada cuánto se reenvía el estado a la app
const int MAXIMO_POR_DEFECTO            = 20;


Preferences memoria;                   // Guarda el aforo máximo aunque se apague el ESP32
LiquidCrystal_I2C lcd(0x27, 16, 2);    // Dirección I2C habitual: 0x27 (algunos usan 0x3F)

// ---------------------------- CONTEO --------------------------------
int personas = 0;
int maximo   = MAXIMO_POR_DEFECTO;
int entradas = 0;
int salidas  = 0;

bool pantallaPendiente = true;     // Hay que redibujar el LCD
bool estadoPendiente   = true;     // Hay que enviar el estado a la app
bool habiaCliente      = false;    // Para saber cuándo se conecta o desconecta la app
unsigned long ultimoEnvio = 0;
String lineaRecibida = "";         // Va armando el mensaje que llega desde la app

// --------------------- MÁQUINA DE ESTADOS ---------------------------
// Reconoce el sentido del paso según qué sensor se activa primero.
enum Paso { ESPERANDO, VIO_EXTERIOR, VIO_INTERIOR, ESPERANDO_LIBRE };
Paso paso = ESPERANDO;
unsigned long inicioPaso = 0;

// ------------------------ BUZZER SIN BLOQUEO ------------------------
int pitidosRestantes = 0;
bool buzzerSonando = false;
unsigned long cambioBuzzer = 0;

// =====================================================================
//                              SENSORES
// =====================================================================

// Mide la distancia en cm con un HC-SR04. Devuelve 999 si no hay eco.
long medirDistanciaCm(int trig, int echo) {
  digitalWrite(trig, LOW);
  delayMicroseconds(2);
  digitalWrite(trig, HIGH);         // Pulso de 10 µs para disparar el sensor
  delayMicroseconds(10);
  digitalWrite(trig, LOW);
  long duracion = pulseIn(echo, HIGH, 25000);  // Espera el eco (máx. 25 ms ≈ 4 m)
  if (duracion == 0) return 999;
  return duracion / 58;              // Conversión de µs a cm
}

// Lee ambos sensores y decide si alguien entró o salió.
void procesarSensores() {
  bool exterior = medirDistanciaCm(TRIG_EXTERIOR, ECHO_EXTERIOR) < DISTANCIA_DETECCION_CM;
  delay(15);  // Pausa para que el eco de un sensor no confunda al otro
  bool interior = medirDistanciaCm(TRIG_INTERIOR, ECHO_INTERIOR) < DISTANCIA_DETECCION_CM;

  switch (paso) {
    case ESPERANDO:
      // Se activa primero el de afuera: posible entrada
      if (exterior && !interior) { paso = VIO_EXTERIOR; inicioPaso = millis(); }
      // Se activa primero el de adentro: posible salida
      else if (interior && !exterior) { paso = VIO_INTERIOR; inicioPaso = millis(); }
      break;

    case VIO_EXTERIOR:
      if (interior) { registrarEntrada(); paso = ESPERANDO_LIBRE; }
      else if (millis() - inicioPaso > TIEMPO_MAX_CRUCE_MS) paso = ESPERANDO_LIBRE;  // No terminó de pasar
      break;

    case VIO_INTERIOR:
      if (exterior) { registrarSalida(); paso = ESPERANDO_LIBRE; }
      else if (millis() - inicioPaso > TIEMPO_MAX_CRUCE_MS) paso = ESPERANDO_LIBRE;
      break;

    case ESPERANDO_LIBRE:
      // Espera a que la puerta quede libre para no contar dos veces a la misma persona
      if (!exterior && !interior) paso = ESPERANDO;
      break;
  }
}

// Suma una persona, avisa a la app y activa la alarma si se llegó al máximo.
void registrarEntrada() {
  personas++;
  entradas++;
  enviarEvento("ENTRADA");
  estadoPendiente = true;
  pantallaPendiente = true;

  if (personas == maximo) pitar(3);       // Se acaba de llenar: 3 pitidos
  else if (personas > maximo) pitar(1);   // Sigue entrando gente estando lleno
}

// Resta una persona (nunca baja de 0) y avisa a la app.
void registrarSalida() {
  if (personas > 0) personas--;
  salidas++;
  enviarEvento("SALIDA");
  estadoPendiente = true;
  pantallaPendiente = true;
}

// =====================================================================
//                     INDICADORES: LCD, LEDs Y BUZZER
// =====================================================================

// Programa una cantidad de pitidos; se reproducen en atenderBuzzer().
void pitar(int veces) {
  pitidosRestantes = veces;
  buzzerSonando = false;
  cambioBuzzer = 0;
}

// Hace sonar el buzzer sin usar delay(), así no se pierden lecturas.
void atenderBuzzer() {
  if (pitidosRestantes == 0 && !buzzerSonando) return;
  if (millis() - cambioBuzzer < 150) return;  // Cada pitido y cada pausa duran 150 ms
  cambioBuzzer = millis();
  if (buzzerSonando) {
    digitalWrite(BUZZER, LOW);
    buzzerSonando = false;
  } else if (pitidosRestantes > 0) {
    digitalWrite(BUZZER, HIGH);
    buzzerSonando = true;
    pitidosRestantes--;
  }
}

// Dibuja el aforo en el LCD y enciende el LED que corresponda.
void actualizarIndicadores() {
  int porcentaje = maximo > 0 ? personas * 100 / maximo : 0;
  bool lleno = personas >= maximo;

  digitalWrite(LED_VERDE, lleno ? LOW : HIGH);
  digitalWrite(LED_ROJO,  lleno ? HIGH : LOW);

  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Aforo: ");
  lcd.print(personas);
  lcd.print("/");
  lcd.print(maximo);

  lcd.setCursor(0, 1);
  if (lleno) lcd.print("LLENO!  ");
  else if (porcentaje >= PORCENTAJE_ALERTA) lcd.print("ALERTA  ");
  else lcd.print("SEGURO  ");
  lcd.print(porcentaje);
  lcd.print("%");
  lcd.setCursor(14, 1);
  lcd.print(appConectada() ? "BT" : "  ");  // "BT" = la app está conectada
}

// =====================================================================
//                         BLUETOOTH CON LA APP
// =====================================================================

// Envía el estado completo: ESTADO;personas;maximo;entradas;salidas
void enviarEstado() {
  if (!appConectada()) return;
  bluetooth.printf("ESTADO;%d;%d;%d;%d\n", personas, maximo, entradas, salidas);
}

// Avisa de una entrada o salida para el historial de la app.
void enviarEvento(const char* tipo) {
  if (!appConectada()) return;
  bluetooth.printf("EVENTO;%s;%d\n", tipo, personas);
}

// Ejecuta una orden recibida desde la app.
void atenderOrden(String orden) {
  orden.trim();
  Serial.println("Orden recibida: " + orden);

  if (orden.startsWith("MAX;")) {
    int nuevo = orden.substring(4).toInt();
    if (nuevo > 0 && nuevo < 1000) {
      maximo = nuevo;
      memoria.putInt("maximo", maximo);  // Queda guardado para el próximo encendido
      pitar(1);
    }
  } else if (orden == "RESET") {
    personas = 0;
    entradas = 0;
    salidas = 0;
    pitar(1);
  }
  estadoPendiente = true;
  pantallaPendiente = true;
}

// Lee lo que llega por Bluetooth y envía el estado cuando hace falta.
void atenderBluetooth() {
  bool hayCliente = appConectada();
  if (hayCliente != habiaCliente) {   // La app se acaba de conectar o desconectar
    habiaCliente = hayCliente;
    Serial.println(hayCliente ? "App conectada" : "App desconectada");
    estadoPendiente = true;
    pantallaPendiente = true;
  }

  while (bluetooth.available()) {     // Arma el mensaje letra por letra hasta el salto de línea
    char letra = bluetooth.read();
    if (letra == '\n') {
      atenderOrden(lineaRecibida);
      lineaRecibida = "";
    } else if (letra != '\r' && lineaRecibida.length() < 40) {
      lineaRecibida += letra;
    }
  }

  // Envía el estado al cambiar algo y también cada 2 s para que la app sepa que seguimos conectados
  if (estadoPendiente || millis() - ultimoEnvio > INTERVALO_ESTADO_MS) {
    estadoPendiente = false;
    ultimoEnvio = millis();
    enviarEstado();
  }
}

// =====================================================================
//                           SETUP Y LOOP
// =====================================================================

void setup() {
  Serial.begin(115200);

  pinMode(TRIG_EXTERIOR, OUTPUT);
  pinMode(ECHO_EXTERIOR, INPUT);
  pinMode(TRIG_INTERIOR, OUTPUT);
  pinMode(ECHO_INTERIOR, INPUT);
  pinMode(LED_VERDE, OUTPUT);
  pinMode(LED_ROJO, OUTPUT);
  pinMode(BUZZER, OUTPUT);

  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("  Aforo Seguro");
  lcd.setCursor(0, 1);
  lcd.print("  Iniciando...");

  memoria.begin("aforo", false);
  maximo = memoria.getInt("maximo", MAXIMO_POR_DEFECTO);  // Recupera el último aforo máximo

  iniciarBluetooth(NOMBRE_BLUETOOTH);  // Desde aquí el celular puede emparejarse con "AforoSeguro"
  Serial.println("Bluetooth listo: AforoSeguro");
  delay(800);
}

void loop() {
  procesarSensores();
  atenderBluetooth();
  atenderBuzzer();

  if (pantallaPendiente) {
    pantallaPendiente = false;
    actualizarIndicadores();
  }
  delay(20);
}
