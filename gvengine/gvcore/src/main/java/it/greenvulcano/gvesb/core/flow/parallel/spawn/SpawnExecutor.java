/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.core.flow.parallel.spawn;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.core.flow.parallel.Result;
import it.greenvulcano.gvesb.core.flow.parallel.SubFlowTask;
import it.greenvulcano.util.thread.BaseThreadFactory;

/**
 * 
 * @version 3.4.0 Jan 17, 2014
 * @author GreenVulcano Developer Team
 * 
 */
public class SpawnExecutor implements ShutdownEventListener
{
    /*
     * Check if a task is still active, then cancel the task
     */
    private class TaskCanceler extends TimerTask {
        private final Future<Result> future;
        private final String id;

        public TaskCanceler(Future<Result> future, String id) {
            this.future = future;
            this.id = id;
        }

        @Override
        public void run() {
        	if (!(this.future.isDone() || this.future.isCancelled())) {
        		logger.debug("Cancelling Spawned task: " + this.id);
        		this.future.cancel(true);
        	}
        }
    }

    private static final Logger  logger      = org.slf4j.LoggerFactory.getLogger(SpawnExecutor.class);

    private static SpawnExecutor instance    = null;
    private Timer           cancelerTimer    = new Timer("SpawnExecutor#TaskCanceler", true);
    private final int                  threadMax   = 10;

    /**
     * Executor of SubFlowTask instances.
     */
    private ThreadPoolExecutor executor = null;

    public static synchronized SpawnExecutor instance() {
        if (instance == null) {
            instance = new SpawnExecutor();
            ShutdownEventLauncher.addEventListener(instance);
        }
        return instance;
    }

    private SpawnExecutor() {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
        executor = new ThreadPoolExecutor(threadMax, threadMax, 2L, TimeUnit.MINUTES, queue, new BaseThreadFactory(
                "SpawnExecutor#NONE", true));
    }

    public void execute(String owner, Id ownerId, SubFlowTask task, long timeout)
            throws InterruptedException {
        if (owner == null)
            throw new NullPointerException("Invalid owner");
        if (ownerId == null)
            throw new NullPointerException("Invalid ownerId");
        if (task == null)
            throw new NullPointerException("Invalid task");

        submitTask(owner, ownerId, task, timeout);
    }

    public void execute(String owner, Id ownerId, List<SubFlowTask> tasks, long timeout)
            throws InterruptedException {
        if (owner == null)
            throw new NullPointerException("Invalid owner");
        if (ownerId == null)
            throw new NullPointerException("Invalid ownerId");
        if (tasks == null)
            throw new NullPointerException("Invalid tasks");
        if (tasks.size() == 0)
            throw new IllegalArgumentException("Empty tasks");

        for (SubFlowTask task : tasks) {
            submitTask(owner, ownerId, task, timeout);            
        }
    }

    public void cleanup(boolean forceTermination) {
        executor.getQueue().clear();
    }

    public void destroy() {
        executor.shutdownNow();
        executor = null;
        cancelerTimer.cancel();
        cancelerTimer = null;
    }

    @Override
    public void shutdownStarted(ShutdownEvent event) {
        logger.info("Shutown event received, stopping Spawned SubFlows");
        destroy();
    }

    /**
     * @param owner
     * @param ownerId
     * @param task
     * @param timeout
     */
    private void submitTask(String owner, Id ownerId, SubFlowTask task, long timeout) {
        task.setSpawned(true);
        task.setSpawnedName("SpawnExecutor#" + owner + "#" + ownerId.toString());

        Future<Result> future = executor.submit(task);
        this.cancelerTimer.schedule(new TaskCanceler(future, task.getSpawnedName()), timeout * 1000);
    }

}
