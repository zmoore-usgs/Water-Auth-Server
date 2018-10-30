#!/bin/bash

# Figure out the directory this script sits in
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

docker run --rm --network container:database -v $DIR:/liquibase/ --env-file $DIR/liquibase.env webdevops/liquibase:mysql update
