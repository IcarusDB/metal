rest_host=$1
driver_id=$2

curl -X GET http://$rest_host:6066/v1/submissions/status/$driver_id