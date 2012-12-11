#!/bin/bash
##execute load
mysql $1 $2 $3 $4 <<EOF
  source $5
EOF