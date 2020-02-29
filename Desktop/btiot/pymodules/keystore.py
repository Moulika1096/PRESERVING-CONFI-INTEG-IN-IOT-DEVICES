#!/usr/bin/python
import os.path
import base64

class Keystore(object):
	def __init__(self):
		self.KEYFILE='/home/pi/btiot/key.txt'

	def read_key_from_file(self):
		open(self.KEYFILE, 'a').close()
		with open(self.KEYFILE,'r+') as f:
			data = f.readline()
			printable_key = data.rstrip('\n')
			return base64.b64decode(printable_key)
		return ''

	def save_key_to_file(self, key):
		printable_key = base64.b64encode(key)
		with open(self.KEYFILE,'w') as f:
			f.write(printable_key)

	def key_file_exists(self):
		return os.path.exists(self.KEYFILE)
