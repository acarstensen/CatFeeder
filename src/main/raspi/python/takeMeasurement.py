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

# figure out max valid reading
maxValidDistance = 11.1
if foodOrWater == "Water":
    maxValidDistance = 8.05
print "Using maxValidDistance: " + str(maxValidDistance)

# try and find 10 valid measurements so you can calculate a nice average
readings = []
for i in range(0, 100):
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
    print "Distance to " + foodOrWater + ": " + str(distance) + " inches"

    # calculate amount remaining
    amountRemaining = maxValidDistance - distance
    print "Amount of " + foodOrWater + " remaining: " + str(amountRemaining) + " inches"

    # append to list
    if amountRemaining >= 0 and amountRemaining <= maxValidDistance:
        readings.append(amountRemaining)

    # exit loop if we have 10 valid readings
    if len(readings) == 10:
        break

if len(readings) == 10:
    # sort the list
    print "unsorted: " + str(readings)
    readings.sort()
    print "sorted: " + str(readings)

    # remove the first and last to get rid of outliers
    del readings[-1]
    del readings[-1]
    del readings[0]
    del readings[0]
    print "after removal: " + str(readings)

    # calculate average
    avgAmountRemaining = float(sum(readings)) / max(len(readings), 1)
    print "Average amount of " + foodOrWater + " remaining: " + str(avgAmountRemaining) + " inches"

    # if less than 0, 0
    if avgAmountRemaining < 0:
        avgAmountRemaining = 0
        print "Rounding up negative number to 0."
else:
    print "We didn't get 10 readings!?: " + str(readings)
    print "Setting reading to -1"
    avgAmountRemaining = -1

# print to log file
timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d_%H%M%S')
fileName = "/home/pi/catfeeder/logs/measure" + foodOrWater + "_" + timestamp + ".log"
fileContents = str(avgAmountRemaining) + " inches"
f = open(fileName,'w')
f.write(fileContents)
f.close()
