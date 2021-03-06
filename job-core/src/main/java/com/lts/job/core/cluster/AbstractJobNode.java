package com.lts.job.core.cluster;

import com.lts.job.core.constant.Constants;
import com.lts.job.core.domain.JobNodeConfig;
import com.lts.job.core.listener.MasterNodeChangeListener;
import com.lts.job.core.listener.NodeChangeListener;
import com.lts.job.core.protocol.command.CommandWrapper;
import com.lts.job.core.registry.NodeRegistry;
import com.lts.job.core.support.Application;
import com.lts.job.core.listener.MasterNodeElectionListener;
import com.lts.job.core.util.GenericsUtils;
import com.lts.job.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 8/15/14.
 * 抽象节点
 */
public abstract class AbstractJobNode<T extends Node> implements JobNode {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JobNode.class);

    protected NodeRegistry registry;
    protected T node;
    protected JobNodeConfig config;
    protected Application application;
    protected NodeFactory nodeFactory;

    public AbstractJobNode() {
        application = new Application();
        config = new JobNodeConfig();
        config.setIdentity(StringUtils.generateUUID());
        config.setWorkThreads(Constants.AVAILABLE_PROCESSOR);
        config.setNodeGroup("lts");
        config.setZookeeperAddress("localhost:2181");
        config.setInvokeTimeoutMillis(1000 * 6);
        config.setListenPort(0);
        config.setJobInfoSavePath(Constants.USER_HOME + "/.job");
        config.setClusterName(Constants.DEFAULT_CLUSTER_NAME);
        // 可用的线程数
        application.setAttribute(Constants.KEY_AVAILABLE_THREADS, config.getWorkThreads());

        application.setConfig(config);
        application.setCommandWrapper(new CommandWrapper(application));
        application.setNodeManager(new NodeManager(application));
        nodeFactory = new NodeFactory(application);
        application.setMasterElector(new MasterElector(application));

        this.registry = new NodeRegistry(application);
        // 用于master选举的监听器
        addNodeChangeListener(new MasterNodeElectionListener(application));
    }

    final public void start() {
        try {

            Class<T> nodeClass = GenericsUtils.getSuperClassGenericType(this.getClass());
            node = nodeFactory.create(nodeClass, config);
            config.setNodeType(node.getNodeType());

            LOGGER.info("当前节点配置:{}", config);

            nodeStart();

            registry.register(node);
            LOGGER.info("启动成功!");

        } catch (Throwable e) {
            LOGGER.error("启动失败!", e);
        }
    }

    final public void stop() {
        try {
            registry.unregister(node);
            nodeStop();
            LOGGER.info("停止成功!");
        } catch (Throwable e) {
            LOGGER.error("停止失败!", e);
        }
    }

    protected abstract void nodeStart();

    protected abstract void nodeStop();

    /**
     * 设置zookeeper注册中心地址
     * @param zookeeperAddress
     */
    public void setZookeeperAddress(String zookeeperAddress) {
        config.setZookeeperAddress(zookeeperAddress);
    }

    /**
     * 设置远程调用超时时间
     * @param invokeTimeoutMillis
     */
    public void setInvokeTimeoutMillis(int invokeTimeoutMillis) {
        config.setInvokeTimeoutMillis(invokeTimeoutMillis);
    }

    /**
     * 设置集群名字
     * @param clusterName
     */
    public void setClusterName(String clusterName) {
        config.setClusterName(clusterName);
    }

    /**
     * 添加节点监听器
     *
     * @param nodeChangeListener
     */
    public void addNodeChangeListener(NodeChangeListener nodeChangeListener) {
        registry.addNodeChangeListener(nodeChangeListener);
    }

    /**
     * 添加 master 节点变化监听器
     *
     * @param masterNodeChangeListener
     */
    public void addMasterNodeChangeListener(MasterNodeChangeListener masterNodeChangeListener) {
        application.getMasterElector().addMasterNodeChangeListener(masterNodeChangeListener);
    }
}
