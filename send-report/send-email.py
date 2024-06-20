# libraries to be imported 
import smtplib, sys, os
import platform
import pandas as pd
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText 
from email.mime.base import MIMEBase 
from email import encoders

current_os = platform.system()
# Get Team name from DataEngine ...
if "Darwin" in current_os:
    rootPath = "/Users/distiller/project/"
else:
    rootPath = "/home/circleci/project/"


# Get email id and attachment file path from command line arguments

email_body_file = sys.argv[1]
subject = sys.argv[2]
smtp_user = sys.argv[3]
smtp_pass = sys.argv[4]
smtp_host = sys.argv[5]
smtp_port = sys.argv[6]
attach = sys.argv[7]


# Fetch Data from MailData.txt File ...
getMailData = {}
def getFromMailFile():
    MailData=rootPath + "Reports/MailData.txt"
    with open(MailData, 'r') as file:
        for line in file:
            if '=' in line:
                key, value = map(str.strip, line.split('='))
                getMailData[key] = value

excel_file_path = rootPath +"src/main/resources/DataEngine/DataEngine.xlsx"

# Fetch Data from DataEngine File ...

try:
    df = pd.read_excel(excel_file_path, sheet_name='Team_Config')
except ValueError:
    df = pd.read_excel(excel_file_path, sheet_name='Team_Config', engine='openpyxl')

Team = str(df.iloc[0, -1])
toaddr = str(df.iloc[2, 1])
ccaddr = str(df.iloc[3, 1])

# with open(email_body_file, 'a') as file:
#     file.write(' - '+Team)


fromaddr = "Automation Report<devops-alerts@rently.com>"
toaddrs = [fromaddr]+[toaddr]+ccaddr.split(",")

# instance of MIMEMultipart 
msg = MIMEMultipart() 

msg['From'] = fromaddr
msg['To'] = toaddr
msg['Cc'] = ccaddr
msg['Subject'] = subject

# string to store the body of the mail 
with open(email_body_file, 'r') as myfile:
  body = myfile.read()

# attach the body with the msg instance 
msg.attach(MIMEText(body, 'html')) 

if int(attach) == 1:
	file_path = sys.argv[9]
	# open the file to be sent 
	filename = os.path.basename(file_path)
	attachment = open(file_path, "rb") 
	
	# instance of MIMEBase and named as p 
	p = MIMEBase('application', 'octet-stream') 
	
	# To change the payload into encoded form 
	p.set_payload((attachment).read()) 
	
	encoders.encode_base64(p) 
	p.add_header('Content-Disposition', "attachment; filename= %s" % filename) 
	
	# attach the instance 'p' to instance 'msg' 
	msg.attach(p) 

user = smtp_user
pwd = smtp_pass
s = smtplib.SMTP(smtp_host,smtp_port)
s.login(user, pwd)

# Converts the Multipart msg into a string 
text = msg.as_string() 

s.sendmail(fromaddr, toaddrs, text) 
s.quit() 
