#!/bin/bash
#useage: dump.sh <host> <username> <password> <file> <database> <table> <table> <table> ...
host=$1
username=$2
password=$3
file=$4
database=$5

mysqldump --host=127.0.0.1 --user=root --password=root --opt --add-drop-database=false --add-drop-table=false --default-character-set=utf8 --result-file=file_name <database_name> <table_name> <table_name>