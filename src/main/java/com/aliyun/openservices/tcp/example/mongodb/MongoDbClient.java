package com.aliyun.openservices.tcp.example.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

// 单例模式实现
public class MongoDbClient {

    private static MongoClient mongoClient = null;

    // 私有构造函数防止外部实例化
    private MongoDbClient() {}

    // 获取MongoClient实例的方法
    public static MongoClient getMongoClient() {
        if (mongoClient == null) {
            synchronized (MongoDbClient.class) {
                // 双重检查锁定：是在实现单例模式时用来减少同步带来的开销的一种技术。其目的是在保证线程安全的同时提高性能。
                if (mongoClient == null) {
                    try {
                        String host = MongoDBConfig.HOST;
                        int port = MongoDBConfig.PORT;
                        mongoClient = MongoClients.create("mongodb://" + MongoDBConfig.HOST + ":" + MongoDBConfig.PORT);
                        // 添加JVM关闭钩子以关闭MongoClient
                        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                            System.out.println("Closing MongoClient.");
                            mongoClient.close();
                        }));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to initialize MongoClient.", e);
                    }
                }
            }
        }
        return mongoClient;
    }
}

/**
 * 第一次检查（外层if）：这一检查是为了避免在每次调用getMongoClient()方法时都需要进行同步。如果实例已经被创建（绝大多数情况下），就直接返回实例，避免了进入同步块，提高了方法的执行效率。
 *
 * 同步块（synchronized）：只有当实例尚未被创建时，才进入同步块。同步块确保在多线程环境中只有一个线程可以进入代码块创建实例。这是保证线程安全的关键步骤。
 *
 * 第二次检查（内层if）：这一检查是为了确保在当前线程等待进入同步块期间，没有其他线程已经创建了实例。如果不进行这一检查，就可能在实例已经被另一个线程创建后，当前线程再次创建实例，这样就违背了单例模式的原则。**/