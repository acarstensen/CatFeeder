#!/usr/bin/env python
import RPi.GPIO as GPIO
import time
import signal
import datetime

# when a user hit's CTRL-C, then wipe the lights
def signal_handler(signal, frame):
    GPIO.cleanup()
    sys.exit(0)


ServoPin=19
ButtonPin=22

# .5 = 1/4 cup
# .3 = snack
spinDuration=.3

# Tell python what pin mode to use
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
# Setup the pins
GPIO.setup(ServoPin, GPIO.OUT)
GPIO.setup(ButtonPin, GPIO.IN)

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


prev_input = GPIO.input(ButtonPin)
while True:
	input = GPIO.input(ButtonPin)

	if (input != prev_input):
		if (input == 1):
			print("Button released - clockwise")
			servo_CW(ServoPin)
        	elif (input == 0):
                	print("Button pressed - counter clockwise")
			servo_CCW(ServoPin)

		prev_input = input

                # output log file
                timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d_%H%M%S')
                fileName = "/home/pi/catfeeder/logs/switchServoControl_" + timestamp + ".log"
                fileContents = "spinDuration: " + str(spinDuration)
                f = open(fileName,'w')
                f.write(fileContents)
                f.close()
	
	time.sleep(0.05)

