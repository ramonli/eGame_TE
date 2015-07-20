#--------------------------------------------------#
# GLOBAL SETTINGS                                  #
#--------------------------------------------------#
# the server host, port and application context
#CONF_host="192.168.2.116"
#CONF_port=8080
#CONF_context="mlotter_te"
#CONF_url="http://" + CONF_host + ":" + str(CONF_port) + "/" + CONF_context
CONF_url="http://localhost:8081/mlottery_te/transaction_engine/"

# working key, it is identical with test database script.
CONF_des_iv="0000000000000000"
CONF_data_key="W0JAMWQ1MGZkMjc2N2U2M2Y2LWVkYTIt"
CONF_mac_key="P2Bbo6+bSSR9O2Qc89f3b4oHTyE1V2gF"

# Http headers
header_protocal_version="X-Protocal-Version"
header_trace_message_id="X-Trace-Message-Id"
header_timestamp="X-Timestamp"
header_transaction_type="X-Transaction-Type"
header_gpe_id="X-GPE-Id"
header_device_id="X-Terminal-Id"
header_operator_id="X-Operator-Id"
header_batch_no="X-Trans-BatchNumber"
header_mac="X-MAC"
header_response_code="X-Response-Code"
header_content_type="Content-Type"
header_game_type_id="X-Game-Type-Id"

