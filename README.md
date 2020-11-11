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
TODO

# === VERIFY ORDER ===
TODO

# === VIEW ORDERS ===

# get all orders
curl http://localhost:9000/v1/orders 
 
# get all orders of a platformId
curl "http://localhost:9000/v1/orders?platformId=web"  

# get all orders of a platformId within a specific timeframe specified by unix timestamps
curl "http://localhost:9000/v1/orders?platformId=web&start=1605063093&end=1605063095"
```


