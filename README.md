# Avro Json Optional Decoder

![Java CI](https://github.com/GoodforGod/avro-json-optional-decoder/workflows/Java%20CI/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_avro-json-optional-decoder&metric=alert_status)](https://sonarcloud.io/dashboard?id=GoodforGod_avro-json-optional-decoder)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_avro-json-optional-decoder&metric=coverage)](https://sonarcloud.io/dashboard?id=GoodforGod_avro-json-optional-decoder)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_avro-json-optional-decoder&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=GoodforGod_avro-json-optional-decoder)

Avro Decoder with support optional fields in JSON. [Based on Celos fork.](https://github.com/Celos/avro-json-decoder)

## Dependency

Project is compatible with Apache Avro library [1.8.2](https://mvnrepository.com/artifact/org.apache.avro/avro/1.8.2)

**Gradle**
```groovy
dependencies {
    compile 'com.github.goodforgod:avro-json-optional-decoder:1.0.0'
}
```

**Maven**
```xml
<dependency>
    <groupId>com.github.goodforgod</groupId>
    <artifactId>avro-json-optional-decoder</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Optional Field Problem

For given AVRO Schema.
```json
{
  "type" : "record",
  "name" : "Person",
  "fields" : [ {
    "name" : "username",
    "type" : "string"
  }, {
    "name" : "name",
    "type" : [ "string", "null" ],
    "default": null
  } ]
}
```

Such JSON will be treated correctly.
```json
{"username":"user1","name":null}
```

However, such JSON (name field is missing).
```json
{"username":"user1"}
```

Will fail with:
```log
org.apache.avro.AvroTypeException: Expected field name not found: name
```

### Solution 

**JsonOptionalDecoder** provided by library allow correct JSON validation in both cases.

Decoder allows decoding JSON that doesn't specify optional values, provided they have defaults.

## How To Use

Change
```java
Decoder decoder = DecoderFactory.get().jsonDecoder(SCHEMA, INPUT_STREAM_OR_STRING);
```

With
```java
Decoder decoder = new JsonOptionalDecoder(SCHEMA, INPUT_STREAM_OR_STRING);
```

## Version History

**1.0.0** - Initial version for Apache Avro 1.8.2, support for Gradle 6.7, etc.

## License

This project licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
