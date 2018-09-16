import RPi.GPIO as GPIO
import time
import datetime
import sys

# read in args
trigPin = int(sys.argv[1])
echoPin = int(sys.argv[2])
foodOrWater = sys.argv[3]

# GPIO setup
TRIG = trigPin
ECHO = echoPin
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(TRIG,GPIO.OUT)
GPIO.setup(ECHO,GPIO.IN)

# send a quick sound wave
GPIO.output(TRIG, True)
time.sleep(0.00001)
GPIO.output(TRIG, False)

# measure the time it takes to bounce
while GPIO.input(ECHO)==0:
  pulse_start = time.time()

while GPIO.input(ECHO)==1:
  pulse_end = time.time()

# calculate distance
pulse_duration = pulse_end - pulse_start
distance = pulse_duration * 6751.95
distance = round(distance, 2)

# output distance
print "Distance to " + foodOrWater + ": " + str(distance) + " inches"

timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d_%H%M%S')
fileName = "/home/pi/catfeeder/logs/measure" + foodOrWater + "_" + timestamp + ".log"
fileContents = str(distance) + " inches"
f = open(fileName,'w')
f.write(fileContents)
f.close()
