
#include <SoftwareSerial.h>


String dataString;
boolean dump=false;
SoftwareSerial blueToothSerial(6, 7);

 
//  VARIABLES
int pulsePin = 1;                 // Pulse Sensor purple wire connected to analog pin 0
int blinkPin = 13;                // pin to blink led at each beat
int fadePin = 5;                  // pin to do fancy classy fading blink at each beat
int fadeRate = 0;                 // used to fade LED on with PWM on fadePin


// these variables are volatile because they are used during the interrupt service routine!
volatile int BPM;                   // used to hold the pulse rate
volatile int Signal;                // holds the incoming raw data
volatile int IBI = 600;             // holds the time between beats, the Inter-Beat Interval
volatile boolean Pulse = false;     // true when pulse wave is high, false when it's low
volatile boolean QS = false;        // becomes true when Arduoino finds a beat.
int i=0;

void setup(){
  pinMode(blinkPin,OUTPUT);         // pin that will blink to your heartbeat!
  pinMode(fadePin,OUTPUT);          // pin that will fade to your heartbeat!
  Serial.begin(115200);             // we agree to talk fast!
  setupBluetooth();  

  interruptSetup();                 // sets up to read Pulse Sensor signal every 2mS 
   // UN-COMMENT THE NEXT LINE IF YOU ARE POWERING The Pulse Sensor AT LOW VOLTAGE, 
   // AND APPLY THAT VOLTAGE TO THE A-REF PIN
   //analogReference(EXTERNAL);   
}



void loop(){
  //sendDataToProcessing('S', Signal);     // send Processing the raw Pulse Sensor data
  if (QS == true){                       // Quantified Self flag is true when arduino finds a heartbeat
        //sendDataToProcessing('B',BPM);   // send heart rate with a 'B' prefix
        dataString = String(BPM);
        //sendDataToProcessing('Q',IBI);   // send time between beats with a 'Q' prefix
        QS = false;                      // reset the Quantified Self flag for next time    
        if(dump){
            Serial.println(dataString);
        }   
     }
  
  if (blueToothSerial.available()){
    char c = blueToothSerial.read();
    Serial.write(c);
    if(c == '*') {
        blueToothSerial.println(dataString);
        Serial.println(dataString);
    }
  }
  
  if(Serial.available()){
    char c = Serial.read();
    Serial.write(c);
    if(c == '*'){
        if(dump) dump = false;
        else dump = true;
    } 
  }
  
  
  delay(20);                             //  take a break
}


void sendDataToProcessing(char symbol, int data ){
    Serial.print(symbol);                // symbol prefix tells Processing what type of data is coming
    Serial.println(data);                // the data to send culminating in a carriage return
  }

void setupBluetooth(){
  pinMode(9, INPUT);
  blueToothSerial.begin(38400);
/*  blueToothSerial.write("\r\n+STNA=BLUETEMP\r\n");
  blueToothSerial.write("\r\n+STWMOD=0\r\n");
  blueToothSerial.write("\r\n+STAUTO=1\r\n");
  blueToothSerial.write("\r\n+STOAUT=1\r\n");
  delay(2000); // This delay is required.
  if(digitalRead(9) == HIGH){  
    delay(2000); // This delay is required.
    blueToothSerial.write("\r\n+INQ=1\r\n");
    delay(2000); // This delay is required.
  }*/
}  





