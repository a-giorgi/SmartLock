#include <ESP8266HTTPClient.h>
#include <ESP8266WiFi.h>
#include <WiFiClientSecure.h>
#include <Ticker.h>
#include <SD.h>
#include <SPI.h> 


String ssid = "";
String password = "";
const char* host = "www.YOURHOST.com";
const char* code = "PUT_DEVICE_CODE_HERE";
const char* link = "/updateStatus.php";
const int httpsPort = 443;
const char fingerprint[] PROGMEM = "CERTIFICATE_FINGERPRINT";
                                 
int inPin = 5;
uint32_t timer_start = 0;
bool lockStatus = false;


bool sdRead(){
  delay(1000);
  if(!SD.begin(4)){
    Serial.println("I couldn't read SD card...");
    return false;
  }else{
    File wifiKeys = SD.open("wifi.txt");
    if (wifiKeys) {
      ssid = "";
      password = "";
      bool found = false;
      while (wifiKeys.available()) {
        char character = char(wifiKeys.read());
        if(character == '\n'){
          Serial.println("ENDLINE"); //now I'm on the second line
          found = true;
        }else if(character != '\r'){
          Serial.print(character);
          if(!found){ //if on the first line write SSID
              ssid = ssid + character;
          }else{//if on the second line write the password
              password = password + character;
          }
        }
      }
      wifiKeys.close();
      return true;
    }else{
      Serial.println("I couldn't read the File...");
      return false;
    }
  
  }

}

//instruction to connet to the wifi connection
void connect(){
  while (WiFi.status() != WL_CONNECTED) {
    delay(300);
    digitalWrite(LED_BUILTIN, LOW);
    delay(300);
    digitalWrite(LED_BUILTIN, HIGH); 
  }
}


//function to send the status to the server
bool sendStatus(){
  //I need the status as a string
  String status = "false";   
  if(lockStatus){
     status = "true"; 
  }
  String content = "status=" + status + "&code=" + code;
  Serial.println("connecting");
  Serial.println("param: "+status);
  WiFiClientSecure httpsClient;
  httpsClient.setFingerprint(fingerprint);
  httpsClient.setTimeout(15000); // 15 Seconds
  int r=0; //retry counter
  while((!httpsClient.connect(host, httpsPort)) && (r < 30)){
      delay(100);
      Serial.print(".");
      r++;
  }
  
  if(r==30) {
    Serial.println("Connection failed");
    return false;
  } else {
    Serial.println("Connected to web");
  }
  //resdy to connect!
   httpsClient.print(String("POST ") + link + " HTTP/1.1\r\n" +
               "Host: " + host + "\r\n" +
               "Content-Type: application/x-www-form-urlencoded"+ "\r\n" +
               "Content-Length: "+content.length() + "\r\n\r\n" +
               content + "\r\n" +
               "Connection: close\r\n\r\n");
  Serial.println("request sent");             
  blinkLed(); 
  return true;
}

void blinkLed(){
  int i = 0;
  while(i < 4){
    delay(50);
    digitalWrite(LED_BUILTIN, LOW);
    delay(50);
    digitalWrite(LED_BUILTIN, HIGH);
    i++;
  }
  if(!lockStatus){
      digitalWrite(LED_BUILTIN, LOW);
  }
}

void setup() {
  //initializing input output
  pinMode(inPin, INPUT); 
  pinMode(LED_BUILTIN, OUTPUT);
  Serial.begin(115200);
  
  //fetching the WiFi credential
  while(!sdRead()){
    delay(2000);
    blinkLed();
  }

  //connecting to the WiFi Network
  WiFi.begin(ssid, password);
  if(WiFi.status() != WL_CONNECTED){
    connect();  
  }
  
  /*byte val = digitalRead(inPin);
  if(val == HIGH){
    lockStatus = false;
  }else{
    lockStatus = true;
  }*/
  
  //starting timer to ping the server
  timer_start = millis();
  
  //detecting and sending the initial status
  byte initLock = digitalRead(inPin);
  if(initLock == HIGH){
     digitalWrite(LED_BUILTIN, LOW);
     lockStatus = false;
     sendStatus();
     digitalWrite(LED_BUILTIN, LOW);  
  }else{
     digitalWrite(LED_BUILTIN, HIGH);
     lockStatus = true;
     sendStatus();
     digitalWrite(LED_BUILTIN, HIGH);
  }
  
}

void loop() {
  //whenever I found the wifi disconnected I'll try to connect again
  if(WiFi.status() != WL_CONNECTED){
    connect();  
  }

  //detecting the status
  byte val = digitalRead(inPin);
  if(val == HIGH){
    if(lockStatus){
      digitalWrite(LED_BUILTIN, LOW);
      lockStatus = false;
      sendStatus();
      digitalWrite(LED_BUILTIN, LOW);
    }
  }else{
    if(!lockStatus){
      digitalWrite(LED_BUILTIN, HIGH);
      lockStatus = true;
      sendStatus();
      digitalWrite(LED_BUILTIN, HIGH);
    }
  }

  //every two minutes I'll send the status anyway to notify that the device is still online
  if(millis() - timer_start > 120000){
    Serial.println("current millis: "+ millis());
    sendStatus();
    timer_start = millis();
  }
}
