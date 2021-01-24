# demo-svc - Simple REST Api + CLI Application

## Design Decision

For the REST api server the state of the application is stored as Clojure atom.

See `people-db` atom in code. In real world application this would have been stored in
the database like PostgreSQL.

Thus the data will not be saved when the server is re-started.

The system is currently determine the type of input using the extension for API code.
e.g. It will only take in the file that have `*.csv`, `*.space`, and `*.piped` via `upload` end-point.


The system will be skipping invalid input and only logs the result to the console if such input line is found.

On the server the logs will be something like:

```
[main] INFO net.b12n.demo-svc.server - server running on port 3000
[qtp1205543136-21] INFO net.b12n.demo-svc.server - File saved to : /home/b12n/codes/demo-svc/uploads/data-with-invalid-lines.csv
[qtp1205543136-21] WARN net.b12n.demo-svc.server - Skipping this line as it is not valid input : Johnson,Josh,M,Blue,06/18/19XX
[qtp1205543136-21] WARN net.b12n.demo-svc.server - Reason: "06/18/19XX" - failed: valid-date? in: [4] at: [:date-of-birth] spec: :net.b12n.demo-svc.core/date-of-birth

[qtp1205543136-21] WARN net.b12n.demo-svc.server - Skipping this line as it is not valid input : Barry,Jane,X,Pink,07/18/1950
[qtp1205543136-21] WARN net.b12n.demo-svc.server - Reason: "X" - failed: valid-gender? in: [2] at: [:gender] spec: :net.b12n.demo-svc.core/gender
```

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
lein cloverage -p src -s test -n net\.b12n\.demo-svc\.*
```

Which should produce result similar to:

```
$./run-test-with-code-coverage
OpenJDK 64-Bit Server VM warning: forcing TieredStopAtLevel to full optimization because JVMCI is enabled
If there are a lot of uncached dependencies this might take a while ...
OpenJDK 64-Bit Server VM warning: forcing TieredStopAtLevel to full optimization because JVMCI is enabled
Loading namespaces:  (net.b12n.demo-svc.utils net.b12n.demo-svc.core net.b12n.demo-svc.server)
Test namespaces:  (net.b12n.demo-svc.core-test net.b12n.demo-svc.server-integration-test net.b12n.demo-svc.server-test net.b12n.demo-svc.utils-test)
Instrumented net.b12n.demo-svc.utils
Instrumented net.b12n.demo-svc.core
[main] INFO org.eclipse.jetty.util.log - Logging initialized @5254ms to org.eclipse.jetty.util.log.Slf4jLog
Instrumented net.b12n.demo-svc.server
Instrumented 3 namespaces in 2.7 seconds.

Testing net.b12n.demo-svc.core-test

Testing net.b12n.demo-svc.server-integration-test
[main] INFO net.b12n.demo-svc.server - File saved to : /home/b12n/codes/demo-svc/uploads/data.csv

Testing net.b12n.demo-svc.server-test

Testing net.b12n.demo-svc.utils-test

Ran 11 tests containing 42 assertions.
0 failures, 0 errors.
Ran tests.
Writing HTML report to: /home/b12n/codes/demo-svc/target/coverage/index.html

|--------------------------+---------+---------|
|                Namespace | % Forms | % Lines |
|--------------------------+---------+---------|
|   net.b12n.demo-svc.core |   52.86 |   58.00 |
| net.b12n.demo-svc.server |   59.12 |   77.59 |
|  net.b12n.demo-svc.utils |  100.00 |  100.00 |
|--------------------------+---------+---------|
|                ALL FILES |   60.58 |   73.53 |
|--------------------------+---------+---------|
```

The server is avilable for test via the Swagger at [http://localhost:3000/](http://localhost:3000/)

Swagger Schema is available at [http://localhost:3000/swagger.json](http://localhost:3000/swagger.json)

List of end-points:

| Request Type  | End Point           | Description                                                                                   |
|-------------- |-------------------- |---------------------------------------------------------------------------------------------  |
| GET           | /records/           | List all records in the system                                                                |
| POST          | /records/           | Add a single record to the system                                                             |
| DELETE        | /records/           | Delete all records in the system                                                              |
| GET           | /records/birthdate  | List all records sorted by date-of-birth (ascending)                                          |
| GET           | /records/firstname  | List all records sorted by first name (ascending)                                             |
| GET           | /records/gender     | List all records sorted by gender (female before male) and last name (ascending)              |
| GET           | /records/lastname   | List all records sorted by last name (descending)                                             |
| POST          | /records/upload     | Upload your own data to the system, currently support extension .csv, .piped, and .space      |


![swagger](https://github.com/burinc/demo-svc/blob/main/resources/swagger.png?raw=true)

### CLI usage

The CLI application will take input from the command line and print the result on the screen.

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

Example Output: using `./resources/data-with-invalid-lines.csv`

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
