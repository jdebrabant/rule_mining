#!/bin/bash

sudo su 
echo 3 > /proc/sys/vm/drop_caches
exit
~/bin/pgsql/bin/pg_ctl restart
