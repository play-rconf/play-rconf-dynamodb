# Play Remote Configuration - DynamoDB


[![Latest release](https://img.shields.io/badge/latest_release-18.12-orange.svg)](https://github.com/play-rconf/play-rconf-dynamodb/releases)
[![JitPack](https://jitpack.io/v/play-rconf/play-rconf-dynamodb.svg)](https://jitpack.io/#play-rconf/play-rconf-dynamodb)
[![Build](https://img.shields.io/travis-ci/play-rconf/play-rconf-dynamodb.svg?branch=master&style=flat)](https://travis-ci.org/play-rconf/play-rconf-dynamodb)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/play-rconf/play-rconf-dynamodb/master/LICENSE)

Retrieves configuration from AWS DynamoDB
*****

## About this project
In production, it is not always easy to manage the configuration files of a
Play Framework application, especially when it running on multiple servers.
The purpose of this project is to provide a simple way to use a remote
configuration with a Play Framework application.



## How to use

To enable this provider, just add the classpath `"io.playrconf.provider.DynamoDbProvider"`
and the following configuration:

```hocon
remote-configuration {

  ## Amazon Web Service DynamoDB
  # ~~~~~
  # Retrieves configuration from AWS DynamoDB
  dynamodb {

    # Key allowed to read DynamoDB
    access-key = ""
    host = ${?REMOTECONF_DYNAMODB_KEY}

    # Secret to use with ty key
    access-secret = ""
    port = ${?REMOTECONF_DYNAMODB_SECRET}

    # Name of the table where are located configuration keys
    table = ""
    table = ${?REMOTECONF_DYNAMODB_TABLE}

    # Name of the field containing the key
    field-key = "key"
    field-key = ${?REMOTECONF_DYNAMODB_FIELDKEY}

    # Name of the field containing the value
    field-value = "value"
    field-value = ${?REMOTECONF_DYNAMODB_FIELDVALUE}

    # Prefix. Get only values with key beginning
    # with the configured prefix
    prefix = ""
    prefix = ${?REMOTECONF_DYNAMODB_PREFIX}

    # Which pattern you have used for keys segmentation.
    # ie: "my.key.cfg", the separator is "." (dot).
    # ie: "my/key/cfg", the separator is "/" (slash).
    separator = "."
    separator = ${?REMOTECONF_DYNAMODB_SEPARATOR}
  }
}
```



## License
This project is released under terms of the [MIT license](https://raw.githubusercontent.com/play-rconf/play-rconf-dynamodb/master/LICENSE).
