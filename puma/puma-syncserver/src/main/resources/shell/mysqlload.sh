#!/bin/bash
##execute load
mysql $1 $2 $3 $4 $5 <<EOF
  source $6
EOF
