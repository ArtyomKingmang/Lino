# Lino

Compiled programming language. Target Platform: Arduino

Example:

````kotlin
Int ledPin = 2

func setup() : Void {
  pinMode(ledPin, OUTPUT)
}

func loop() : Void {
  
  digitalWrite(ledPin, HIGH)
  delay(200)
  
  digitalWrite(ledPin, LOW)
  delay(200)
}

````