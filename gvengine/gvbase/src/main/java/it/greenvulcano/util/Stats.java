/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.util;

/**
 * The class support management of event's statistics informations.
 * The samplig starts at first <code>hint()</code> invocation. The history is
 * the weighted average o fthe last N <code>hint()</code>.
 *
 * <b>Usage:</b>
 * <p/>
 * At every event must be invoked the method <code>hint()</code>.<br/>
 * When needed the throughput can be obtained through the method <code>getThroughput()</code>.<br/>
 * When needed mean throughput can be obtained through the <code>getHistoryThroughput()</code>.<br/>
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class Stats
{
    /**
     * Samping time. Throughput's statistics are computed with this period.
     */
    private long    pollTimeMillis;

    /**
     * Scale. The times are computed to milliseconds precision, so if scale is 1 then we have hint()/millis,
     * if the scael is 1000 we have hint()/second.
     */
    private float   scale;

    /**
     * Time of first hint() or start start() methos invocation.
     */
    private long    startTime;

    /**
     * Time of last hint() invocation.
     */
    private long    endTime;

    /**
     * Total hint() count.
     */
    private long    totalHints;

    /**
     * Start time of current sample.
     */
    private long    sampleStartTime;

    /**
     * hint() count of current sample.
     */
    private long    sampleHints;

    /**
     * Last computed throughput.
     */
    private float   throughput;

    /**
     * Min computed throughput.
     */
    private float   minThroughput;

    /**
     * Max computed throughput.
     */
    private float   maxThroughput;

    /**
     * Circular array holding the last N calculated throughputs.
     */
    private float[] historyThroughputs;

    /**
     * Circular array holding the last N times used to calculate the last N throughputs.
     */
    private float[] historyWeights;

    /**
     * Sum of the last N throughputs multiplied with the corresponding last N times.
     */
    private float   historyThroughput;

    /**
     * Sum of the last N times.
     */
    private float   historyWeight;

    /**
     * Length of the circular array.
     */
    private int     historyLength;

    /**
     * Free position in the circular array.
     */
    private int     historyPosition;

    /**
     * Number of valid entry in the circular array.
     */
    private int     historySize;

    /**
     * Initialize the instance.
     *
     * @param scale
     * @param pollTimeMillis sampling time
     * @param historyLength history length
     */
    public Stats(float scale, long pollTimeMillis, int historyLength)
    {
        this.scale = scale;
        this.pollTimeMillis = pollTimeMillis;
        if (historyLength < 1) {
            historyLength = 1;
        }
        this.historyLength = historyLength;
        reset();
    }

    /**
     * Reset the statistics state.
     */
    public synchronized void reset()
    {
        sampleHints = 0;
        totalHints = 0;
        minThroughput = Float.NaN;
        maxThroughput = Float.NaN;
        throughput = Float.NaN;
        startTime = 0;
        endTime = 0;
        sampleStartTime = 0;

        historyThroughputs = new float[historyLength];
        historyWeights = new float[historyLength];
        historyPosition = 0;
        historySize = 0;
        historyThroughput = 0;
        historyWeight = 0;
    }

    /**
     * Checks if the last sample time is elapsed.
     *
     * @return if the last sample time is elapsed
     */
    public boolean isPollTimeElapsed()
    {
        return System.currentTimeMillis() - sampleStartTime > pollTimeMillis;
    }

    /**
     * Update the tota lhints counter and the current sample.
     * If the sampling time is elaped then update the throughput.
     *
     * @return true if the throughput is updated
     */
    public synchronized boolean hint()
    {
        endTime = System.currentTimeMillis();
        if (startTime == 0) {
            sampleStartTime = endTime;
            startTime = endTime;
        }
        if (endTime - sampleStartTime > pollTimeMillis) {
            getThroughput();
            ++sampleHints;
            ++totalHints;
            return true;
        }
        ++sampleHints;
        ++totalHints;
        return false;
    }

    /**
     * Compute the throughput. Update min and max throughputs.
     * If the sampling time is elaped then update the l'history and
     * start a new sample.
     *
     * @return throughput
     */
    public synchronized float getThroughput()
    {
        if (totalHints == 0) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - sampleStartTime;

        if (elapsedTime < pollTimeMillis) {
            return throughput;
        }

        throughput = sampleHints * scale / elapsedTime;

        updateHistory(throughput, elapsedTime);

        if (Float.isNaN(minThroughput)) {
            minThroughput = throughput;
        }
        else {
            if (throughput < minThroughput) {
                minThroughput = throughput;
            }
        }

        if (!Float.isInfinite(throughput)) {
            if (Float.isNaN(maxThroughput)) {
                maxThroughput = throughput;
            }
            else {
                if (throughput > maxThroughput) {
                    maxThroughput = throughput;
                }
            }
        }

        sampleStartTime = currentTime;
        sampleHints = 0;

        return throughput;
    }

    /**
     * Time of the first hint.
     *
     * @return Time of the first hint in millis.
     */
    public long getStartTime()
    {
        return startTime;
    }

    /**
     * Time of the last hint.
     *
     * @return Time of the last hint in millis.
     */
    public long getEndTime()
    {
        return endTime;
    }

    /**
     * Start time of the last sample.
     *
     * @return Start time of the last sample in millis.
     */
    public long getSampleStartTime()
    {
        return sampleStartTime;
    }

    /**
     * Hint's counter.
     *
     * @return number of hints
     */
    public long getTotalHints()
    {
        return totalHints;
    }

    /**
     * Min throughput.
     *
     * @return Min throughput.
     */
    public float getMinThroughput()
    {
        return minThroughput;
    }

    /**
     * Max throughput.
     *
     * @return Max throughput.
     */
    public float getMaxThroughput()
    {
        return maxThroughput;
    }

    /**
     * Average throughput.
     *
     * @return average throughput
     */
    public float getAverageThroughput()
    {
        long elapsedTime = getEndTime() - getStartTime();
        if (elapsedTime == 0) {
            if (getTotalHints() == 0) {
                return Float.NaN;
            }
            return Float.POSITIVE_INFINITY;
        }
        return getTotalHints() * scale / elapsedTime;
    }

    /**
     * Weighted average of the last N throughputs.
     *
     * @return weighted average of the last N throughputs.
     */
    public float getHistoryThroughput()
    {
        if (historyWeight == 0) {
            if (getTotalHints() == 0) {
                return Float.NaN;
            }
            return Float.POSITIVE_INFINITY;
        }
        return historyThroughput / historyWeight;
    }

    /**
     * Update the throughput history with the new throughput.
     */
    private void updateHistory(float throughput, long elapsedTime)
    {
        if (historySize < historyLength) {
            historyThroughput += throughput * elapsedTime;
            historyWeight += elapsedTime;
            ++historySize;
        }
        else {
            historyThroughput = historyThroughput
                    - (historyThroughputs[historyPosition] * historyWeights[historyPosition])
                    + (throughput * elapsedTime);
            historyWeight = historyWeight - historyWeights[historyPosition] + elapsedTime;
        }
        historyThroughputs[historyPosition] = throughput;
        historyWeights[historyPosition] = elapsedTime;
        historyPosition = (historyPosition + 1) % historyLength;
    }
}
