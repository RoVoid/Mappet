package mchorse.mappet.api.scripts.user.data;

import mchorse.mappet.api.scripts.code.blocks.ScriptBlockState;
import mchorse.mappet.api.scripts.code.data.ScriptBox;
import mchorse.mappet.api.scripts.code.data.ScriptVector;
import mchorse.mappet.api.scripts.code.world.ScriptWorld;

import java.util.List;

/**
 * Script Box
 * <p> CREATE: {@link mchorse.mappet.api.scripts.user.IScriptMath#box(double, double, double, double, double, double)} </p>
 *
 * <pre>{@code
 * function main(c)
 * {
 *     var pos = c.getSubject().getPosition();
 *     var box = mappet.box(-10, 4, -10, 10, 6, 10);
 *     if (box.contains(pos)){
 *         c.send("Player in the box")
 *     }
 * }
 * }</pre>
 */

public interface IScriptBox {
    /**
     * Checks if this box collides with another.
     */
    boolean isColliding(ScriptBox box);

    /**
     * Offsets the box by given coordinates
     *
     * <pre>{@code
     * function main(c)
     * {
     *     var box = mappet.box(-10, 4, -10, 10, 6, 10);
     *     box.offset(10, 0, 10);
     *     c.send(box.toString()); // ScriptBox(0.0, 4.0, 0.0, 20.0, 6.0, 20.0)
     * }
     * }</pre>
     */
    void offset(double x, double y, double z);

    /**
     * <p id="title"> Checks if given coordinates are inside of this box </p>
     *
     * <pre>{@code
     * function main(c)
     * {
     *     var box = mappet.box(-10, 4, -10, 10, 6, 10);
     *     c.send(box.contains(0, 4, 0)) // true
     *     c.send(box.contains(0, 7, 0)) // false
     * }
     * }</pre>
     */
    boolean contains(double x, double y, double z);

    /**
     * <pre>{@code
     * function main(c)
     * {
     *     var box = mappet.box(-10, 4, -10, 10, 6, 10);
     *     var pos = c.getSubject().getPosition()
     *     if (box.contains(pos)){
     *         c.send("Subject is inside the box")
     *     }
     * }
     * }</pre>
     */
    boolean contains(ScriptVector vector);

    /**
     * Returns a list of positions for blocks in the box that match a given block state in a given world.
     *
     * <pre>{@code
     * function main(c)
     * {
     *     var state = mappet.createBlockState("minecraft:stone");
     *     var box = mappet.box(-10, 4, -10, 10, 6, 10);
     *
     *     c.send(box.getBlocksPositions(c.getWorld(), state));
     * }
     * }</pre>
     */
    List<ScriptVector> getBlocksPositions(ScriptWorld world, ScriptBlockState state);

    String toString();
}

