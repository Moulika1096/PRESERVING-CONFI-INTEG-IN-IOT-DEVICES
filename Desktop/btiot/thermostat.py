#!/usr/bin/python
from Tkinter import *
import ttk
from datetime import datetime
import subprocess
import threading
import time
import atexit
import time
import sys

sys.path.append('/home/pi/btiot/pymodules')
import os
try:
    BTIOT_DEBUG = os.environ['BTIOT_DEBUG']
except:
    BTIOT_DEBUG = False;

if not BTIOT_DEBUG:
    import RPi.GPIO as GPIO
    import Adafruit_DHT
else:
    import random



def cleanup():
	try:
		simple_agent.kill()
	except:
		pass
	try:
		rfcomm_server.kill()
	except:
		pass
	try:
		sensor_data_uploader.kill()
	except:
		pass

atexit.register(cleanup)
if not BTIOT_DEBUG:
    sensor = Adafruit_DHT.DHT11

gpio = 04
turnoffId = 0

def Refresher():
    global humidLabel
    global tempLabel
    if not BTIOT_DEBUG:
        try:
            humidity, temperature = Adafruit_DHT.read_retry(sensor, gpio)
        except:
            pass
            humidity, temperature = 0,0
    else:
        humidity, temperature = random.randint(0,99),random.randint(0,99)
    humidLabel.configure(text=str(humidity)+"%")
    tempLabel.configure(text=str(temperature)+"C")
    print('iteration')
    mainapp.after(60000, Refresher) # every minute...

def turnOffBluetooth():
	toggle_btn.config(relief="raised")
	toggle_btn['text'] = "Turn Bluetooth ON"
	toggle_bluetooth(False)

def centerApp(app,w,h):
    x = (app.winfo_screenwidth() - w) / 2
    y = (app.winfo_screenheight() - h) / 2
    app.geometry("%dx%d+%d+%d" % (w,h,x, y))

def toggle_bluetooth(isOn):
    #simple_agent_thread = threading.Thread(target=bt_simple_agent.simple_agent_function,args=())
    global simple_agent
    global rfcomm_server
    global turnoffId

    if isOn:
        toggle_btn.configure(text= "Turning BT on....")
        bt_on = subprocess.Popen(['rfkill','unblock','bluetooth'],stderr=subprocess.STDOUT)
        bt_on.wait()
        time.sleep(2)
        p = subprocess.Popen(['bluetoothctl','--agent','KeyboardDisplay'],stdout=subprocess.PIPE,stdin=subprocess.PIPE,stderr=subprocess.STDOUT)
        p.communicate(input=b'default-agent\ndiscoverable on\nquit')
        p.wait()
        #simple_agent_thread.start()
        simple_agent = subprocess.Popen(['python','pymodules/bt_simple_agent.py'],stderr=subprocess.STDOUT)
        rfcomm_server = subprocess.Popen(['python','pymodules/rfcomm_server.py'],stderr=subprocess.STDOUT)
        if turnoffId != 0:
        	print "turnoffId != 0"
        	mainapp.after_cancel(turnoffId)
        else:
        	print "turnoffId = 0"
        turnoffId = mainapp.after(300000, turnOffBluetooth) # after 300 seconds
        toggle_btn.configure(text= "Turn Bluetooth OFF")
    else:
		bt_off = subprocess.Popen(['rfkill','block','bluetooth'],stderr=subprocess.STDOUT)
		bt_off.wait()
		p = subprocess.Popen(['bluetoothctl','--agent','KeyboardDisplay'],stdout=subprocess.PIPE,stdin=subprocess.PIPE,stderr=subprocess.STDOUT)
		p.communicate(input=b'discoverable off\nquit')
		p.wait()
        #simple_agent_thread.join()
		simple_agent.kill()
		rfcomm_server.kill()

        
def toggle():
    if toggle_btn.config('relief')[-1] == 'sunken':
        toggle_btn.config(relief="raised")
        toggle_btn['text'] = "Turn Bluetooth ON"
        toggle_bluetooth(False)
    else:
        toggle_btn.config(relief="sunken")
        toggle_btn['text'] = "Turn Bluetooth OFF"
        toggle_bluetooth(True)

    print toggle_btn['relief']


mainapp = Tk()
mainapp.title(os.getcwd())

style = ttk.Style()
current_theme =style.theme_use()
style.theme_settings(current_theme, {"TNotebook.Tab": {"configure": {"foreground":"white","background":"blue","align":"left", "width":"20"},
"map":{"background": [("selected", "blue")] }},"TNotebook": {"configure": {"mintabwidth": 120}}})

centerApp(mainapp,500,280)

tabs_parent = ttk.Notebook(mainapp)

# Home
tab1 = Frame(tabs_parent)

print mainapp.winfo_screenmmwidth()/4
dt = datetime.now()


dateLabel = Label(tab1,text=dt.strftime('%A, %B %d'),wraplength=200,padx=15)
dateLabel.configure(font=("Courier", 24))
dateLabel.pack(side="left",anchor="w")

sensorData = Frame(tab1)

humidLabel = Label(sensorData,text="49%")
humidLabel.pack(side="top")
#humidLabel.grid(sticky=W,row=0, column=0,padx=5, pady=5)
humidLabel.configure(font=("Courier", 24))

tempLabel = Label(sensorData,text="76F")
tempLabel.pack(side="bottom")
#tempLabel.grid(sticky=E,row=0, column=1,padx=5, pady=5)
tempLabel.configure(font=("Courier", 72))

#sensorData.grid(row=0,column=1,sticky='E',columnspan=4)
sensorData.pack(side="right", expand='true')


#image.grid(row=0, column=2, columnspan=2, rowspan=2,
#           sticky=W+E+N+S, padx=5, pady=5)

#Settings
tab2 = Frame(tabs_parent)
toggle_btn = Button(tab2,text="Turn Bluetooth ON", width=12, relief="raised", command=toggle)
toggle_btn.pack(side='top')

tabs_parent.add(tab1,text="Home")
tabs_parent.add(tab2,text="Settings")
tabs_parent.pack(expand=1, fill='both')
global sensor_data_uploader
sensor_data_uploader = subprocess.Popen(['python','read_sensor_upload.py'],stderr=subprocess.STDOUT)
Refresher()
mainapp.mainloop()
