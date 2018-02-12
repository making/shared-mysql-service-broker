# shared-mysql-service-broker
Open Service Broker API for an existing shared MySQL

## Install the service broker on Cloud Foundry

```
wget http://central.maven.org/maven2/am/ik/servicebroker/shared-smysql-service-broker/0.0.1/shared-smysql-service-broker-0.0.1.jar -O target/shared-smysql-service-broker-0.0.1.jar
cf create-user-provided-service shared-mysql -p '{"url": "mysql://username:password@mysql.example.com:3306/shared_mysql_service_broker"}'
cf push
```


```
cf create-service-broker shared-mysql admin password https://shared-mysql-service-broker.<apps domain>
cf enable-service-access shared-mysql
```

## Install the service broekr on Kubernetes


```
./k8/install-service-catalog.sh
kubectl -f k8s/namespace.yml
cp k8s/secret.yml.old k8s/secret.yml
# Edit secret.yml for your environment
kubectl -f k8s
```
