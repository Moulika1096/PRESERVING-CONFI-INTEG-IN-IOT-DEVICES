#!/usr/bin/python
import webdav.client as wc
#client.mkdir(SERVER_DIR)

#client.push(remote_directory=SERVER_DIR,local_directory=CLIENT_DIR)
class WebdavUpload(object):
    def __init__(self):
        self.SERVER_DIR=''
        SERVER_URL=""
        WEBDAV_USER=""
        WEBDAV_PASSWORD=""
        options = {
          'webdav_hostname': SERVER_URL,
          'webdav_login': WEBDAV_USER,
          'webdav_password': WEBDAV_PASSWORD
        }
        self.client = wc.Client(options)

    def upload(self,server_dir,client_dir_path):
        self.client.upload_sync(remote_path=self.SERVER_DIR+'/'+server_dir,local_path=client_dir_path)
