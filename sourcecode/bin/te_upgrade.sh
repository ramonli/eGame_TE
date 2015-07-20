#!/bin/sh
# ----------------------------------------------------------------
# Upgrade TE automatically
# ----------------------------------------------------------------

TOMCAT_HOME=/home/winter/nigeria_pos/tomcat-nigeria-pos
# check whether the TOMCAT_HOME is valid
if [ -r $TOMCAT_HOME/webapps ]; then
    echo "Use TOMCAT_HOME: $TOMCAT_HOME"
else
    echo "TOMCAT_HOME($TOMCAT_HOME) is invalid"
    exit 0
fi

if [ $# != 1 ]; then
    me=`basename $0`
    echo Usage: ./$me [path to TE pakcage]
    echo Example: ./$me ./mlottery_te_SE_v1.5.3-rc3.zip
    exit 0
fi
te_dist=$1

# ----------------------------------------
# Prepare the TE package
# ----------------------------------------
tmp_dir="./TE_TMP"
if [ ! -d "$tmp_dir" ]; then
    mkdir $tmp_dir
else
    rm -rf $tmp_dir
fi
# unzip TE package first
unzip $te_dist -d $tmp_dir
# unzip mlottery_te.war then
unzip $tmp_dir/mlottery_te.war -d $tmp_dir/mlottery_te
# backup orginal configuration files
cp $tmp_dir/mlottery_te/WEB-INF/classes/log4j_production.xml $tmp_dir/mlottery_te/WEB-INF/classes/log4j_production.xml.bak
cp $tmp_dir/mlottery_te/WEB-INF/classes/jdbc.properties $tmp_dir/mlottery_te/WEB-INF/classes/jdbc.properties.bak
cp $tmp_dir/mlottery_te/WEB-INF/classes/mlottery_te.properties $tmp_dir/mlottery_te/WEB-INF/classes/mlottery_te.properties.bak
# use the configuration files from current deployment
cp -rf $TOMCAT_HOME/webapps/mlottery_te/WEB-INF/classes/mlottery_te.properties $tmp_dir/mlottery_te/WEB-INF/classes/mlottery_te.properties
cp -rf $TOMCAT_HOME/webapps/mlottery_te/WEB-INF/classes/jdbc.properties $tmp_dir/mlottery_te/WEB-INF/classes/jdbc.properties
cp -rf $TOMCAT_HOME/webapps/mlottery_te/WEB-INF/classes/log4j_production.xml $tmp_dir/mlottery_te/WEB-INF/classes/log4j_production.xml
echo "* Prepare TE realse successfully!"

# ----------------------------------------
# Backup TE app first
#----------------------------------------
# make a backup directory
backup_dir="./Backups"
if [ ! -d "$backup_dir" ]; then
	mkdir $backup_dir
fi
TIME_STAMP=`date '+%Y-%m-%d'`
cp -rf $TOMCAT_HOME/webapps/mlottery_te $backup_dir/mlottery_te.$TIME_STAMP
echo "* Backup TE to $backup_dir/mlottery_te.$TIME_STAMP successfully!"

# ----------------------------------------
# Upgrade TE
# ----------------------------------------
rm -rf $TOMCAT_HOME/webapps/mlottery_te
cp -rf $tmp_dir/mlottery_te $TOMCAT_HOME/webapps
echo "* Upgrade TE successfully!"
