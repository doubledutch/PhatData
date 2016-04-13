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

#### 1.1.5 Get the Latest Document

````
GET /stream/:topic/_

{"foo":"bar"}
````

#### 1.1.6 Get a Range of Documents

````
GET /stream/:topic/:start_location-:end_location

[{"foo":"bar"},{"foo":"baz"}]
````

#### 1.1.7 Get All Documents After a Location

````
GET /stream/:topic/:start_location-

[{"foo":"bar"},{"foo":"baz"}]
````

#### 1.1.8 Truncate a Stream

````
DELETE /stream/:topic/:start_location
````

### 1.2 Map Function

The map service reads documents from one stream, passes them to a map function and optionally writes the output to another stream. There is no specification for how this service should be build or function other than this.

Map functions are used to filter and transform data. A map function may output more than one object from each input object or none at all.

While the map service initially might sound like the simples, make sure to understand the specifics implementation you are using. Does it require the map function to be side effect free? ...or could you use it to trigger external events? What happens when a crash happens? ...is the service able to perfectly deduce which documents have been mapped, giving you exactly once delivery - or does it have an at most once or at least once guarantee? The reference implementation has setting for all of these and uses a third stream to store its state.

### 1.3 Reduce Function

The reduce service reads document from one stream, passes them to a reduce function along with an accumulator object and stores snapshots of the accumulator in another stream.

The specification of the reduce service is two fold - the storage of the snapshots and the HTTP based interface to get the latest reduce function.

The snapshots should be in the form of a JSON object with exactly two keys: The last location in the source stream that has been included in this snapshot and the actual accumulator for this snapshot.

````
{"location":9302903,
 "accumulator":{"foo":"bar"}}
````

The interface to retrieve an accumulater is simply a get request based on the name of the reduce function.

````
GET /reduce/:name

{"foo":"bar"}
````

The simplest implementation of web interface might simply get the latest object in the snapshot stream. A more advanced implementation can use a lambda architecture approach to get the latest snapshot and then retrieve all events since the snapshot and reduce them into the accumulator on the fly.

### 1.4 Partitioned Reduce Function



### 1.5 Key Value Store

### 1.6 Timeseries Index and Aggregate

## 2. Reference Implementation

## 3. Command line tool set