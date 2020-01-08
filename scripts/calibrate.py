import numpy as np
import argparse
import cv2
import json
import urllib.request
import socket
import time

numpixels = 400

print("var pixels = [")
# Iterate through all the pixels
for x in range(0, numpixels):

	# Turn off all the pixels, except this one - turn it on full white
	frame = bytearray(numpixels * 3)
	frame[(x*3)+0] = 0xFF
	frame[(x*3)+1] = 0xFF
	frame[(x*3)+2] = 0xFF

	sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	sock.sendto(frame, ('192.168.4.5', 2390)) # IP Address of ESP8266 device
	time.sleep(0.1) # give the network some time to send this packet

	# Request JPEG image from video camera
	req = urllib.request.Request('http://192.168.0.23/picture/1/current/') # This URL for my MotionEye OS camera
	response = urllib.request.urlopen(req)


	# Find the co-ordinates of the brightest spot in the photo
	# From https://www.pyimagesearch.com/2014/09/29/finding-brightest-spot-image-using-python-opencv/
	arr = np.asarray(bytearray(response.read()), dtype=np.uint8)
	image = cv2.imdecode(arr,-1) 
	gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
	gray = cv2.GaussianBlur(gray, (41, 41), 0)
	(minVal, maxVal, minLoc, maxLoc) = cv2.minMaxLoc(gray)

	# Print the result
	print("["+str(maxLoc[0])+","+str(maxLoc[1])+"],")

print("];")


