/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.flow.parallel;

import it.greenvulcano.gvesb.buffer.GVBuffer;


/**
 * 
 * @version 3.4.0 Jun 17, 2013
 * @author GreenVulcano Developer Team
 * 
 */
public class Result
{
    public enum State {
        STATE_OK("Normal End"),
        STATE_ERROR("Execution Error"),
        STATE_TIMEOUT("Timed Out"),
        STATE_CANCELLED("Flow Cancelled"),
        STATE_INTERRUPTED("Flow Interrupted");

        private String desc;

        private State(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }
    }

    private State    state;
    private Object   output;
    private GVBuffer input;

    public Result(State state, GVBuffer input, Object output) {
        this.state = state;
        this.input = input;
        this.output = output;
    }

    public State getState() {
        return this.state;
    }

    public Object getOutput() {
        return this.output;
    }

    public GVBuffer getInput() {
        return this.input;
    }

    @Override
    public String toString() {
        return "Result state[" + state + "] output: " + output;
    }
}
