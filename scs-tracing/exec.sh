#!/bin/sh

function runProfile() {
    profile=$1
    nohup mvn spring-boot:run -P${profile} &
    #mvn spring-boot:run -P${profile} &
}

function run() {
    runProfile service-1
    sleep 10
    runProfile service-2
    sleep 10
    runProfile service-3
    sleep 10
    runProfile service-4
}

function killPort() {
    port=$1
    lsof -i:${port} | awk 'NR!=1{print $2}' | xargs kill -9
}

function stop() {
    ports=$1
    for port in ${ports[*]};do echo "kill port: ${port}";killPort ${port};done
}

svc=$1
cmd=$2
port=$3
svc_dir=""
svc_ports=(8101 8102 8103 8104)
if [ ${svc} == "sleuth" ]
then
    svc_dir="scs-sleuth"
elif [ ${svc} == "zipkin" ]
then
    svc_dir="scs-zipkin"
elif [ ${svc} == "skywalking" ]
then
    svc_dir="scs-skywalking"
else
    echo "unknown service \"${svc}\""
fi

if [ ${cmd} == "start" ]
then
    cd ${svc_dir};run
elif [ ${cmd} == "stop" ]
then
    cd ${svc_dir};stop "${svc_ports[*]}"
elif [ ${cmd} == "restart" ]
then
    cd ${svc_dir};stop "${svc_ports[*]}";run
elif [ ${cmd} == "restart1" ]
then
    cd ${svc_dir};killPort 9091;runProfile service-1
else
    echo "unknown command \"${cmd}\""
fi