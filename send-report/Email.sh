Trigger=$1

# Find Root Directory
if [ -d "/home/circleci/" ]; then
  ROOT_PATH="/home/circleci/"
elif [ -d "/Users/distiller/" ]; then
  ROOT_PATH="/Users/distiller/"
else echo "Report log : NO ROOT DIRECTORY FOUND"
fi


data_file="${ROOT_PATH}project/Reports/MailData.txt"
DATE_FOLDER="$(TZ='America/Los_Angeles' date +%F)"

# Fetch Team Key from data_file
while IFS='=' read -r key value; do
    if [ "$key" == "Team" ]; then
        Team="$value"
        break
    fi
done < "$data_file"


if [ "$Trigger" == "" ]; then
  Trigger="Manual_Trigger"
fi


#File name declaration
REPORT_DIR="$DATE_FOLDER/$Team/$Trigger-$CIRCLE_BUILD_NUM"
GALEN_REPORT_DIR="$DATE_FOLDER/$Team/$Trigger-$CIRCLE_BUILD_NUM/Galen"

# Create directory and move files to S3 Bucket
mkdir -p /tmp/$REPORT_DIR


# Fetch Pass and Fail count from data_file
report_data() {
	local Browser="$1"
	Pass="null"
  Fail="null"
  Total="null"
	while IFS='=' read -r key value; do
    if [ "$key" == "${Browser}Passed" ]; then
        Pass="${value}"
        break
    fi
	done < "$data_file"
	while IFS='=' read -r key value; do
      if [ "$key" == "${Browser}Failed" ]; then
          Fail="${value}"
          break
      fi
	done < "$data_file"
	while IFS='=' read -r key value; do
      if [ "$key" == "${Browser}Total" ]; then
          Total="${value}"
          break
      fi
  	done < "$data_file"
}


# Fetch and Upload Reports based on each Browser ...
browser_report() {
	local Browser="$1"
	# Selection of browser links based on directory created ...
	if [ -f "${ROOT_PATH}project/Reports/"$Browser"Automation.html" ]; then
    cp -r ${ROOT_PATH}project/Reports/"$Browser"Automation.html /tmp/$REPORT_DIR
    # Upload to S3 Bucket
    AWS_ACCESS_KEY_ID=$AWS_ACCESS AWS_SECRET_ACCESS_KEY=$AWS_SECRET aws s3 cp /tmp/$REPORT_DIR $S3_URI$REPORT_DIR --recursive --acl public-read
    # Fetch Browser Execution Data
    report_data "$Browser"
    # Add content to the Report mail
    CONTENT="${CONTENT}<br> Please find Rently Team Regression <b>$Browser</b> <a href='$S3_BUCKET_URL$REPORT_DIR/${Browser}Automation.html'>report</a><br>
    <span style='color:green;font-weight:bold;'> Pass = "${Pass}"</span>,
    <span style='color:red;font-weight:bold;'> Failure = "${Fail}" </span> and
    <span style='font-weight:bold;'> Total = "${Total}" </span><br>"
  else echo "Report log : "$Browser" is Not Executed"
  fi
}


# Only Other than Report_Mailer Should get the Browser reports ...


if [ "${CIRCLE_JOB}" != "Report_Mailer" ]; then

    # Reports call for All Browsers..
    browser_report "chrome"
    browser_report "firefox"
    browser_report "safari"
    browser_report "edge"

    # Galen Report ...
    if [ -f "${ROOT_PATH}project/Reports/galenReport/report.html" ]; then
      mkdir -p /tmp/$GALEN_REPORT_DIR   #Make dir for galen reports
      cp -r ${ROOT_PATH}project/Reports/galenReport/* /tmp/$GALEN_REPORT_DIR
      AWS_ACCESS_KEY_ID=$AWS_ACCESS AWS_SECRET_ACCESS_KEY=$AWS_SECRET aws s3 cp /tmp/$GALEN_REPORT_DIR/ $S3_URI$GALEN_REPORT_DIR/  --recursive --acl public-read
      CONTENT="${CONTENT}<br> Please find Rently Team Regression <b>Galen report</b> here in <a href='$S3_BUCKET_URL$GALEN_REPORT_DIR/report.html'>Report</a>"
    else echo "Report log : Galen is Not Executed"
    fi

    # Axe Report ...
    if [ -f "${ROOT_PATH}project/target/java-a11y/htmlcs/html/index.html" ]; then
      cp -r ${ROOT_PATH}project/target/java-a11y/* /tmp/$REPORT_DIR
      AWS_ACCESS_KEY_ID=$AWS_ACCESS AWS_SECRET_ACCESS_KEY=$AWS_SECRET aws s3 cp /tmp/$REPORT_DIR $S3_URI$REPORT_DIR --recursive --acl public-read
      CONTENT="${CONTENT}<br> Please find Rently Team Regression <b>Axe report</b> here in <a href='$S3_BUCKET_URL$REPORT_DIR/htmlcs/html/index.html'>Report</a>"
    else echo "Report log : Axe is Not Executed"
    fi


    # Lighthouse Report ...
    if [ -f "${ROOT_PATH}project/Reports/LighthouseSample.html" ]; then
      cp ${ROOT_PATH}project/Reports/LighthouseSample.html /tmp/$REPORT_DIR
      AWS_ACCESS_KEY_ID=$AWS_ACCESS AWS_SECRET_ACCESS_KEY=$AWS_SECRET aws s3 cp /tmp/$REPORT_DIR $S3_URI$REPORT_DIR --recursive --acl public-read
      CONTENT="${CONTENT}<br> Please find Rently Team Regression <b>LightHouse report</b> here in <a href='$S3_BUCKET_URL$REPORT_DIR'>Report</a>"
    else echo "Report log : Lighthouse is Not Executed"
    fi
else echo "Report log : Parallel Mailer Job - Browser Check is disabled ..."
fi



# Moving to the Workspace ...
if [[ "$CIRCLE_JOB" == "ParallelTest_"* ]]; then
  echo "Report log : Parallel Mailer Job Uploading ..."
  if [ "${CONTENT}" == "" ]; then
      CONTENT="No Execution reports found."
  fi
  echo "<br><b>$CIRCLE_JOB :<br> </b>${CONTENT}<br>" > ${ROOT_PATH}project/Reports/Mailer/parallel_email-$CIRCLE_JOB.text
# Triggering the mail ...
else
  if [ "${CIRCLE_JOB}" == "Report_Mailer" ]; then
    echo "Consolidate Mailer Job Running ..."
    BASE_DIR="/home/circleci/Mail/"
    mkdir -p $BASE_DIR
    cp -r /tmp/home/* $BASE_DIR
    NUM=1

    # To Verify the filename by iteration and select by contains of specified name
    find "$BASE_DIR" -type f -name "*email-ParallelTest_*.text" | sort -t '_' -k 2 -n | while read -r FILE_PATH; do
      echo "Content of matching file : $FILE_PATH"
      cat "$FILE_PATH" >> "/tmp/Content.text"
      if [ $NUM -eq 15 ]; then
        break
      fi
      NUM=$(expr $NUM + 1)
    done
    CONTENT+="$(cat "/tmp/Content.text")"
  fi

  if [ "${CONTENT}" == "" ]; then
    CONTENT="No Execution reports found."
  fi

  echo "<p>Hi Team,<br>${CONTENT}<br><br> Regards <br> -DevSecOps " > /tmp/email.text
  sudo python3 $(pwd)/send-report/send-email.py /tmp/email.text "Regression Test Report - $Trigger" $SMTP_USER_SELENIUM $SMTP_PASSWORD_SELENIUM $SMTP_SERVER_SELENIUM $SMTP_PORT_SELENIUM 0

fi
