#!/usr/bin/env python
import RPi.GPIO as GPIO
import time
import signal
import sys
import datetime

ServoPin=19
ButtonPin=22

# .5 = 1/4 cup
# .3 = snack
spinDuration = float(sys.argv[1])

# Tell python what pin mode to use
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
# Setup the pins
GPIO.setup(ServoPin, GPIO.OUT)

# This function turns the servo Clock Wise
def servo_CW(ServoPIN):
  # Setup the frequency first (100Hz)
  servo = GPIO.PWM(ServoPIN, 100)
  # Then setup the duty cycle, 2.5pulses/100cycles
  servo.start(2.5)
  time.sleep(spinDuration)
  servo.stop()

# This function turns the servo counter clock wise
def servo_CCW(ServoPIN):
  servo = GPIO.PWM(ServoPIN, 100)
  servo.start(18)
  time.sleep(spinDuration)
  servo.stop()

# turn servo once each direction to try and keep it centered
servo_CW(ServoPin)
servo_CCW(ServoPin)

# log what you did
timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d_%H%M%S')
fileName = "/home/pi/catfeeder/logs/outputFood_" + timestamp + ".log"
fileContents = "spinDuration: " + str(spinDuration)
f = open(fileName,'w')
f.write(fileContents)
f.close()


