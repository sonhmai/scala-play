# Simple API in Scala


## Appendix


### Running

```bash
sbt clean
sbt compile
sbt run
```

Server will start up on the HTTP port at <http://localhost:9000/>

### Endpoints

Using curl

```bash
# === PLACE ORDER ===

curl -X POST -H "Content-Type: application/json" \
  -d '{"memberId": "345", "totalAmount": 5000, "platformId": "web"}' \
  http://localhost:9000/v1/orders
# returning json of OrderId, OTP

# === VERIFY ORDER, CURRENTLY DOES NOT WORK ===
# if OTP is correct, order marked verified

curl -X POST -H "Content-Type: application/json" \
  -d '{"orderId": "424d2dd3-711b-40d7-979a-b7510c64f7de", "OTP": 234}' \
  http://localhost:9000/v1/orders/verify

# === VIEW ORDERS ===

# get all orders
curl http://localhost:9000/v1/orders 
 
# get all orders of a platformId
curl "http://localhost:9000/v1/orders?platformId=web"  

# get all orders of a platformId within a specific timeframe specified by unix timestamps
curl "http://localhost:9000/v1/orders?platformId=web&start=1605063093&end=1605063095"
```

### TODO

- use DB/ file instead of in-memory list in repository layer.
- implement OTP verification, current verify endpoint does not work.

