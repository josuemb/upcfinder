# upcfinder

A Groovy/Java library and command-line tool that looks up product information for a
UPC (Universal Product Code, also known as a bar code) using the
[UPCitemdb](https://www.upcitemdb.com/) REST API. Given a UPC, it returns a map of
product details such as title, brand, description, category, and recorded prices.

For background on the UPC standard, see the
[Universal Product Code](http://en.wikipedia.org/wiki/Universal_Product_Code)
article on Wikipedia.

## How it works

The library sends the UPC to the UPCitemdb trial REST endpoint
(`https://api.upcitemdb.com/prod/trial/lookup`), parses the JSON response, and
returns the first matching item as a `Map`. When no product is found the lookup
returns `null`. If the API rate-limits the request (HTTP `429` or a `TOO_FAST`
response code), the lookup automatically retries up to 3 times with a delay
between attempts.

## Requirements

- Java 21 or newer (JDK)
- Gradle wrapper (`./gradlew`) is included, so a system Gradle install is not required

The build uses Groovy 5.1.0 and JUnit 5 (JUnit Jupiter) for tests.

## Quick start

Build the executable jar with the Gradle wrapper, then run a lookup from the
command line:

```bash
./gradlew build
java -cp build/libs/upcfinder-0.0.2.jar com.josuemb.upcfinder.xmlrpc.UPCFinder 0049000006346
```

You can pass several UPC codes at once:

```bash
java -cp build/libs/upcfinder-0.0.2.jar com.josuemb.upcfinder.xmlrpc.UPCFinder 0049000006346 0012345678905
```

## Build

Build the project and produce the executable jar under `build/libs/`:

```bash
./gradlew build
```

The jar is built with a `Main-Class` and `Class-Path` manifest, so it can also be
launched directly:

```bash
java -jar build/libs/upcfinder-0.0.2.jar 0049000006346
```

Running the Gradle wrapper with no task (`./gradlew`) prints the available helper
tasks (`zip`, `gzip`, `bz2`, and the full task list). The jar file name carries the
project version, so it is `upcfinder-<version>.jar` under `build/libs/`.

The result is printed as `upc=<map of product information>`, for example:

```
0049000006346=[title:Coca-Cola Classic Soda Soft Drink, 12 fl oz, 12 pack, brand:Coca-Cola, ...]
```

## Usage as a library

Add `upcfinder-<version>.jar` (and its runtime dependencies) to your classpath, then
call the static `find` method.

Java:

```java
import com.josuemb.upcfinder.xmlrpc.UPCFinder;
import java.util.Map;

Map information = UPCFinder.find("0049000006346");
```

Groovy:

```groovy
import com.josuemb.upcfinder.xmlrpc.UPCFinder

def information = UPCFinder.find('0049000006346')
```

`find(String upc)` returns a `Map` of product information, or `null` when the UPC is
not found. An overload, `find(String[] upcs)`, looks up several codes and returns a
map keyed by UPC for the items that were found (or `null` when none are found).

The returned map includes fields such as `title`, `brand`, `description`, `upc`,
`ean`, `model`, `color`, `size`, `dimension`, `weight`, `category`, `currency`,
`lowest_price`, `highest_price`, and `images`.

## Running tests

```bash
./gradlew test
```

The tests exercise real lookups against the UPCitemdb trial API, so they require
network access and are subject to the trial tier rate limits described below.

## API note

This library uses the UPCitemdb **trial** REST API. The trial tier allows roughly
100 requests per day and does not require an API key. Because it is rate limited,
heavy or production use should move to a paid UPCitemdb plan (which uses an API
key). See the
[UPCitemdb API documentation](https://www.upcitemdb.com/wp/docs/main/development/api/)
for current limits and plans.

## License

Licensed under the Apache License, Version 2.0. See
<http://www.apache.org/licenses/LICENSE-2.0> for the full text.
