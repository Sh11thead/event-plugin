package org.tron.eventplugin;
import com.alibaba.fastjson.JSONObject;
import org.pf4j.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.tron.mongodb.MongoConfig;
import org.tron.mongodb.MongoManager;
import org.tron.mongodb.MongoTemplate;

public class MongodbSenderImpl{
    private static MongodbSenderImpl instance = null;
    private static final Logger log = LoggerFactory.getLogger(MongodbSenderImpl.class);
    ExecutorService service = Executors.newFixedThreadPool(8);

    private boolean loaded = false;
    private BlockingQueue<Object> triggerQueue = new LinkedBlockingQueue();

    private String blockTopic = "";
    private String transactionTopic = "";
    private String contractEventTopic = "";
    private String contractLogTopic = "";
    private String solidityTopic = "";

    private String trc20TrackerTopic = "";
    private String trc20SolidityTrackerTopic = "";
    private String blockErasedTopic = "";
    private String shieldedTRC20TrackerTopic = "";
    private String shieldedSolidityTRC20TrackerTopic = "";


    private Thread triggerProcessThread;
    private boolean isRunTriggerProcessThread = true;

    private MongoManager mongoManager;
    private Map<String, MongoTemplate> mongoTemplateMap;

    private String dbName;
    private String dbUserName;
    private String dbPassword;

    private MongoConfig mongoConfig;

    public static MongodbSenderImpl getInstance(){
        if (Objects.isNull(instance)) {
            synchronized (MongodbSenderImpl.class){
                if (Objects.isNull(instance)){
                    instance = new MongodbSenderImpl();
                }
            }
        }

        return instance;
    }

    public void setDBConfig(String dbConfig){
        if (StringUtils.isNullOrEmpty(dbConfig)){
            return;
        }

        String[] params = dbConfig.split("\\|");
        if (params.length != 3){
            return;
        }

        dbName = params[0];
        dbUserName = params[1];
        dbPassword = params[2];

        loadMongoConfig();
    }

    public void setServerAddress(final String serverAddress){
        if (StringUtils.isNullOrEmpty(serverAddress)){
            return;
        }

        String[] params = serverAddress.split(":");
        if (params.length != 2){
            return;
        }

        String mongoHostName = "";
        int mongoPort = -1;

        try{
            mongoHostName = params[0];
            mongoPort = Integer.valueOf(params[1]);
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }

        if (Objects.isNull(mongoConfig)){
            mongoConfig = new MongoConfig();
        }

        mongoConfig.setHost(mongoHostName);
        mongoConfig.setPort(mongoPort);
    }

    public void init(){

        if (loaded){
            return;
        }

        if (Objects.isNull(mongoManager)){
            mongoManager = new MongoManager();
            mongoManager.initConfig(mongoConfig);
        }

        mongoTemplateMap = new HashMap<>();
        createCollections();

        triggerProcessThread = new Thread(triggerProcessLoop);
        triggerProcessThread.start();

        loaded = true;
    }

    private void createCollections(){
        mongoManager.createCollection(blockTopic);
        createMongoTemplate(blockTopic);

        mongoManager.createCollection(transactionTopic);
        createMongoTemplate(transactionTopic);

        mongoManager.createCollection(contractLogTopic);
        createMongoTemplate(contractLogTopic);

        mongoManager.createCollection(contractEventTopic);
        createMongoTemplate(contractEventTopic);

        mongoManager.createCollection(solidityTopic);
        createMongoTemplate(solidityTopic);

        mongoManager.createCollection(trc20TrackerTopic);
        createMongoTemplate(trc20TrackerTopic);

        mongoManager.createCollection(trc20SolidityTrackerTopic);
        createMongoTemplate(trc20SolidityTrackerTopic);

        mongoManager.createCollection(blockErasedTopic);
        createMongoTemplate(blockErasedTopic);

        mongoManager.createCollection(shieldedSolidityTRC20TrackerTopic);
        createMongoTemplate(shieldedSolidityTRC20TrackerTopic);

        mongoManager.createCollection(shieldedTRC20TrackerTopic);
        createMongoTemplate(shieldedTRC20TrackerTopic);
    }

    private void loadMongoConfig(){
        if (Objects.isNull(mongoConfig)){
            mongoConfig = new MongoConfig();
        }

        if (StringUtils.isNullOrEmpty(dbName)){
            return;
        }

        Properties properties = new Properties();

        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("mongodb.properties");
            if (Objects.isNull(input)){
                return;
            }
            properties.load(input);

            int connectionsPerHost = Integer.parseInt(properties.getProperty("mongo.connectionsPerHost"));
            int threadsAllowedToBlockForConnectionMultiplie = Integer.parseInt(
                    properties.getProperty("mongo.threadsAllowedToBlockForConnectionMultiplier"));

            mongoConfig.setDbName(dbName);
            mongoConfig.setUsername(dbUserName);
            mongoConfig.setPassword(dbPassword);
            mongoConfig.setConnectionsPerHost(connectionsPerHost);
            mongoConfig.setThreadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplie);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private MongoTemplate createMongoTemplate(final String collectionName){

        MongoTemplate template = mongoTemplateMap.get(collectionName);
        if (Objects.nonNull(template)){
            return template;
        }

        template = new MongoTemplate(mongoManager) {
            @Override
            protected String collectionName() {
                return collectionName;
            }

            @Override
            protected <T> Class<T> getReferencedClass() {
                return null;
            }
        };

        mongoTemplateMap.put(collectionName, template);

        return template;
    }


    public void setTopic(int triggerType, String topic){
        if (triggerType == Constant.BLOCK_TRIGGER){
            blockTopic = topic;
        }
        else if (triggerType == Constant.TRANSACTION_TRIGGER){
            transactionTopic = topic;
        }
        else if (triggerType == Constant.CONTRACTEVENT_TRIGGER){
            contractEventTopic = topic;
        }
        else if (triggerType == Constant.CONTRACTLOG_TRIGGER){
            contractLogTopic = topic;
        } else if (triggerType == Constant.SOLIDITY_TRIGGER) {
            solidityTopic = topic;
        }
        else if (triggerType == Constant.TRC20TRACKER_TRIGGER) {
            trc20TrackerTopic = topic;
        }
        else if (triggerType == Constant.TRC20TRACKER_SOLIDITY_TRIGGER) {
            trc20SolidityTrackerTopic = topic;
        }
        else if (triggerType == Constant.BLOCK_ERASE_TRIGGER) {
            blockErasedTopic = topic;
        }
        else if (triggerType == Constant.SHIELDED_TRC20SOLIDITYTRACKER_TRIGGER) {
            shieldedSolidityTRC20TrackerTopic = topic;
        }
        else if (triggerType == Constant.SHIELDED_TRC20TRACKER_TRIGGER) {
            shieldedTRC20TrackerTopic = topic;
        }
        else {
            return;
        }
    }

    public void close() {
    }

    public BlockingQueue<Object> getTriggerQueue(){
        return triggerQueue;
    }

    public void handleBlockEvent(Object data) {
        if (blockTopic == null || blockTopic.length() == 0){
            return;
        }
        MongoTemplate template = mongoTemplateMap.get(blockTopic);
        if (Objects.nonNull(template)) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    template.addEntity((String)data);
                }
            });
        }
    }

    public void handleTransactionTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(transactionTopic)){
            return;
        }

        MongoTemplate template = mongoTemplateMap.get(transactionTopic);
        if (Objects.nonNull(template)) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    template.addEntity((String)data);
                }
            });
        }
    }

    public void handleSolidityTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(solidityTopic)){
            return;
        }

        MongoTemplate template = mongoTemplateMap.get(solidityTopic);
        if (Objects.nonNull(template)) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    template.addEntity((String)data);
                }
            });
        }
    }

    public void handleContractLogTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(contractLogTopic)){
            return;
        }

        MongoTemplate template = mongoTemplateMap.get(contractLogTopic);
        if (Objects.nonNull(template)) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    template.addEntity((String)data);
                }
            });
        }
    }

    public void handleContractEventTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(contractEventTopic)){
            return;
        }

        MongoTemplate template = mongoTemplateMap.get(contractEventTopic);
        if (Objects.nonNull(template)) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    String dataStr = (String)data;
                    if (dataStr.contains("\"removed\":true")) {
                        try {
                            JSONObject jsStr = JSONObject.parseObject(dataStr);
                            String uniqueId = jsStr.getString("uniqueId");
                            if (uniqueId != null) {
                                template.delete("uniqueId", uniqueId);
                            }
                        } catch (Exception ex) {
                            log.error("unknown exception happened in parse object ", ex);
                        }
                    } else {
                        template.addEntity(dataStr);
                    }
                }
            });
        }
    }


    public void handleTrc20Trigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(trc20TrackerTopic)){
            return;
        }

        MongoTemplate template = mongoTemplateMap.get(trc20TrackerTopic);
        if (Objects.nonNull(template)) {
            try{
                template.addEntity((String)data);
            }catch (Exception e){
                log.error("handleTrc20Trigger in mongo error ", e);
                throw e;
            }
        }
    }


    public void handleTrc20SolidityTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(trc20TrackerTopic)){
            return;
        }

        //MongoTemplate template = mongoTemplateMap.get(trc20SolidityTrackerTopic);
        MongoTemplate template = mongoTemplateMap.get(trc20TrackerTopic);
        if (Objects.nonNull(template)) {
            try {
                String dataStr = (String)data;
                JSONObject jsStr = JSONObject.parseObject(dataStr);
                String blockHash = jsStr.getString("blockHash");
                if (StringUtils.isNotNullOrEmpty(blockHash)) {
                    template.update("solidity",new Boolean(true),"blockHash",blockHash);
                }
            } catch (Exception ex) {
                log.error("handleTrc20SolidityTrigger in mongo error ", ex);
                throw ex;
            }
        }
    }

    public void handleShieldedTrc20Trigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(shieldedTRC20TrackerTopic)){
            return;
        }

        MongoTemplate template = mongoTemplateMap.get(shieldedTRC20TrackerTopic);
        if (Objects.nonNull(template)) {
            try{
                template.addEntity((String)data);
            }catch (Exception e){
                log.error("handleShieldedTrc20Trigger in mongo error ", e);
                throw e;
            }
        }
    }


    public void handleShieldedTrc20SolidityTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(shieldedTRC20TrackerTopic)){
            return;
        }

        //MongoTemplate template = mongoTemplateMap.get(trc20SolidityTrackerTopic);
        MongoTemplate template = mongoTemplateMap.get(shieldedTRC20TrackerTopic);
        if (Objects.nonNull(template)) {
            try {
                String dataStr = (String)data;
                JSONObject jsStr = JSONObject.parseObject(dataStr);
                String blockHash = jsStr.getString("blockHash");
                if (StringUtils.isNotNullOrEmpty(blockHash)) {
                    template.update("solidity",new Boolean(true),"blockHash",blockHash);
                }
            } catch (Exception ex) {
                log.error("handleTrc20SolidityTrigger in mongo error ", ex);
                throw ex;
            }
        }
    }



    public void handleBlockEraseTrigger(Object data) {
        if (Objects.isNull(data) || Objects.isNull(blockErasedTopic)){
            return;
        }

        MongoTemplate template = mongoTemplateMap.get(blockErasedTopic);
        if (Objects.nonNull(template)) {
            try{
                template.addEntity((String)data);
            }catch (Exception e){
                log.error("handleBlockEraseTrigger in mongo error ", e);
                throw e;
            }
        }
    }


    private Runnable triggerProcessLoop =
            () -> {
                while (isRunTriggerProcessThread) {
                    try {
                        String triggerData = (String)triggerQueue.poll(1, TimeUnit.SECONDS);

                        if (Objects.isNull(triggerData)){
                            continue;
                        }

                        if (triggerData.contains(Constant.BLOCK_TRIGGER_NAME)){
                            handleBlockEvent(triggerData);
                        }
                        else if (triggerData.contains(Constant.TRANSACTION_TRIGGER_NAME)){
                            handleTransactionTrigger(triggerData);
                        }
                        else if (triggerData.contains(Constant.CONTRACTLOG_TRIGGER_NAME)){
                            handleContractLogTrigger(triggerData);
                        }
                        else if (triggerData.contains(Constant.CONTRACTEVENT_TRIGGER_NAME)){
                            handleContractEventTrigger(triggerData);
                        } else if (triggerData.contains(Constant.SOLIDITY_TRIGGER_NAME)) {
                            handleSolidityTrigger(triggerData);
                        }
                        else if (triggerData.contains(Constant.TRC20TRACKER_TRIGGER_NAME)) {
                            handleTrc20Trigger(triggerData);
                        }
                        else if (triggerData.contains(Constant.TRC20TRACKER_SOLIDITY_TRIGGER_NAME)) {
                            handleTrc20SolidityTrigger(triggerData);
                        }
                        else if (triggerData.contains(Constant.BLOCK_ERASE_TRIGGER_NAME)) {
                            handleBlockEraseTrigger(triggerData);
                        }
                        else if (triggerData.contains(Constant.SHIELDED_TRC20SOLIDITYTRACKER_TRIGGER_NAME)) {
                            handleShieldedTrc20SolidityTrigger(triggerData);
                        }else if (triggerData.contains(Constant.SHIELDED_TRC20TRACKER_TRIGGER_NAME)) {
                            handleShieldedTrc20Trigger(triggerData);
                        }

                    } catch (InterruptedException ex) {
                        log.info(ex.getMessage());
                        Thread.currentThread().interrupt();
                    } catch (Exception ex) {
                        log.error("unknown exception happened in process capsule loop", ex);
                    } catch (Throwable throwable) {
                        log.error("unknown throwable happened in process capsule loop", throwable);
                    }
                }
            };
}
