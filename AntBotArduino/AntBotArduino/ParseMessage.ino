byte msgChars[64];
int charCounter = 0;
int charsInMessage = 0;
void parseMessageNew(){
  charCounter = 0;
  while(Serial.peek() != -1){
    msgChars[charCounter] = Serial.read();
    charCounter++;
    delayMicroseconds(100);
  }
  charsInMessage = charCounter;
  charCounter = 0;
  //New String to put in the Queue.
  String message = "";
  //Check what is coming in. If it starts with a 't' it's a new moving command.
  //If it starts with an 'h' the Rover should stop everything.
  if(msgChars[charCounter] == 't') {
    //Set Flags for state distinguishing.
    message = "";
    messageBegin = true;
    decimal = false;
    decimalCounter = 0;
    breakCounter = 0;
    deletedZero = false;
    //First get the Turning part of the message. It ends wit a " m"
    while(msgChars[charCounter] != 'm' && breakCounter < 64){
      //While the read char is part of the turn message we append the String.
      //Since the accuracy of the rover is to low anyway we discard any decimal information.
      if(!decimal){
        //This discards any leading zeros.
        if((message.equals("t ") || message.equals("t -")) && msgChars[charCounter] == '0'){
          //Store the last char before removing it from the buffer.
          lastChar = msgChars[charCounter];
          charCounter++;
          //Set flag, that a zero was deleted.
          deletedZero = true;
        }
        //If the decimal point comes in we set the Flag for being in decimal value and discard the decimal point.
        else if (msgChars[charCounter] == '.' || msgChars[charCounter] == ','){
          decimal = true;
        }
        //Otherwise we append the String until the 'm' arrives. It also gets rid of all non number characters
        //exept the beginning of the message, which is needed for the command.
        else if ((msgChars[charCounter] >= '0' && msgChars[charCounter] <= '9') || messageBegin){
          //Store the last char before removing it from the buffer.
          lastChar = msgChars[charCounter];
          charCounter++;
          //Convert char to String
          String tmp_ = String(lastChar);
          //Append to message
          message += tmp_;
          if(messageBegin){
            if(lastChar == ' ' && msgChars[charCounter] != '-'){
              messageBegin = false;
            } else if(lastChar == '-'){
              messageBegin = false;
            }
          }
        }
        else {
          //Store the last char before removing it from the buffer.
          lastChar = msgChars[charCounter];
          charCounter++;
        }
      }
      //Everything following the decimal point gets discarded. But it needs to be read out to empty the Serial buffer.
      else {
        //Store the last char befor removing it from the buffer.
        lastChar = msgChars[charCounter];
        charCounter++;
      }
      if(msgChars[charCounter] == 'm'){
        if(lastChar != ' ' || message.equals("t ") && !deletedZero){
          lastChar = msgChars[charCounter];
          charCounter++;
        }
      }
      breakCounter++;
      //Delay is needed here because the Serial.read() commands can't execute fast enough and the system crashes.
    }
    //Put the message in the Queue if it's not empty or has no angle distance information after parsing.
    if(message.length() != 0 && !message.equals("t ") && !message.equals("t -")){
      if (msgChars[charCounter] == 'm'){
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
    while(msgChars[charCounter] != 'n' && msgChars[charCounter] != -1 && breakCounter < 64){
      if(!decimal || (decimal && decimalCounter <= decimalAccuracy)){
        
        //Again we don't want the information filling up the queue if there is no command.
        //So we check, if the predecimal of the move command is 0. If so we check, if there is a decimal part.
        //Never trust the user so also check, if the move command equals "-0" or something like "0.00"
        //Or if there are any leading zeros or letters in between (typo or something) and get rid of all of those.
        if((message.equals("m ") || message.equals("m -")) && msgChars[charCounter] == '0'){
          lastChar = msgChars[charCounter];
          charCounter++;
          String tmp_ = String(lastChar);
          message += tmp_;
          appended = true;
          if(msgChars[charCounter] != '.' && msgChars[charCounter] != ','){
            message.remove(message.length()-1);
            appended = false;
            deletedZero = true;
          }
        }
        
        else if((msgChars[charCounter] >= '0' && msgChars[charCounter] <= '9') || msgChars[charCounter] == '.' || msgChars[charCounter] == ',' || messageBegin){
          if(msgChars[charCounter] == '.' || msgChars[charCounter] == ','){
            decimal = true;
          }
          lastChar = msgChars[charCounter];
          charCounter++;
          if(lastChar == ','){
            lastChar = '.';
          }
          String tmp_ = String(lastChar);
          message += tmp_;
          appended = true;
          if(messageBegin){
            if(lastChar == ' ' && msgChars[charCounter] != '-'){
              messageBegin = false;
            } else if(lastChar == '-'){
              messageBegin = false;
            }
          }
        }
        
        else {
          lastChar = msgChars[charCounter];
          charCounter++;
          appended = false;
        }
        if(decimal && appended){
          decimalCounter++;
        }
      } else {
        lastChar = msgChars[charCounter];
        charCounter++;
        appended = false;
      }
      if(msgChars[charCounter] == 'n'){
        if(lastChar != ' ' || message.equals("m ") && !deletedZero){
          lastChar = msgChars[charCounter];
          charCounter++;
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
  } else if (msgChars[charCounter] == 'h'){
    lastChar = msgChars[charCounter];
    //Serial.println("HALT");
    commandQueue = "";
    finished = true;
  } else{
    //Unknown Message Type -> Do nothing
  }
  for(int n = 0; n < sizeof(msgChars); n++){
    msgChars[n] = 0;
  }
}
