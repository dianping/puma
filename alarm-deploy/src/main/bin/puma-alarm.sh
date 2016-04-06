#!/usr/bin/env bash
#该脚本为Linux下启动java程序的通用脚本。即可以作为开机自启动service脚本被调用，
#也可以作为启动java程序的独立脚本来使用。
#
#Author: tudaxia.com, Date: 2011/6/7
#
#警告!!!：该脚本stop部分使用系统kill命令来强制终止指定的java程序进程。
#在杀死进程前，未作任何条件检查。在某些情况下，如程序正在进行文件或数据库写操作，
#可能会造成数据丢失或数据不完整。如果必须要考虑到这类情况，则需要改写此脚本，
#增加在执行kill命令前的一系列检查。
#
#
###################################
#环境变量及程序执行参数
#需要根据实际环境以及Java程序名称来修改这些参数
###################################
#JDK所在路径
#JAVA_HOME=""

#执行程序启动所使用的系统用户，考虑到安全，推荐不使用root帐号
RUNNING_USER=nobody

#Java程序所在的目录（classes的上一级目录）
case "`uname`" in
    Linux)
		BIN_PATH=$(readlink -f $(dirname $0))
		;;
	*)
		BIN_PATH=`cd $(dirname $0); pwd`
		;;
esac

#APP路径
APP_PATH=`cd ${BIN_PATH}/..; pwd`

#外部依赖,Puma
PUMA_LIB_PATH=${APP_PATH}/lib/puma

#外部依赖,Dianping
DP_LIB_PATH=${APP_PATH}/lib/dianping

#外部依赖,其他
OTHERS_LIB_PATH=${APP_PATH}/lib/others

#配置文件
PROP_PATH=${APP_PATH}/puma-alarm.properties

#Main函数
APP_MAIN=com.dianping.puma.alarm.deploy.PumaAlarmServerLauncher

#LOG配置文件
LOG_REL_PATH=logs/log4j.xml

#标准输出重定向
STD_OUT="/data/applogs/puma-alarm-deploy/logs/std.out"

#拼凑完整的classpath参数，包括指定lib目录下所有的jar
for i in ${PUMA_LIB_PATH}/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done
for i in ${DP_LIB_PATH}/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done
for i in ${OTHERS_LIB_PATH}/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

#java虚拟机启动参数
JAVA_OPTS="-ms512m -mx512m -Xmn256m -Djava.awt.headless=true -XX:MaxPermSize=128m -Dlog4j.configuration=${LOG_REL_PATH}"

###################################
#(函数)判断程序是否已启动
#
#说明：
#使用JDK自带的JPS命令及grep命令组合，准确查找pid
#jps 加 l 参数，表示显示java的完整包路径
#使用awk，分割出pid ($1部分)，及Java程序名称($2部分)
###################################
#初始化psid变量（全局）
psid=0

checkpid() {
   javaps=`jps -l | grep ${APP_MAIN}`

   if [ -n "$javaps" ]; then
      psid=`echo $javaps | awk '{print $1}'`
   else
      psid=0
   fi
}

###################################
#(函数)启动程序
#
#说明：
#1. 首先调用checkpid函数，刷新$psid全局变量
#2. 如果程序已经启动（$psid不等于0），则提示程序已启动
#3. 如果程序没有被启动，则执行启动命令行
#4. 启动命令执行后，再次调用checkpid函数
#5. 如果步骤4的结果能够确认程序的pid,则打印[OK]，否则打印[Failed]
#注意：echo -n 表示打印字符后，不换行
#注意: "nohup 某命令 >/dev/null 2>&1 &" 的用法
###################################
start() {
   checkpid

   if [ $psid -ne 0 ]; then
      echo "================================"
      echo "warn: $APP_MAINCLASS already started! (pid=$psid)"
      echo "================================"
   else
      echo "Starting $APP_MAIN ..."
      exec java $JAVA_OPTS -classpath $CLASSPATH ${APP_MAIN} >${STD_OUT} 2>&1 &
      checkpid
      if [ $psid -ne 0 ]; then
         echo "(pid=$psid) [OK]"
         exit 0
      else
         echo "[Failed]"
         exit 1
      fi
   fi
}

###################################
#(函数)停止程序
#
#说明：
#1. 首先调用checkpid函数，刷新$psid全局变量
#2. 如果程序已经启动（$psid不等于0），则开始执行停止，否则，提示程序未运行
#3. 使用kill -9 pid命令进行强制杀死进程
#4. 执行kill命令行紧接其后，马上查看上一句命令的返回值: $?
#5. 如果步骤4的结果$?等于0,则打印[OK]，否则打印[Failed]
#6. 为了防止java程序被启动多次，这里增加反复检查进程，反复杀死的处理（递归调用stop）。
#注意：echo -n 表示打印字符后，不换行
#注意: 在shell编程中，"$?" 表示上一句命令或者一个函数的返回值
###################################
stop() {
   checkpid

   if [ $psid -ne 0 ]; then
      echo "Stopping $APP_MAIN ...(pid=$psid) "
      kill -15 $psid
      if [ $? -eq 0 ]; then
         echo "Stopping [OK]"
      else
         echo "Stopping [Failed]"
      fi

      echo "Waiting for $APP_MAIN to stop ..."
      sleep 3
      checkpid
      if [ $psid -ne 0 ]; then
         stop
      fi
   else
      echo "================================"
      echo "warn: $APP_MAIN is not running"
      echo "================================"
   fi

   exit 0
}

###################################
#(函数)检查程序运行状态
#
#说明：
#1. 首先调用checkpid函数，刷新$psid全局变量
#2. 如果程序已经启动（$psid不等于0），则提示正在运行并表示出pid
#3. 否则，提示程序未运行
###################################
status() {
   checkpid

   if [ $psid -ne 0 ];  then
      echo "$APP_MAIN is running! (pid=$psid)"
   else
      echo "$APP_MAIN is not running"
   fi
}

###################################
#(函数)打印系统环境参数
###################################
info() {
   echo "System Information:"
   echo "****************************"
   echo `head -n 1 /etc/issue`
   echo `uname -a`
   echo
   echo `java -version`
   echo
   echo "APP_PATH=$APP_PATH"
   echo "APP_MAIN=$APP_MAIN"
   echo "BIN_PATH=$BIN_PATH"
   echo "PUMA_LIB_PATH=$PUMA_LIB_PATH"
   echo "DP_LIB_PATH=$DP_LIB_PATH"
   echo "OTHERS_LIB_PATH=$OTHERS_LIB_PATH"
   echo "****************************"
}

###################################
#读取脚本的第一个参数($1)，进行判断
#参数取值范围：{start|stop|restart|status|info}
#如参数不在指定范围之内，则打印帮助信息
###################################
case "$1" in
   'start')
      start
      ;;
   'stop')
     stop
     ;;
   'restart')
     stop
     start
     ;;
   'status')
     status
     ;;
   'info')
     info
     ;;
  *)
esac
