package com.aliyun.openservices.tcp.example.consumer;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;
import com.aliyun.openservices.tcp.example.mongodb.MongoDBConfig;
import com.aliyun.openservices.tcp.example.mongodb.MongoDbClient;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MessageOrderListenerImpl1 implements MessageOrderListener {
    // private static final Logger logger = LoggerFactory.getLogger(MessageOrderListenerImpl1.class);
    private MongoCollection<Document> collection;

    public MessageOrderListenerImpl1() {
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
                // logger.warn("Received empty message body.");
                return OrderAction.Success; // 或根据实际情况采取其他行动
            }
            System.out.println("Received message: " + body);
            // 使用日志框架代替直接使用System.out.println
            // 日志框架提供了更多功能，如日志级别控制、日志文件的自动切割、异步日志记录等，这些都是在生产环境中非常有用的特性。
            // 以SLF4J配合Logback为例
            // logger.info("message.getBody():{}", body);

            Document doc = Document.parse(body);
            collection.insertOne(doc);
            // 单个插入改成批量插入：
            // List<Document> documents = new ArrayList<>();
            // 添加文档到列表中
            // documents.add(doc);
            // try {
            //     // collection.insertOne(doc);
            //     // collection.insertMany(documents);
            //     InsertManyResult insertManyResult = collection.insertMany(documents);
            //     logger.info("Inserted {} documents", insertManyResult.getInsertedIds().size());
            // } catch (MongoWriteException e) {
            //     logger.error("Insertion failed due to write error: ", e);
            // } catch (MongoException e) {
            //     logger.error("Insertion failed due to MongoDB error: ", e);
            // }

        } catch (Exception e) {
            // System.err.println("Error processing message: " + e.getMessage());
            // e.printStackTrace();
            // 移除了 System.err.println 和 e.printStackTrace() 的调用，使用 logger.error 来记录错误，这样可以保持日志记录的一致性
            // logger.error("Error processing message: ", e);
            System.out.println("Error processing message");
            return OrderAction.Suspend; // 如果处理失败，则请求消息系统稍后重试
        }
        return OrderAction.Success; // 消息处理成功
    }
}


