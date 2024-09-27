<div align="center">
  <img src="icon.png" width="300">
</div>

Simple compiled programming language for tutorial. 

Target Platform: Arduino

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

Test input:

````kotlin
func main(Int a) : Int
{
    Int c = 10
    print "Hello"
    return 0
}
````

Test output:

````cpp
int main(int a){
   int c=10;
   Serial.print("Hello");
   return 0;
}
````
