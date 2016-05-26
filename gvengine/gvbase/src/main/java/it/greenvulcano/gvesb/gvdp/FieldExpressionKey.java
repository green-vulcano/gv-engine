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
package it.greenvulcano.gvesb.gvdp;

/**
 * FieldExpressionKey is a representation of an identifier for a specific
 * element that may be retrieved from a {@link IDataProvider}.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class FieldExpressionKey
{
    /**
     * @version 3.0.0 Mar 10, 2010
     * @author GreenVulcano Developer Team
     *
     */
    public enum Direction {
        /**
         * This direction tells the data provider to get value from the input
         * object.
         */
        IN,
        /**
         * This direction tells the data provider to get value from the input
         * object and to set to the output object.
         */
        INOUT,
        /**
         * This direction tells the data provider to set value to the output
         * object.
         */
        OUT
    };

    private String    fieldId;
    private String    expressionType;
    private String    expression;
    private Direction direction;

    /**
     * Constructs a new FieldExpressionKey with the specified id.
     *
     * @param fieldId
     *        The id of this field
     * @param expressionType
     *        the type of expression to use
     * @param expression
     *        The expression of this field
     * @param direction
     *        The direction the value resulting the expression will be write to.
     */
    public FieldExpressionKey(String fieldId, String expressionType, String expression, String direction)
    {
        this.fieldId = fieldId;
        this.expressionType = expressionType;
        this.expression = expression;
        this.direction = Direction.valueOf(direction);
    }

    /**
     * @return the field id
     */
    public String getFieldId()
    {
        return fieldId;
    }

    /**
     * @return the expression
     */
    public String getExpression()
    {
        return expression;
    }

    /**
     * @return the expression type
     */
    public String getExpressionType()
    {
        return expressionType;
    }

    /**
     * @return the direction
     */
    public Direction getDirection()
    {
        return direction;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o)
    {
        if (o instanceof FieldExpressionKey) {
            String otherFieldId = ((FieldExpressionKey) o).getFieldId();
            return (fieldId == otherFieldId || (fieldId != null && fieldId.equals(otherFieldId)));
        }
        return false;
    }
}
