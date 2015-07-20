#!/bin/sh

# Modify below variables to real value
APP_NAME=mlottery_te
APP_DIR=/cygdrive/d/project/apache-tomcat-6.0.20/webapps/$APP_NAME

# check if the app_dir exist
if [ ! -d "$APP_DIR" ]; then
	echo "Can't find app_dir:$APP_DIR"
	exit 0
fi

# create backup directory
BACKUP_DATE=`date '+%Y%m%d'`
# find app version...'tr' will remove character
VERSION=`cat $APP_DIR/META-INF/MANIFEST.MF|grep 'Implementation-Version'|awk {'print $2'}|tr -d '[ ]|\n|\r'`
BACKUP_DIR=${APP_NAME}-${VERSION}-${BACKUP_DATE}
# check if the directory exist
if [ -d "$BACKUP_DIR" ]; then
	echo "The backup directory $BACKUP_DIR already exist!"
	exit 0
fi
mkdir $BACKUP_DIR
cd $BACKUP_DIR
mkdir WEB-INF
cd ..

cp -rf $APP_DIR/META-INF $BACKUP_DIR
cp -rf $APP_DIR/WEB-INF/classes $BACKUP_DIR/WEB-INF
cp -rf $APP_DIR/WEB-INF/lib $BACKUP_DIR/WEB-INF
cp -rf $APP_DIR/WEB-INF/web.xml $BACKUP_DIR/WEB-INF

#TODO: handle log files

echo "Backup $APP_NAME to $BACKUP_DIR successfully!"
