from net.grinder.script.Grinder import grinder
from HTTPClient import NVPair
from te_conf import *
from java.util import UUID
import time
import random

from com.mpos.lottery.te.common.encrypt import HMacMd5Cipher
from com.mpos.lottery.te.common.encrypt import TriperDESCipher

"""
The message represents a http request/response message pack which contains all http headers 
and message body. In this message, all "X-' headers plusing message body should be performed 'Hash-MAC'
digest, and the  message body should be performed DES encryption.
"""
class Message:
    def __init__(self, protocal_version, timestamp, transaction_type, transaction_id, 
                 gpe_id, device_id, operator_id, batch_no, response_code, raw_body):
        self.protocal_version = protocal_version
        self.trace_message_id = self._trace_msg_id();
        self.timestamp = timestamp
        self.transaction_type = transaction_type
        self.transaction_id = transaction_id
        self.gpe_id = gpe_id
        self.device_id = device_id
        self.operator_id = operator_id
        self.batch_no = batch_no
        self.resopnse_code = response_code;
        self.raw_body = raw_body
        # assemble mac/des
        self.mac = self._mac()
        self.encrypted_body = self._des()
        # assemble nvpairs
#        self.nvpairs = self._assemble_nvpairs();
        #print("Finish initialising Message.")

    # If name this method as 'mac', a exception will be thrown out: call of non-function ('NoneType' object)
    # It is really strange...Is 'mac' the key word of python?? Anyway I rename 'mac' to 'do_mac', 
    # it is ok now. 
    def _mac(self):
        mac_input =  "Protocal-Version:" + self.protocal_version + "|"
        mac_input += "GPE_Id:" + self.gpe_id + "|"
        mac_input += "Terminal-Id:" + str(self.device_id) + "|"
        mac_input += "Operator-Id:" + self.operator_id + "|"
        mac_input += "Trans-BatchNumber:" + self.batch_no + "|"
        mac_input += "TractMsg-Id:" + self.trace_message_id + "|"
        mac_input += "Timestamp:" + self.timestamp + "|"
        mac_input += "Transaction-Type:" + str(self.transaction_type) + "|"
        if self.transaction_id == None:
            mac_input += "Transaction-Id:|"
        else:
            mac_input += "Transaction-Id:" + self.transaction_id + "|"
        
        if self.resopnse_code == None:
            mac_input += "Response-Code:|"
        else:
            mac_input += "Response-Code:" + str(self.resopnse_code) + "|"
        
        mac_input += self.raw_body
        
        # mac
        #print("Ready to MAC:" + mac_input) 
        return HMacMd5Cipher.doDigest(mac_input.strip(), CONF_mac_key)
        
    def _des(self):
        # encrypt raw_body by 3DES/CBC
        return TriperDESCipher.encrypt(CONF_data_key, self.raw_body, CONF_des_iv)
    
    def _trace_msg_id(self):
        uuid = UUID.randomUUID()
        uuid_str = uuid.toString()
        # print("generate a new uuid %s" % uuid_str)
        #return str(grinder.threadNumber) + "-" + uuid_str.replace('-','')
        #return str(time.time()).zfill(20)
        #return str(random.getrandbits(64)).zfill(20)
        return str(random.random()).zfill(20)
        
    def assemble_nvpairs(self):
        return [
                NVPair(header_protocal_version, self.protocal_version),
                NVPair(header_trace_message_id, self.trace_message_id),
                NVPair(header_timestamp, self.timestamp),
                NVPair(header_transaction_type, str(self.transaction_type)),
                NVPair(header_gpe_id, self.gpe_id),
                NVPair(header_device_id, str(self.device_id)),
                NVPair(header_operator_id, self.operator_id),
                NVPair(header_batch_no, self.batch_no),
                NVPair(header_mac, self.mac),
                NVPair(header_content_type, "text/xml"),
                NVPair(header_game_type_id, "1")
            ]
        
    
    

