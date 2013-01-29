

String dataString;
boolean dump=false;

//  VARIABLES
int pulsePin = 1;                 // Pulse Sensor purple wire connected to analog pin 0
int blinkPin = 13;                // pin to blink led at each beat
double smoothedBPM = 0;
#define EXP 0.05

// these variables are volatile because they are used during the interrupt service routine!
volatile int BPM;                   // used to hold the pulse rate
volatile int Signal;                // holds the incoming raw data
volatile int IBI = 600;             // holds the time between beats, the Inter-Beat Interval
volatile boolean Pulse = false;     // true when pulse wave is high, false when it's low
volatile boolean QS = false;        // becomes true when Arduoino finds a beat.

void setup(){
  pinMode(blinkPin,OUTPUT);         // pin that will blink to your heartbeat!
  Serial.begin(38400);             // we agree to talk fast!

  interruptSetup();                 // sets up to read Pulse Sensor signal every 2mS 
}

void loop(){
  if (QS == true){                       // Quantified Self flag is true when arduino finds a heartbeat
        //dataString = String(BPM);
        if(smoothedBPM == 0) {
          smoothedBPM = BPM;
        } else {  
          smoothedBPM = smoothedBPM*(1-EXP) + EXP*BPM;
        }  
        int v = smoothedBPM;
        dataString = String(v);

        QS = false;                      // reset the Quantified Self flag for next time    
     }
  
  if (Serial.available()){
    char c = Serial.read();
    if(c == '*') {
        if(dataString != ""){
          Serial.println(dataString);
          dataString = "";
        }  
    }
  }
  
  delay(20);                             //  take a break
}






