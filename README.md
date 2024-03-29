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
    implementation 'com.github.goodforgod:avro-json-optional-decoder:1.1.6'
}
```

**Maven**
```xml
<dependency>
    <groupId>com.github.goodforgod</groupId>
    <artifactId>avro-json-optional-decoder</artifactId>
    <version>1.1.6</version>
</dependency>
```

## Compatibility

Library is Java 1.8 compatible.

Library is compatible with different Apache Avro versions. Please use compatible library version for your Apache Avro version.

| [Apache Avro](https://mvnrepository.com/artifact/org.apache.avro/avro-compiler) Version | [Library](https://mvnrepository.com/artifact/com.github.goodforgod/avro-json-optional-decoder) Version |
| ---- | ---- |
| [1.9.2](https://mvnrepository.com/artifact/org.apache.avro/avro-compiler/1.9.2) | [1.1.6](https://mvnrepository.com/artifact/com.github.goodforgod/avro-json-optional-decoder/1.1.4) |
| [1.8.2](https://mvnrepository.com/artifact/org.apache.avro/avro-compiler/1.8.2) | [1.0.1](https://mvnrepository.com/artifact/com.github.goodforgod/avro-json-optional-decoder/1.0.1) |


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
    "type" : [ "null", "string" ]
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

## Solution 

**JsonOptionalDecoder** provided by library allow correct JSON validation in both cases,
decoding JSON that doesn't specify optional values, provided they have defaults.

Check [guides](https://www.baeldung.com/java-apache-avro#2-deserialization) on how-to-use Avro Decoders.

Be aware JsonOptionalDecoder is not thread-safe.

### Optional Record Problem

For given AVRO Schema.
```json
{
  "name": "Test",
  "type": "record",
  "fields": [
    {
      "name": "required",
      "type": "string"
    },
    {
      "name": "inner",
      "type": [
        "null",
        {
          "name": "inner",
          "type": "record",
          "fields": [
            {
              "name": "req",
              "type": "string"
            }
          ]
        }
      ]
    }
  ]
}
```

This input will be correct:
```json
{"required":"u", "inner": {"req": "q"}}
```

As this input will be correct:
```json
{"required":"u"}
```

#### By Default

If property *default* is not specified, then missing field will be treated as [avro *null*](https://avro.apache.org/docs/1.9.2/spec.html#schema_primitive) value.

```json
{
    "name" : "name",
    "type" : [ "null", "string" ]
}
```

You can specify default value as per AVRO specification.

Keep in mind you *mind putting type corresponding to default value first*, due to AVRO incorrect union type validation.
```json
{
    "name" : "name",
    "type" : [ "string", "null" ],
    "default": "bob"
}
```

## How To Use

Change
```java
Decoder decoder = DecoderFactory.get().jsonDecoder(SCHEMA, INPUT_STREAM_OR_STRING);
```

With
```java
Decoder decoder = new JsonOptionalDecoder(SCHEMA, INPUT_STREAM_OR_STRING);
```

## License

This project licensed under the MIT - see the [LICENSE](LICENSE) file for details.
