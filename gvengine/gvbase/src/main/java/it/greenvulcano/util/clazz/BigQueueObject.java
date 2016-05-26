/*
 * Copyright (c) 2009-2015 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.util.clazz;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang.SerializationUtils;

import com.leansoft.bigqueue.BigQueueImpl;
import com.leansoft.bigqueue.IBigQueue;

/**
 * @version 3.5.0 28/gen/2015
 * @author GreenVulcano Developer Team
 */
public class BigQueueObject<T extends Serializable>
{
    private IBigQueue bigQueue = null;
    private String queueDir;
    private String queueName;

    public BigQueueObject(String queueDir, String queueName) throws IOException {
        this.queueDir = queueDir;
        this.queueName = queueName;
        this.bigQueue = new BigQueueImpl(queueDir, queueName);
    }

    public void flush() {
        this.bigQueue.flush();
    }

    public void close() throws IOException {
        this.bigQueue.close();
    }
    
    public void gc() throws IOException {
        this.bigQueue.gc();
    }

    public void writeObject(T obj) throws IOException {
        this.bigQueue.enqueue(SerializationUtils.serialize(obj));
    }
    
    @SuppressWarnings("unchecked")
    public T readObject() throws IOException {
        byte[] data = this.bigQueue.dequeue();
        if (data == null) return null;
        return (T) SerializationUtils.deserialize(data);
    }

    public boolean isEmpty() {
        return this.bigQueue.isEmpty();
    }

    public long size() {
        return this.bigQueue.size();
    }
    
    public void removeAll() throws IOException {
        this.bigQueue.removeAll();
    }
    
    @Override
    public String toString() {
        return "BigQueueObject<" + getClass().getName() + ">[" + queueDir + " - " + queueName + "]";
    }

    @Override
    protected void finalize() throws Throwable {
        if (this.bigQueue != null) {
            try {
                this.bigQueue.close();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        this.bigQueue = null;
        super.finalize();
    }
}
