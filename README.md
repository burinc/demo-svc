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

|--------------------------|---------|---------|
|                Namespace | % Forms | % Lines |
|--------------------------|---------|---------|
|   net.b12n.demo-svc.core |   99.41 |  100.00 |
| net.b12n.demo-svc.server |   63.55 |   80.20 |
|--------------------------|---------|---------|
|                ALL FILES |   69.76 |   83.33 |
|--------------------------|---------|---------|
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


![swagger](https://github.com/burinc/demo-svc/blob/main/resources/swagger.png?raw=true)

### CLI usage

For loading data via the CLI you can run the following command

```sh
# To see the basic usage
lein run -m net.b12n.demo-svc.core --help
```

This should give you the following output

```
srk - Simple Record Keeper library

Usage:
  srk [--input-file=<input-file> --file-type=<file-type> | --help ]

Options:
  -i, --input-file=<input-file>  Input file to use [default: data.csv]
  -t, --file-type=<file-type>    File type (one of csv, piped, and space) [default: csv]
  -h, --help                     Print this usage

  Example Usage:
  # a) Load input file of type 'csv' to the system
  srk -i ./resources/data.csv -t csv

  # b) Load input file of type 'piped' to the system
  srk -i ./resources/data.piped -t piped

  # c) Load input file of type 'space' to the system
  srk -i ./resoures/data.space -t space

  # c) Show help
  srk -h
```

Example Output: using `./resources/data-with-invalid-lines.csv` input

See also `run-cli` for more details

```
$./run-cli
OpenJDK 64-Bit Server VM warning: forcing TieredStopAtLevel to full optimization because JVMCI is enabled
If there are a lot of uncached dependencies this might take a while ...
OpenJDK 64-Bit Server VM warning: forcing TieredStopAtLevel to full optimization because JVMCI is enabled
[main] INFO net.b12n.demo-svc.core - Load data from resources/data-with-invalid-lines.csv of type :csv
[main] WARN net.b12n.demo-svc.core - Invalid line : `Johnson,Josh,M,Blue,06/18/19XX` due to `"06/18/19XX" - failed: valid-date? in: [4] at: [:date-of-birth] spec: :net.b12n.demo-svc.core/date-of-birth
`
[main] WARN net.b12n.demo-svc.core - Invalid line : `Barry,Jane,X,Pink,07/18/1950` due to `"X" - failed: valid-gender? in: [2] at: [:gender] spec: :net.b12n.demo-svc.core/gender
`
a) sorted by gender and then last name (ascending)
({:last-name "Barry",
  :first-name "Jane",
  :gender "F",
  :fav-color "Pink",
  :date-of-birth "07/18/1950"}
 {:last-name "Henry",
  :first-name "Jill",
  :gender "F",
  :fav-color "White",
  :date-of-birth "10/18/1980"}
 {:last-name "Johnson",
  :first-name "Josh",
  :gender "M",
  :fav-color "Blue",
  :date-of-birth "06/18/1990"}
 {:last-name "Smith",
  :first-name "John",
  :gender "M",
  :fav-color "Red",
  :date-of-birth "06/18/2000"})

b) sorted by last name (descending)
({:last-name "Smith",
  :first-name "John",
  :gender "M",
  :fav-color "Red",
  :date-of-birth "06/18/2000"}
 {:last-name "Johnson",
  :first-name "Josh",
  :gender "M",
  :fav-color "Blue",
  :date-of-birth "06/18/1990"}
 {:last-name "Henry",
  :first-name "Jill",
  :gender "F",
  :fav-color "White",
  :date-of-birth "10/18/1980"}
 {:last-name "Barry",
  :first-name "Jane",
  :gender "F",
  :fav-color "Pink",
  :date-of-birth "07/18/1950"})

c) sorted by first name (ascending)
({:last-name "Smith",
  :first-name "John",
  :gender "M",
  :fav-color "Red",
  :date-of-birth "06/18/2000"}
 {:last-name "Johnson",
  :first-name "Josh",
  :gender "M",
  :fav-color "Blue",
  :date-of-birth "06/18/1990"}
 {:last-name "Henry",
  :first-name "Jill",
  :gender "F",
  :fav-color "White",
  :date-of-birth "10/18/1980"}
 {:last-name "Barry",
  :first-name "Jane",
  :gender "F",
  :fav-color "Pink",
  :date-of-birth "07/18/1950"})

d) sorted by date of birth (ascending)
({:last-name "Barry",
  :first-name "Jane",
  :gender "F",
  :fav-color "Pink",
  :date-of-birth "07/18/1950"}
 {:last-name "Henry",
  :first-name "Jill",
  :gender "F",
  :fav-color "White",
  :date-of-birth "10/18/1980"}
 {:last-name "Johnson",
  :first-name "Josh",
  :gender "M",
  :fav-color "Blue",
  :date-of-birth "06/18/1990"}
 {:last-name "Smith",
  :first-name "John",
  :gender "M",
  :fav-color "Red",
  :date-of-birth "06/18/2000"})
```
