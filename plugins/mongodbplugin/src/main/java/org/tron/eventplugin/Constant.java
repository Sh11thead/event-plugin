package org.tron.eventplugin;

public class Constant {
    public static final int BLOCK_TRIGGER = 0;
    public static final int TRANSACTION_TRIGGER = 1;
    public static final int CONTRACTLOG_TRIGGER = 2;
    public static final int CONTRACTEVENT_TRIGGER = 3;
    public static final int SOLIDITY_TRIGGER = 4;

    public static final int TRC20TRACKER_TRIGGER = 1000;
    public static final int TRC20TRACKER_SOLIDITY_TRIGGER = 1001;
    public static final int BLOCK_ERASE_TRIGGER = 1002;


    public static final String BLOCK_TRIGGER_NAME = "blockTrigger";
    public static final String TRANSACTION_TRIGGER_NAME = "transactionTrigger";
    public static final String CONTRACTLOG_TRIGGER_NAME = "contractLogTrigger";
    public static final String CONTRACTEVENT_TRIGGER_NAME = "contractEventTrigger";
    public static final String SOLIDITY_TRIGGER_NAME = "solidityTrigger";

    public static final String TRC20TRACKER_TRIGGER_NAME = "trc20TrackerTrigger";
    public static final String TRC20TRACKER_SOLIDITY_TRIGGER_NAME = "trc20SolidityTrackerTrigger";
    public static final String BLOCK_ERASE_TRIGGER_NAME = "blockErasedTrigger";

    private Constant(){}
}
