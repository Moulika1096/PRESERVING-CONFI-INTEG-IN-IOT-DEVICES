from Crypto.Random import get_random_bytes
import base64
import os.path
import sys
sys.path.append('/home/pi/btiot/pymodules')
import schedule, time
from datetime import datetime
from datetime import date
from keystore import Keystore
from aes_gcm_encr import AesGCMCrypter
from fileupload import WebdavUpload
try:
    BTIOT_DEBUG = os.environ['BTIOT_DEBUG']
except:
    BTIOT_DEBUG = False;

if not BTIOT_DEBUG:
    import RPi.GPIO as GPIO
    import Adafruit_DHT
    sensor = Adafruit_DHT.DHT11
else:
    import random

gpio = 04
local_folder = '/home/pi/btiot/thermostat'

class SensorData(object):
    def __init__(self):
        if not BTIOT_DEBUG:
            GPIO.setwarnings(False)
            GPIO.setmode(GPIO.BCM)       # Use BCM GPIO numbers
    def read_sensor_data(self):
        if not BTIOT_DEBUG:
            try:
                humidity, temperature = Adafruit_DHT.read_retry(sensor, gpio)
            except:
                pass
                humidity, temperature = 0,0
        else:
            humidity, temperature = random.randint(0,99),random.randint(0,99)
        #humidity, temperature = 23.1,12.4
        #finalTemp = "Temp :"+str(temperature)
        #finalHumid= "Humid:"+str(humidity)
        message = "Time = "+str(datetime.now())+", Temperature={0:0.1f}*C,  Humidity={1:0.1f}%".format(temperature, humidity)
        filepath = local_folder + "/file-" + date.today().isoformat() + ".txt"
        if not os.path.exists(local_folder):
            os.mkdir(local_folder)
        with open(filepath, "a") as f:
            keystoreobj =  Keystore()
            if not keystoreobj.key_file_exists():
                key = get_random_bytes(16)
                keystoreobj.save_key_to_file(key)
            key = keystoreobj.read_key_from_file()
            aes = AesGCMCrypter(key)
            encrypted = aes.encrypt(message)
            f.write(encrypted)
            f.write('\n')

def job():
    print("Executing job")
    sensordataObj =  SensorData()
    sensordataObj.read_sensor_data()
    webdavObj = WebdavUpload()
    webdavObj.upload('thermostat',local_folder)


if __name__ == '__main__':
    schedule.every(1).minutes.do(job)
    while True:
        schedule.run_pending()
        time.sleep(1)


