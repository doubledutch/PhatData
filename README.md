# PhatData

A set of small tools that can be used to build large scale data analytics systems.

At its core, PhatData is a specification rather than an implementation. This open source project does include a full reference implementation, but the intent is that each of the PhatData services can easily be implemented by anyone in their own prefered language. The power in PhatData lies in the way these simple services can be combined.

## 1. Specification

### 1.1 Streams

The stream service is the foundation all other PhatData services are build on. It is used to store data and configurations in a simple predictable manner.

A stream is a list JSON documents with a very simple HTTP based interface to POST and GET documents that adheres to the following rules.

 * Every document stored in a stream is immutable - once added, it can not be changed.
 * As documents are added they are each assigned a location in the stream as an immutable continuoesly incrementing 64 bit integer. That is, the first document is given the location 0, the second document 1 and so forth.
 * A stream can be truncated. That is all documents after a specific location can be removed. After a truncation, new objects added must reuse the same locations - ensuring that the stream still has a continuoesly incrementing set of locations with no gaps.

A stream service can host multiple streams, each with a unique topic. The HTTP based interface for a stream service is as follows.

#### 1.1.1 List Topics

````
GET /stream/

[{"topic":"metrics","size":123833247,"count":861062},
 {"topic":"log_data","size":30986110875,"count":1002901386}]
````

#### 1.1.2 Get Metadata for a Single Topic

````
GET /stream/:topic

[{"topic":"log_data","size":30986110875,"count":1002901386}]
````

#### 1.1.3 Add a Single Document

````
POST /stream/:topic

{"location":1002901386}
````

#### 1.1.3 Add Multiple Documents

````
POST /stream/:topic

{"location_list":[1002901387,1002901388,1002901389,1002901390]}
````

#### 1.1.4 Get a Document

````
GET /stream/:topic/:location

{"foo":"bar"}
````

#### 1.1.5 Get a Range of Documents

````
GET /stream/:topic/:start_location-:end_location

[{"foo":"bar"},{"foo":"baz"}]
````

#### 1.1.5 Get All Documents After a Location

````
GET /stream/:topic/:start_location-

[{"foo":"bar"},{"foo":"baz"}]
````

#### 1.1.6 Truncate a Stream

````
DELETE /stream/:topic/:start_location
````

## 2. Reference Implementation

## 3. Command line tool set