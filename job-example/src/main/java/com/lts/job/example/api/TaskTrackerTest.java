package com.lts.job.example.api;

import com.lts.job.example.support.MasterNodeChangeListenerImpl;
import com.lts.job.example.support.TestJobRunner;
import com.lts.job.task.tracker.TaskTracker;

import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 */
public class TaskTrackerTest {

    public static void main(String[] args) {
        final TaskTracker taskTracker = new TaskTracker();
        taskTracker.setJobRunnerClass(TestJobRunner.class);

        taskTracker.setZookeeperAddress("localhost:2181");
        taskTracker.setNodeGroup("test_trade_TaskTracker");
//        taskTracker.setClusterName("lts");
        taskTracker.setWorkThreads(20);
//        taskTracker.setJobInfoSavePath(Constants.USER_HOME);
        taskTracker.addMasterNodeChangeListener(new MasterNodeChangeListenerImpl());

        taskTracker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                taskTracker.stop();
            }
        }));

    }
}