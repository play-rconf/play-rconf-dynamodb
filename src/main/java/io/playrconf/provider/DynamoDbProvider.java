/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 The Play Remote Configuration Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.playrconf.provider;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import io.playrconf.sdk.AbstractProvider;
import io.playrconf.sdk.FileCfgObject;
import io.playrconf.sdk.KeyValueCfgObject;
import io.playrconf.sdk.exception.ProviderException;
import io.playrconf.sdk.exception.RemoteConfException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

/**
 * Retrieves configuration from AWS DynamoDB.
 *
 * @author Thibault Meyer
 * @since 18.04.05
 */
public class DynamoDbProvider extends AbstractProvider {

    /**
     * Contains the provider version.
     */
    private static String providerVersion;

    @Override
    public String getName() {
        return "DynamoDB";
    }

    @Override
    public String getVersion() {
        if (DynamoDbProvider.providerVersion == null) {
            synchronized (DynamoDbProvider.class) {
                final Properties properties = new Properties();
                final InputStream is = DynamoDbProvider.class.getClassLoader()
                    .getResourceAsStream("playrconf-dynamodb.properties");
                try {
                    properties.load(is);
                    DynamoDbProvider.providerVersion = properties.getProperty("playrconf.dynamodb.version", "unknown");
                    properties.clear();
                    is.close();
                } catch (final IOException ignore) {
                }
            }
        }
        return DynamoDbProvider.providerVersion;
    }

    @Override
    public String getConfigurationObjectName() {
        return "dynamodb";
    }

    @Override
    public void loadData(final Config config,
                         final Consumer<KeyValueCfgObject> kvObjConsumer,
                         final Consumer<FileCfgObject> fileObjConsumer) throws ConfigException, RemoteConfException {
        final String accessKey = config.getString("access-key");
        final String accessSecret = config.getString("access-secret");
        final String region = config.getString("region");
        final String tableName = config.getString("table");
        final String fieldKey = config.getString("field-key");
        final String fieldValue = config.getString("field-value");
        final String prefix = config.hasPath("prefix") ? config.getString("prefix") : "";
        final String separator = config.hasPath("separator") ? config.getString("separator") : ".";

        // Check values
        if (accessKey == null || accessKey.isEmpty()) {
            throw new ConfigException.BadValue(config.origin(), "access-key", "Required");
        } else if (accessSecret == null || accessSecret.isEmpty()) {
            throw new ConfigException.BadValue(config.origin(), "access-secret", "Required");
        } else if (region == null || region.isEmpty()) {
            throw new ConfigException.BadValue(config.origin(), "region", "Required");
        } else if (fieldKey == null || fieldKey.isEmpty()) {
            throw new ConfigException.BadValue(config.origin(), "field-key", "Required");
        } else if (fieldValue == null || fieldValue.isEmpty()) {
            throw new ConfigException.BadValue(config.origin(), "field-value", "Required");
        } else if (separator == null || separator.isEmpty()) {
            throw new ConfigException.BadValue(config.origin(), "separator", "Required");
        }

        // Instantiate Amazon Web Service client
        final AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, accessSecret);
        final AmazonDynamoDB amazonDynamoDBClient = AmazonDynamoDBClientBuilder.standard()
            .withRegion(region)
            //.withEndpointConfiguration(
            //    new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2")
            //)
            .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
            .build();

        try {
            // Retrieve items from DynamoDB
            final List<Map<String, AttributeValue>> items = amazonDynamoDBClient
                .scan(tableName, Arrays.asList(fieldKey, fieldValue))
                .getItems();

            // Retrieve all available objects as "Key -> Value"
            Tuple2<String, Object> currentTuple = new Tuple2<>();
            for (final Map<String, AttributeValue> item : items) {
                for (final Map.Entry<String, AttributeValue> data : item.entrySet()) {
                    if (data.getKey().compareTo(fieldKey) == 0) {
                        String cfgKey = data
                            .getValue()
                            .getS()
                            .replaceFirst(prefix, "")
                            .replace(separator, ".");
                        if (!cfgKey.matches("[0-9a-zA-Z_](.*)")) {
                            cfgKey = cfgKey.substring(1);
                        }
                        currentTuple.setLeft(cfgKey);
                    } else if (data.getKey().compareTo(fieldValue) == 0) {
                        switch (data.getValue().toString().substring(1, 2)) {
                            case "B": {
                                currentTuple.setRight(data.getValue().getBOOL());
                                break;
                            }
                            case "N": {
                                currentTuple.setRight(data.getValue().getN());
                                break;
                            }
                            case "L": {
                                final List<Object> sub = new ArrayList<>();
                                data.getValue().getL().forEach(subItem -> {
                                    switch (subItem.toString().substring(1, 2)) {
                                        case "B": {
                                            sub.add(subItem.getBOOL());
                                            break;
                                        }
                                        case "N": {
                                            sub.add(subItem.getN());
                                            break;
                                        }
                                        case "S": {
                                            sub.add(subItem.getS());
                                            break;
                                        }
                                        default: {
                                        }
                                    }
                                });
                                currentTuple.setRight(sub);
                                break;
                            }
                            case "S": {
                                currentTuple.setRight(data.getValue().getS());
                                break;
                            }
                            default: {
                            }
                        }
                    }

                    // Tuple is complete! Try to consume object
                    if (currentTuple.getLeft() != null && currentTuple.getRight() != null) {
                        if (currentTuple.getRight() != null & isFile(currentTuple.getRight().toString())) {
                            fileObjConsumer.accept(new FileCfgObject(
                                currentTuple.getLeft(),
                                currentTuple.getRight().toString()
                            ));
                        } else {
                            kvObjConsumer.accept(new KeyValueCfgObject(
                                currentTuple.getLeft(),
                                currentTuple.getRight() == null ? null : currentTuple.getRight().toString()
                            ));
                        }

                        // Reset tuple
                        currentTuple = new Tuple2<>();
                    }
                }
            }
        } catch (final SdkClientException ex) {
            throw new ProviderException("Can't connect to the provider: " + ex.getMessage());
        } finally {

            // Close client
            amazonDynamoDBClient.shutdown();
        }
    }
}
