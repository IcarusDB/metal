rest_host=$1
driver_id=$2

curl -X POST http://$rest_host:6066/v1/submissions/kill/$driver_id