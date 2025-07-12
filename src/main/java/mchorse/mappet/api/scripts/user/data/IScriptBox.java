package mchorse.mappet.api.scripts.user.data;

import mchorse.mappet.api.scripts.code.blocks.ScriptBlockState;
import mchorse.mappet.api.scripts.code.data.ScriptBox;
import mchorse.mappet.api.scripts.code.data.ScriptVector;
import mchorse.mappet.api.scripts.code.world.ScriptWorld;

import javax.vecmath.Vector3d;
import java.util.List;

/**
 * Script box represents a box in the space
 *
 * <pre>{@code
 * function main(c)
 * {
 *     var subject = c.getSubject();
 *     var subjectPosition = subject.getPosition();
 *     var box = mappet.box(-10, 4, -10, 10, 6, 10);
 *     if (box.contains(subjectPosition)){
 *         c.send("the player in in the box")
 *     }
 * }
 * }</pre>
 */

public interface IScriptBox
{
    /**
     * Returns a string representation of this box.
     *
     * @return String in the format ScriptBox(minX, minY, minZ, maxX, maxY, maxZ)
     */
    String toString();

    /**
     * Checks whether this box collides (intersects) with another box.
     *
     * @param box Another box to check collision with
     * @return True if the boxes intersect
     */
    boolean isColliding(ScriptBox box);

    /**
     * Offsets (moves) this box by the given amounts.
     *
     * @param x X-axis offset
     * @param y Y-axis offset
     * @param z Z-axis offset
     */
    void offset(double x, double y, double z);

    /**
     * Checks whether the specified coordinates are inside this box.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return True if the point is inside the box
     */
    boolean contains(double x, double y, double z);

    /**
     * Checks whether the specified vector is inside this box.
     *
     * @param vector A ScriptVector to check
     * @return True if the vector is inside the box
     */
    boolean contains(ScriptVector vector);

    /**
     * Checks whether the specified vector is inside this box.
     *
     * @param vector A Vector3d to check
     * @return True if the vector is inside the box
     */
    boolean contains(Vector3d vector);

    /**
     * Returns positions of blocks that match a given block state within this box.
     *
     * @param world The script world to scan
     * @param state The block state to match
     * @return List of positions of matching blocks
     */
    List<ScriptVector> getBlocksPositions(ScriptWorld world, ScriptBlockState state);
}

