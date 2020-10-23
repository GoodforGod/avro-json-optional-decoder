# Avro Json Optional Decoder

![Java CI](https://github.com/GoodforGod/avro-json-optional-decoder/workflows/Java%20CI/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_avro-json-optional-decoder&metric=alert_status)](https://sonarcloud.io/dashboard?id=GoodforGod_avro-json-optional-decoder)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_avro-json-optional-decoder&metric=coverage)](https://sonarcloud.io/dashboard?id=GoodforGod_avro-json-optional-decoder)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_avro-json-optional-decoder&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=GoodforGod_avro-json-optional-decoder)

Avro Decoder with support optional fields in JSON.
[Based on Celos fork.](https://github.com/Celos/avro-json-decoder)

## Dependency

**Gradle**
```groovy
dependencies {
    compile 'com.github.goodforgod:avro-json-optional-decoder:1.2.0'
}
```

**Maven**
```xml
<dependency>
    <groupId>com.github.goodforgod</groupId>
    <artifactId>avro-json-optional-decoder</artifactId>
    <version>1.2.0</version>
</dependency>
```

## Compatibility

Library is Java 1.8 compatible.

Library is compatible with different Apache Avro versions. Please use compatible library version for your Apache Avro version.

| [Apache Avro](https://mvnrepository.com/artifact/org.apache.avro/avro-compiler) Version | [Library](https://mvnrepository.com/artifact/com.github.goodforgod/avro-json-optional-decoder) Version |
| ---- | ---- |
| [1.10.0](https://mvnrepository.com/artifact/org.apache.avro/avro-compiler/1.10.0) | [1.2.0](https://mvnrepository.com/artifact/com.github.goodforgod/avro-json-optional-decoder/1.2.0) |
| [1.9.2](https://mvnrepository.com/artifact/org.apache.avro/avro-compiler/1.9.2) | [1.1.0](https://mvnrepository.com/artifact/com.github.goodforgod/avro-json-optional-decoder/1.1.0) |
| [1.8.2](https://mvnrepository.com/artifact/org.apache.avro/avro-compiler/1.8.2) | [1.0.0](https://mvnrepository.com/artifact/com.github.goodforgod/avro-json-optional-decoder/1.0.0) |


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

**JsonOptionalDecoder** provided by library allow correct JSON validation in both cases,
decoding JSON that doesn't specify optional values, provided they have defaults.

Check [guides](https://www.baeldung.com/java-apache-avro#2-deserialization) on how-to-use Avro Decoders.

Be aware JsonOptionalDecoder is not thread-safe.

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

**1.2.0** - Apache [Avro 1.10.0](https://mvnrepository.com/artifact/org.apache.avro/avro-compiler/1.9.2) support.

**1.1.0** - Apache [Avro 1.9.2](https://mvnrepository.com/artifact/org.apache.avro/avro-compiler/1.9.2) support, improved tests.

**1.0.0** - Initial version for Apache [Avro 1.8.2](https://mvnrepository.com/artifact/org.apache.avro/avro-compiler/1.8.2), support for Gradle 6.7, etc.

## License

This project licensed under the MIT - see the [LICENSE](LICENSE) file for details.
