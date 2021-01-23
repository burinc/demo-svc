# demo-svc - Simple REST api build

## Development/Build

Run server using the script `./run-server`

```shell
#!/usr/bin/env bash
## Run live server - required for integration test
set -eo pipefail
lein run -m net.b12n.demo-svc.server
```

Then run basic tests via `./run-test-with-code-coverage`

```shell
#!/usr/bin/env bash
set -eo pipefail
## Note: must start system first via $lein run
## as this will run regular test + integration tests which require live server to be up
lein cloverage
```

Which should produce result similar to:


```
./run-test-with-code-coverage
OpenJDK 64-Bit Server VM warning: forcing TieredStopAtLevel to full optimization because JVMCI is enabled
If there are a lot of uncached dependencies this might take a while ...
OpenJDK 64-Bit Server VM warning: forcing TieredStopAtLevel to full optimization because JVMCI is enabled
Loading namespaces:  (net.b12n.demo-svc.core net.b12n.demo-svc.server)
Test namespaces:  (net.b12n.demo-svc.core-test net.b12n.demo-svc.server-integration-test net.b12n.demo-svc.server-test)
Instrumented net.b12n.demo-svc.core
[main] INFO org.eclipse.jetty.util.log - Logging initialized @5120ms to org.eclipse.jetty.util.log.Slf4jLog
Instrumented net.b12n.demo-svc.server
Instrumented 2 namespaces in 2.6 seconds.

Testing net.b12n.demo-svc.core-test

Testing net.b12n.demo-svc.server-integration-test
[main] INFO net.b12n.demo-svc.server - File saved to : /home/b12n/codes/demo-svc/uploads/data.csv

Testing net.b12n.demo-svc.server-test

Ran 7 tests containing 37 assertions.
0 failures, 0 errors.
Ran tests.
Writing HTML report to: /home/b12n/codes/demo-svc/target/coverage/index.html

|--------------------------+---------+---------|
|                Namespace | % Forms | % Lines |
|--------------------------+---------+---------|
|   net.b12n.demo-svc.core |   99.41 |  100.00 |
| net.b12n.demo-svc.server |   63.55 |   80.20 |
|--------------------------+---------+---------|
|                ALL FILES |   69.76 |   83.33 |
|--------------------------+---------+---------|
```

The server is avilable for test via the Swagger at [http://localhost:3000/](http://localhost:3000/)

Swagger Schema is available at [http://localhost:3000/swagger.json](http://localhost:3000/swagger.json)

List of end-points:

| Request Type	| End Point						| Description																																										|
|--------------	|--------------------	|---------------------------------------------------------------------------------------------	|
| GET						| /records/						| List all records in the system																																|
| POST					| /records/						| Add a single record to the system																															|
| DELETE				| /records/						| Delete all records in the system																															|
| GET						| /records/birthdate	| List all records sorted by date-of-birth (ascending)																					|
| GET						| /records/firstname	| List all records sorted by first name (ascending)																							|
| GET						| /records/gender			| List all records sorted by gender (female before male) and last name (ascending)							|
| GET						| /records/lastname		| List all records sorted by last name (descending)																							|
| POST					| /records/upload			| Upload your own data to the system, currently support extension .csv, .piped, and .space			|


![swagger](https://github.com/burinc/demo-svc/blob/master/resources/swagger.png?raw=true)

