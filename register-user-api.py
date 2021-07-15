import requests
import json

# https://docs.ejabberd.im/developer/ejabberd-api/admin-api/


data = {"user":"user3", "host":"debian.local", "password":"pass3"}
# json_s = json.dumps(data)
# print(json_s)
auth = ("admin@debian.local","admin")
r = requests.post('http://localhost:5443/api/register',json=data, auth=auth)
print(r.status_code)