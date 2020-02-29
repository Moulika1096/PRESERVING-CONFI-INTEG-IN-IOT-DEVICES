#!/usr/bin/python
import os.path
import json
class DictionaryStore(object):
	def __init__(self, keyfile):
		self.keyfile = keyfile

	def read_from_file(self):
		open(self.keyfile, 'a').close()
		with open(self.keyfile,'r+') as f:
			try:
				data = json.load(f)
			except:
				return []
			return data
		return []

	def save_to_file(self, data):
		with open(self.keyfile,'w') as f:
			json.dump(data,f)

