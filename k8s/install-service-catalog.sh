#!/bin/sh

helm repo add svc-cat https://svc-catalog-charts.storage.googleapis.com
helm install svc-cat/catalog \
	--name catalog \
	--namespace catalog \
	--set insecure=true