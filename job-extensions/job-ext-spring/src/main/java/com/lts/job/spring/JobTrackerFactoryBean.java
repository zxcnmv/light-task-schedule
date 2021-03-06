package com.lts.job.spring;

import com.lts.job.core.listener.MasterNodeChangeListener;
import com.lts.job.core.util.Assert;
import com.lts.job.core.util.StringUtils;
import com.lts.job.store.Config;
import com.lts.job.tracker.JobTracker;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * JobClient 的 FactoryBean
 * Created by Robert HG (254963746@qq.com) on 3/6/15.
 */
public class JobTrackerFactoryBean implements FactoryBean<JobTracker>, InitializingBean, DisposableBean {

    private JobTracker jobTracker;

    private volatile boolean started;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 监听端口
     */
    private Integer listenPort;
    /**
     * zookeeper地址
     */
    private String zookeeperAddress;
    /**
     * master节点变化监听器
     */
    private MasterNodeChangeListener[] masterNodeChangeListeners;
    /**
     * mongo数据库配置
     */
    private Config storeConfig;

    @Override
    public JobTracker getObject() throws Exception {
        return jobTracker;
    }

    @Override
    public Class<?> getObjectType() {
        return JobTracker.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() throws Exception {
        if (started) {
            jobTracker.stop();
            started = false;
        }
    }

    public void checkProperties() {
        Assert.hasText(zookeeperAddress, "zookeeperAddress必须设值!");
        Assert.notNull(storeConfig, "storeConfig不能为空!");
        if (listenPort != null && listenPort <= 0) {
            listenPort = null;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        checkProperties();

        jobTracker = new JobTracker();

        if (StringUtils.hasText(clusterName)) {
            jobTracker.setClusterName(clusterName);
        }
        jobTracker.setZookeeperAddress(zookeeperAddress);
        jobTracker.setStoreConfig(storeConfig);
        if (listenPort != null) {
            jobTracker.setListenPort(listenPort);
        }
        if (masterNodeChangeListeners != null) {
            for (MasterNodeChangeListener masterNodeChangeListener : masterNodeChangeListeners) {
                jobTracker.addMasterNodeChangeListener(masterNodeChangeListener);
            }
        }
    }

    public void start() {
        if (!started) {
            jobTracker.start();
            started = true;
        }
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setListenPort(Integer listenPort) {
        this.listenPort = listenPort;
    }

    public void setZookeeperAddress(String zookeeperAddress) {
        this.zookeeperAddress = zookeeperAddress;
    }

    public void setMasterNodeChangeListeners(MasterNodeChangeListener[] masterNodeChangeListeners) {
        this.masterNodeChangeListeners = masterNodeChangeListeners;
    }

    public void setStoreConfig(Config storeConfig) {
        this.storeConfig = storeConfig;
    }
}
