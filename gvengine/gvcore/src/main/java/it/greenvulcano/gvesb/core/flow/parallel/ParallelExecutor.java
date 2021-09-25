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
package it.greenvulcano.gvesb.core.flow.parallel;

import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.util.thread.BaseThreadFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;

/**
 * 
 * @version 3.4.0 Jun 17, 2013
 * @author GreenVulcano Developer Team
 * 
 */
public class ParallelExecutor
{
    public enum TerminationMode {
        // all subflows must terminate normally
        NORMAL_END("Normal End"), 
        // max execution time for the node after wich all executing subflows are terminated 
        // and all queued subflows are cancelled
        TIMEOUT("Timeout"), 
        // at first subflow that ends with succes all executing subflows are terminated 
        // and all queued subflows are cancelled
        FIRST_END("First End"), 
        // at first subflow that ends with error all executing subflows are terminated 
        // and all queued subflows are cancelled
        FIRST_ERROR("First Error");

        private String desc;

        private TerminationMode(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }

        public static TerminationMode fromString(String name) {
            if ((name == null) || "".equals(name)) {
                return null;
            }
            if ("normal-end".equals(name)) {
                return NORMAL_END;
            }
            if ("first-end".equals(name)) {
                return FIRST_END;
            }
            if ("first-error".equals(name)) {
                return FIRST_ERROR;
            }
            if ("timeout".equals(name)) {
                return TIMEOUT;
            }
            return null;
        }
    }


    private Logger             logger;
    private String             owner;
    private boolean            isTimedout;
    
    /**
     * Executor of SubFlowTask instances.
     */
    private ThreadPoolExecutor executor = null;

    public ParallelExecutor(String owner, int threadMax, Logger logger) {
        this.owner = owner;
        this.logger = logger;

        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
        executor = new ThreadPoolExecutor(threadMax, threadMax, 2L, TimeUnit.MINUTES, queue, new BaseThreadFactory(
                "ParallelExecutor#NONE", true));
    }

    public boolean isTimedout() {
        return this.isTimedout;
    }

    public List<Result> execute(Id ownerId, List<SubFlowTask> tasks, TerminationMode termMode, long timeout)
            throws InterruptedException {
        if (ownerId == null)
            throw new NullPointerException("Invalid ownerId");
        if (tasks == null)
            throw new NullPointerException("Invalid tasks");
        if (tasks.size() == 0)
            throw new IllegalArgumentException("Empty tasks");
        isTimedout = false;

        ((BaseThreadFactory) executor.getThreadFactory()).setThNamePrefix("ParallelExecutor#" + owner + "#"
                + ownerId.toString());

        List<Result> results = null;

        switch (termMode) {
            case NORMAL_END :
                results = executeAll(tasks, termMode, timeout);
                break;
            case TIMEOUT :
                results = executeAll(tasks, termMode, timeout);
                break;
            case FIRST_END :
                results = endOnFirst(tasks, termMode, timeout);
                break;
            case FIRST_ERROR :
                results = endOnFirst(tasks, termMode, timeout);
                break;
        }

        return results;
    }

    private List<Result> executeAll(List<SubFlowTask> tasks, TerminationMode termMode, long timeout)
            throws InterruptedException {
        List<Future<Result>> futures = null;
        boolean timed = (termMode == TerminationMode.TIMEOUT);
        if (timed) {
            futures = executor.invokeAll(tasks, timeout, TimeUnit.SECONDS);
        }
        else {
            futures = executor.invokeAll(tasks);
        }
        List<Result> results = new ArrayList<Result>(tasks.size());
        Iterator<SubFlowTask> taskIter = tasks.iterator();
        for (Future<Result> f : futures) {
            SubFlowTask task = taskIter.next();
            try {
                results.add(f.get());
            }
            catch (ExecutionException exc) {
                results.add(task.getFailureResult(exc.getCause()));
            }
            catch (CancellationException exc) {
                if (timed) {
                    isTimedout = true;
                }
                results.add(task.getCancelledResult(exc));
            }
        }

        return results;
    }

    
    
    private List<Result> endOnFirst(List<SubFlowTask> tasks, TerminationMode termMode, long timeout)
            throws InterruptedException {
        boolean timed = (termMode == TerminationMode.TIMEOUT);
        long nanos = timeout * (10 ^ 9);
        int ntasks = tasks.size();
        List<Result> results = new ArrayList<Result>();
        List<Future<Result>> futures = new ArrayList<Future<Result>>(ntasks);
        Map<Future<Result>, SubFlowTask> futuresToTasks = new HashMap<Future<Result>, SubFlowTask>();
        ExecutorCompletionService<Result> ecs = new ExecutorCompletionService<Result>(executor);
        try {
            long lastTime = (timed) ? System.nanoTime() : 0;
            SubFlowTask t = null;
            Future<Result> f = null;
            Iterator<SubFlowTask> it = tasks.iterator();
            while (it.hasNext()) {
                t = it.next();
                f = ecs.submit(t);
                futuresToTasks.put(f, t);
                futures.add(f);
            }
            
            for (int i = 0; i < ntasks; ++i) {
                if (timed) {
                    f = ecs.poll(nanos, TimeUnit.NANOSECONDS);
                    if ((f == null) && (nanos <= 0)) {
                        throw new TimeoutException("Timeout " + timeout + "s elapsed");
                    }
                    long now = System.nanoTime();
                    nanos -= now - lastTime;
                    lastTime = now;
                }
                else {
                    f = ecs.take();
                }
                if (f != null) {
                    Result res = null;
                    try {
                        res = f.get(100, TimeUnit.MILLISECONDS);
                    }
                    catch (InterruptedException exc) {
                        throw exc;
                    }
                    catch (ExecutionException exc) {
                        res = futuresToTasks.get(f).getFailureResult(exc.getCause());
                    }
                    catch (CancellationException exc) {
                        res = futuresToTasks.get(f).getCancelledResult(exc);
                    }
                    catch (TimeoutException exc) {
                        continue;
                    }
                    catch (Exception exc) {
                        res = futuresToTasks.get(f).getFailureResult(exc);
                    }
                    switch (termMode) {
                        case FIRST_END :
                            if (res.getState() == Result.State.STATE_OK) {
                                results.add(res);
                                return results;
                            }
                            break;
                        case FIRST_ERROR :
                            if (res.getState() == Result.State.STATE_OK) {
                                results.add(res);
                            }
                            else {
                                return results;
                            }
                            break;
                        case NORMAL_END:
                        case TIMEOUT:
                        default:
                            break;
                    }
                }
            }
        }
        catch (TimeoutException exc) {
            logger.warn(exc.getMessage());
            isTimedout = true;
        }
        finally {
            for (Future<Result> f : futures) {
                f.cancel(true);
            }
            futures.clear();
            futuresToTasks.clear();
        }
        return results;
    }
    
    
    public void cleanup(boolean forceTermination) {
        executor.getQueue().clear();
    }

    public void destroy() {
        executor.shutdownNow();
        executor = null;
    }
}
