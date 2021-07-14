# xmpp-smack-chat-application  
# Usage:  
install ejabberd  
copy my ejabberd config file to /opt/ejabberd/conf/ejabberd.yml  
**start server:**    
sudo runuser ejabberd ./ejabberdctl live  
**create users**  
sudo runuser ejabberd ./ejabberdctl register user1 debian.local pass  
sudo runuser ejabberd ./ejabberdctl register user2 debian.local pass2  
**check if has worked**  
sudo runuser ejabberd ./ejabberdctl registered_users debian.local  
  

**run chat client like with following cli args:**  
username password otheruser@debian.local 127.0.0.1 5222  
i.E.:  
user2 pass2 debian.local 127.0.0.1 5222
