/**
 * Copyright (C) 2010-2016 Alibaba Group Holding Limited
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aliyun.openservices.tcp.example.consumer;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;
import com.aliyun.openservices.ons.api.order.OrderConsumer;
import com.aliyun.openservices.tcp.example.MqConfig;
import com.aliyun.openservices.tcp.example.mongodb.MongoDBConfig;
import com.aliyun.openservices.tcp.example.mongodb.MongoDbClient;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * MQ 接收消息示例 Demo
 */
public class SimpleOrderConsumer0 {

    public static void main(String[] args) {

        MongoClient mongoClient = MongoDbClient.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase(MongoDBConfig.DATABASE);
        MongoCollection<Document> collection= database.getCollection(MongoDBConfig.COLLECTION);

        Properties consumerProperties = new Properties();
        consumerProperties.setProperty(PropertyKeyConst.GROUP_ID, MqConfig.ORDER_GROUP_ID);
        consumerProperties.setProperty(PropertyKeyConst.AccessKey, MqConfig.ACCESS_KEY);
        consumerProperties.setProperty(PropertyKeyConst.SecretKey, MqConfig.SECRET_KEY);
        consumerProperties.setProperty(PropertyKeyConst.NAMESRV_ADDR, MqConfig.NAMESRV_ADDR);
        OrderConsumer consumer = ONSFactory.createOrderedConsumer(consumerProperties);
        consumer.subscribe(MqConfig.ORDER_TOPIC, MqConfig.TAG,  new MessageOrderListener() {

            @Override
            public OrderAction consume(final Message message, final ConsumeOrderContext context) {
            	try {
					String body = new String(message.getBody(),"UTF-8");
					System.out.println(body);
                    Document doc = Document.parse(body);
                    InsertOneResult insertOneResult = collection.insertOne(doc);
                    System.out.println("插入文档成功："+ insertOneResult.toString());
                    // 单个插入改成批量插入：
                    // List<Document> documents = new ArrayList<>();
                    // 添加文档到列表中
                    // documents.add(doc);
                    // InsertManyResult insertManyResult = collection.insertMany(documents);
                    // System.out.println("插入文档成功："+ insertManyResult);
                } catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (MongoWriteException e) {
                    // logger.error("Insertion failed due to write error: ", e);
                    System.out.println("Insertion failed due to write error");
                } catch (MongoException e) {
                    // logger.error("Insertion failed due to MongoDB error: ", e);
                    System.out.println("Insertion failed due to MongoDB error");
                }
                return OrderAction.Success;
            }
        });
        // consumer.subscribe(MqConfig.ORDER_TOPIC, MqConfig.TAG,  new MessageOrderListenerImpl());
        consumer.start();
        System.out.println("Consumer start success.");

        //等待固定时间防止进程退出
        try {
            Thread.sleep(200000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
