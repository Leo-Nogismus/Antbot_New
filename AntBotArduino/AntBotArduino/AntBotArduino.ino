// Library can be found here: http://www.pjrc.com/teensy/td_libs_Encoder.html
#include <Encoder.h>

// Heartbeat
#define HEARTBEAT_MS 1000
unsigned long last_heartbeat;

// Wheel 1
#define WHEEL_1_PWM 44
#define WHEEL_1_DIR 35
Encoder wheel_1_encoder(21, 32);
int wheel_1_pwm;
int wheel_1_dir;
int new_wheel_1_count;
int wheel_1_count;

// Wheel 2
#define WHEEL_2_PWM 46
#define WHEEL_2_DIR 27
Encoder wheel_2_encoder(20, 24);
int wheel_2_pwm;
int wheel_2_dir;
int new_wheel_2_count;
int wheel_2_count;

// Wheel 3
#define WHEEL_3_PWM 45
#define WHEEL_3_DIR 16
Encoder wheel_3_encoder(18, 17);
int wheel_3_pwm;
int wheel_3_dir;
int new_wheel_3_count;
int wheel_3_count;

// Wheel 4
#define WHEEL_4_PWM 6
#define WHEEL_4_DIR 5
Encoder wheel_4_encoder(2, 4);
int wheel_4_pwm;
int wheel_4_dir;
int new_wheel_4_count;
int wheel_4_count;

//The command queue, ech command is devided with a comma. The command looks either like i.e. "t 90" or "m 1.5". 
String commandQueue;
//Flag to mark the decimal point.
boolean decimal = false;
//Flag to mark the beginning of a message
boolean messageBegin = false;
//Flag to show, that leading zeroes were deleted.
boolean deletedZero = false;

//Accuracy for the distance parameter
int decimalAccuracy = 2;
int decimalCounter = 0;
//Stores the last read character, needed to distinguish between end of message and random char.
char lastChar;
//Kills the loops if the runntime is longer, than the arduino buffer.
int breakCounter = 0;
//Used for debug only
unsigned long timer = 0;

boolean finished = false;

void setup() {
  
  Serial.begin(115200);
  // Setup Pins
  pinMode(WHEEL_1_PWM, OUTPUT);
  pinMode(WHEEL_1_DIR, OUTPUT);
  pinMode(WHEEL_2_PWM, OUTPUT);
  pinMode(WHEEL_2_DIR, OUTPUT);
  pinMode(WHEEL_3_PWM, OUTPUT);
  pinMode(WHEEL_3_DIR, OUTPUT);
  pinMode(WHEEL_4_PWM, OUTPUT);
  pinMode(WHEEL_4_DIR, OUTPUT);

}

void loop() {

  checkHeartbeat();
    
  if(commandQueue.length() == 0){
    checkIncomingMessage();
  } else {
    int commaPosition = commandQueue.indexOf(',');
    String command = commandQueue.substring(0, commaPosition);
    commandQueue.remove(0, commaPosition + 1);
    int beginOfNumber = command.indexOf(' ');
    String commandType = command.substring(0, beginOfNumber);
    command.remove(0, beginOfNumber + 1);
    if(commandType == "t"){
      float angle = command.toFloat();
      turning(angle);
    } else if (commandType == "m"){
      float distance = command.toFloat();
      moving(distance);
      
    }
  }
}

void checkHeartbeat(){
  if (last_heartbeat + HEARTBEAT_MS <= millis())
    {
        // Set hearbeat and reset timer
        Serial.print("x ");
        Serial.print( millis()/1000 );
        last_heartbeat = millis();
        Serial.println(" n");
    }
}

void checkIncomingMessage(){
  if(Serial.peek() != -1){
    finished = false;
    parseMessageNew();
  }
}
/*
//Parses an incoming Message and adds the commands to the Command Queue.
//Each Incoming Command gets executed FIFO-style.
//Halt commands interrupt the whole progress and empty the Command Queue.
//A lot of stuff is for exceptions. In this code every message with the form "t x m y n" is accepted, parsed, if neccessary cropped and stored.
//x and y can be of any form. The code deletes all non number characters, leading zeros, and everything after the decimal accuracy.
void parseMessage(){
  //New String to put in the Queue.
  String message = "";
  //Check what is coming in. If it starts with a 't' it's a new moving command.
  //If it starts with an 'h' the Rover should stop everything.
  if(Serial.peek() == 't') {
    //Set Flags for state distinguishing.
    message = "";
    messageBegin = true;
    decimal = false;
    decimalCounter = 0;
    breakCounter = 0;
    deletedZero = false;
    //First get the Turning part of the message. It ends wit a " m"
    while(Serial.peek() != 'm' && Serial.peek() != -1 && breakCounter < 64){
      //While the read char is part of the turn message we append the String.
      //Since the accuracy of the rover is to low anyway we discard any decimal information.
      if(!decimal){
        //This discards any leading zeros.
        if((message.equals("t ") || message.equals("t -")) && Serial.peek() == '0'){
          //Store the last char before removing it from the buffer.
          lastChar = Serial.read();
          //Set flag, that a zero was deleted.
          deletedZero = true;
        }
        //If the decimal point comes in we set the Flag for being in decimal value and discard the decimal point.
        else if (Serial.peek() == '.' || Serial.peek() == ','){
          decimal = true;
        }
        //Otherwise we append the String until the 'm' arrives. It also gets rid of all non number characters
        //exept the beginning of the message, which is needed for the command.
        else if ((Serial.peek() >= '0' && Serial.peek() <= '9') || messageBegin){
          //Store the last char before removing it from the buffer.
          lastChar = Serial.read();
          //Convert char to String
          String tmp_ = String(lastChar);
          //Append to message
          message += tmp_;
          if(messageBegin){
            if(lastChar == ' ' && Serial.peek() != '-'){
              messageBegin = false;
            } else if(lastChar == '-'){
              messageBegin = false;
            }
          }
        }
        else {
          //Store the last char before removing it from the buffer.
          lastChar = Serial.read();
        }
      }
      //Everything following the decimal point gets discarded. But it needs to be read out to empty the Serial buffer.
      else {
        //Store the last char befor removing it from the buffer.
        lastChar = Serial.read();
      }
      if(Serial.peek() == 'm'){
        if(lastChar != ' ' || message.equals("t ") && !deletedZero){
          lastChar = Serial.read();
        }
      }
      breakCounter++;
      //Delay is needed here because the Serial.read() commands can't execute fast enough and the system crashes.
      delayMicroseconds(50);
    }
    //Put the message in the Queue if it's not empty or has no angle distance information after parsing.
    if(message.length() != 0 && !message.equals("t ") && !message.equals("t -")){
      if (Serial.peek() == 'm'){
        commandQueue += message;
        commandQueue += ",";
      }
    }
    
    //The next step is to get the move command. The message is emptied and rewritten to.
    message = "";
    messageBegin = true;
    decimal = false;
    decimalCounter = 0;
    breakCounter = 0;
    deletedZero = false;
    boolean appended = false;
    while(Serial.peek() != 'n' && Serial.peek() != -1 && breakCounter < 64){
      if(!decimal || (decimal && decimalCounter <= decimalAccuracy)){
        
        //Again we don't want the information filling up the queue if there is no command.
        //So we check, if the predecimal of the move command is 0. If so we check, if there is a decimal part.
        //Never trust the user so also check, if the move command equals "-0" or something like "0.00"
        //Or if there are any leading zeros or letters in between (typo or something) and get rid of all of those.
        if((message.equals("m ") || message.equals("m -")) && Serial.peek() == '0'){
          lastChar = Serial.read();
          String tmp_ = String(lastChar);
          message += tmp_;
          appended = true;
          if(Serial.peek() != '.' && Serial.peek() != ','){
            message.remove(message.length()-1);
            appended = false;
            deletedZero = true;
          }
        }
        
        else if((Serial.peek() >= '0' && Serial.peek() <= '9') || Serial.peek() == '.' || Serial.peek() == ',' || messageBegin){
          if(Serial.peek() == '.' || Serial.peek() == ','){
            decimal = true;
          }
          lastChar = Serial.read();
          if(lastChar == ','){
            lastChar = '.';
          }
          String tmp_ = String(lastChar);
          message += tmp_;
          appended = true;
          if(messageBegin){
            if(lastChar == ' ' && Serial.peek() != '-'){
              messageBegin = false;
            } else if(lastChar == '-'){
              messageBegin = false;
            }
          }
        }
        
        else {
          lastChar = Serial.read();
          appended = false;
        }
        if(decimal && appended){
          decimalCounter++;
        }
      } else {
        lastChar = Serial.read();
        appended = false;
      }
      if(Serial.peek() == 'n'){
        if(lastChar != ' ' || message.equals("m ") && !deletedZero){
          lastChar = Serial.read();
        }
      }
      breakCounter++;
      //Delay is needed here because the Serial.read() commands can't execute fast enough and the system crashes.
      delayMicroseconds(50);
    }
    lastChar = Serial.read();
    if(message.length() != 0 && !message.equals("m ") && !message.equals("m -")){
      if(message.substring(message.length()-4) != "0.00"){
        commandQueue += message;
        commandQueue += ",";
      }
    }
  } else if (Serial.peek() == 'h'){
    lastChar = Serial.read();
    //Serial.println("HALT");
    commandQueue = "";
    finished = true;
  } else{
    //Unknown Message Type -> Do nothing
  }
}*/

