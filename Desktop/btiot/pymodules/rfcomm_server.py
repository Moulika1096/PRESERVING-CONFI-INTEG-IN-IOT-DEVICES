# file: rfcomm-server.py
#!/usr/bin/python
from bluetooth import *
import os.path
import sys
sys.path.append('/home/pi/btiot/pymodules')

from Crypto.Random import get_random_bytes
import base64
from keystore import Keystore
from aes_gcm_encr import AesGCMCrypter
from dic_store import DictionaryStore
from fileupload import WebdavUpload

print("rfcomm server started")

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

while True:
    try:
        print("Waiting for connection on RFCOMM channel %d" % port)
        client_sock, client_info = server_sock.accept()
        print("Accepted connection from ", client_info)
        while True:
            try:
                data = client_sock.recv(1024)
                if len(data) == 0: break
                print ("data received = " + data)
                n = data.find("getkey")
                print ("n = "+str(n))
                if(n == 0):
                    n = data.find("\r\n")
                    print(n)
                    cmd_deviceid = data[0:n].rstrip().split(" ")
                    print(cmd_deviceid)
                    deviceid = cdatamd_deviceid[1]
                    print ("Deviceid from device getkey command is "+deviceid)
                    server_keys_folder = 'keys'
                    local_server_keys_dir_path = '/home/pi/btiot/keys'
                    localKeysObj =  DictionaryStore('/home/pi/btiot/local_keys.json')
                    serverKeysObj =  DictionaryStore(local_server_keys_dir_path+'/server_keys.json')
                    keystoreobj =  Keystore()

                    data_key = keystoreobj.read_key_from_file()
                    encoded_data_key = base64.b64encode(data_key)

                    print ("data key is "+encoded_data_key)
                    device_key = get_random_bytes(16)
                    encoded_device_key = base64.b64encode(device_key)
                    print ("device_key  is "+encoded_device_key)
                    #Encrypt data key using device key
                    aes_encr_obj = AesGCMCrypter(device_key)
                    server_keys = serverKeysObj.read_from_file()
                    print server_keys
                    pos = 0
                    for x in server_keys:
                        print x
                        if (x['deviceid'] == deviceid):
                            print("position = "+str(pos))
                            break
                        pos=pos+1
                    if (pos < len(server_keys)):
                        print pos
                        del server_keys[pos]
                        print(server_keys)
                    print("final position = "+str(pos)) 
                    server_key = {}
                    server_key['deviceid'] = deviceid
                    server_key['key'] = aes_encr_obj.encrypt(data_key)
                    server_keys.append(server_key)
                    #Save server keys file for upload
                    serverKeysObj.save_to_file(server_keys)
                   
                    print ("Saved server side keys "+str(server_keys))
                    webdavObj = WebdavUpload()
                    webdavObj.upload(server_keys_folder,local_server_keys_dir_path)
                    client_sock.send(encoded_device_key+"\n")

                else: 
                    client_sock.close()
                    break;
                print("received [%s]" % data)
            except Exception as e:
                print(e)
                client_sock.close()
                break
                
    except Exception as e:
        print(e)
        pass
server_sock.close()
print("all done")


