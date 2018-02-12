#!/bin/sh
fly -t home sp -p shared-mysql-service-broker \
    -c `dirname $0`/pipeline.yml \
    -l `dirname $0`/credentials.yml