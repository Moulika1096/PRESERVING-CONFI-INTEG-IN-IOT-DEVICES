from Crypto.Cipher import AES
from Crypto.Random import get_random_bytes
import base64

class AesGCMCrypter(object):

    def __init__(self, key):
        self.key = key

    def encrypt(self, sensitive_data):

        kdf_salt = get_random_bytes(16)

        # Encrypt using AES GCM
        cipher = AES.new(self.key, AES.MODE_GCM)
        ciphertext, tag = cipher.encrypt_and_digest(sensitive_data)

        # Nonce is generated randomly if not provided explicitly
        nonce = cipher.nonce

        # Message to transmit/share
        transmitted_message = base64.b64encode(ciphertext)+":"+base64.b64encode(tag)+":"+base64.b64encode(nonce)+":"+base64.b64encode(kdf_salt)

        return transmitted_message

    def decrypt(self, receivedmsg):
        params = receivedmsg.split(":")
        received_ciphertext = base64.b64decode(params[0])
        received_tag = base64.b64decode(params[1])
        received_nonce = base64.b64decode(params[2])
        received_kdf_salt = base64.b64decode(params[3])

        decryption_key = self.key

        cipher = AES.new(decryption_key, AES.MODE_GCM, received_nonce)

        try:
            decrypted_data = cipher.decrypt_and_verify(received_ciphertext, received_tag)
        except ValueError as mac_mismatch:
            print "\nMAC validation failed during decryption. No authentication gurantees on this ciphertext"
            print "\nUnauthenticated AAD: " + str(received_aad)
        return decrypted_data
