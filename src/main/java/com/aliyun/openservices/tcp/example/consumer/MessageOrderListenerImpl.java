package com.aliyun.openservices.tcp.example.consumer;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;
import com.aliyun.openservices.tcp.example.mongodb.MongoDBConfig;
import com.aliyun.openservices.tcp.example.mongodb.MongoDbClient;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;

public class MessageOrderListenerImpl implements MessageOrderListener {

    private MongoCollection<Document> collection;

    public MessageOrderListenerImpl() {
        initMongoCollection();
    }

    private void initMongoCollection() {
        MongoClient mongoClient = MongoDbClient.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase(MongoDBConfig.DATABASE);
        collection = database.getCollection(MongoDBConfig.COLLECTION);
    }


    @Override
    public OrderAction consume(final Message message, final ConsumeOrderContext context) {
        try {
            String body = new String(message.getBody(), "UTF-8");
            if (body == null || body.isEmpty()) {
                return OrderAction.Success;
            }
            System.out.println("收到消息: " + body);
            Document doc = Document.parse(body);
            InsertOneResult insertOneResult = collection.insertOne(doc);
            System.out.println("插入数据库成功："+ insertOneResult.getInsertedId());
        } catch (MongoWriteException e) {
            System.out.println("Insertion failed due to write error");
            return OrderAction.Suspend; // 如果处理失败，则请求消息系统稍后重试
        } catch (MongoException e) {
            System.out.println("Insertion failed due to MongoDB error");
            return OrderAction.Suspend; // 如果处理失败，则请求消息系统稍后重试
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error processing message");
            return OrderAction.Suspend; // 如果处理失败，则请求消息系统稍后重试
        }
        return OrderAction.Success; // 消息处理成功
    }
}


