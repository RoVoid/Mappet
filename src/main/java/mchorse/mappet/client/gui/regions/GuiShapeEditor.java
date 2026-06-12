package mchorse.mappet.client.gui.regions;

import mchorse.mappet.api.regions.Region;
import mchorse.mappet.api.regions.shapes.AbstractShape;
import mchorse.mappet.api.regions.shapes.BoxShape;
import mchorse.mappet.api.regions.shapes.CylinderShape;
import mchorse.mappet.api.regions.shapes.SphereShape;
import mchorse.mappet.utils.Colors;
import mchorse.mappet.client.gui.utils.GuiEnumElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.context.GuiSimpleContextMenu;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

import javax.vecmath.Vector3d;

public class GuiShapeEditor extends GuiElement {
    public GuiEnumElement<Shape> shapeSwitch;
    public GuiTrackpadElement x;
    public GuiTrackpadElement y;
    public GuiTrackpadElement z;
    public GuiTrackpadElement sizeX;
    public GuiTrackpadElement sizeY;
    public GuiTrackpadElement sizeZ;

    public GuiLabel bottomLabel;
    public GuiElement bottomRow;

    private Region region;
    private AbstractShape shape;

    public GuiShapeEditor(Minecraft mc) {
        super(mc);

        context(() -> new GuiSimpleContextMenu(this.mc).action(Icons.REMOVE, IKey.lang("mappet.gui.region.context.remove"), this::removeShape,
                Colors.NEGATIVE));

        shapeSwitch = new GuiEnumElement<>(mc, Shape.BOX, this::changeShape);
        shapeSwitch.bakeLabels("mappet.gui.shapes");

        x = new GuiTrackpadElement(mc, (v) -> shape.offset.x = v);
        y = new GuiTrackpadElement(mc, (v) -> shape.offset.y = v);
        z = new GuiTrackpadElement(mc, (v) -> shape.offset.z = v);

        sizeX = new GuiTrackpadElement(mc, (v) -> {
            if (shape instanceof BoxShape) ((BoxShape) shape).size.x = v;
            else if (shape instanceof SphereShape) ((SphereShape) shape).horizontal = v;
        });
        sizeY = new GuiTrackpadElement(mc, (v) -> {
            if (shape instanceof BoxShape) ((BoxShape) shape).size.y = v;
            else if (shape instanceof SphereShape) ((SphereShape) shape).vertical = v;
        });
        sizeZ = new GuiTrackpadElement(mc, (v) -> {
            if (shape instanceof BoxShape) ((BoxShape) shape).size.z = v;
        });

        flex().column(5).vertical().stretch();

        bottomLabel = Elements.label(IKey.lang(""));
        bottomRow = Elements.row(mc, 5, 0, sizeX, sizeY, sizeZ);

        add(shapeSwitch);
        add(Elements.label(IKey.lang("mappet.gui.region.offset")));
        add(Elements.row(mc, 5, 0, x, y, z));
        add(bottomLabel, bottomRow);
    }

    private void removeShape() {
        int index = parent.getChildren().indexOf(this);
        if (index < 0) return;

        removeFromParent();
        region.shapes.remove(index);
        getParentContainer().resize();
    }

    private void changeShape(Shape value) {
        AbstractShape shape = null;

        if (value == Shape.BOX) shape = new BoxShape();
        else if (value == Shape.SPHERE) shape = new SphereShape();
        else if (value == Shape.CYLINDER) shape = new CylinderShape();

        if (shape == null) return;

        int index = parent.getChildren().indexOf(this);
        if (index < 0) return;

        shape.from(this.shape);
        region.shapes.set(index, shape);
        set(region, shape);
    }

    public void set(Region region, AbstractShape shape) {
        this.region = region;
        this.shape = shape;

        if (shape instanceof BoxShape) shapeSwitch.select(Shape.BOX);
        else if (shape instanceof CylinderShape) shapeSwitch.select(Shape.CYLINDER);
        else shapeSwitch.select(Shape.SPHERE);

        x.setValue(shape.offset.x);
        y.setValue(shape.offset.y);
        z.setValue(shape.offset.z);

        sizeZ.removeFromParent();

        if (shape instanceof BoxShape) {
            Vector3d size = ((BoxShape) shape).size;

            sizeX.setValue(size.x);
            sizeY.setValue(size.y);
            sizeZ.setValue(size.z);

            bottomLabel.label.set("mappet.gui.region.box_size");
            bottomRow.add(sizeZ);
        }
        else if (shape instanceof SphereShape) {
            sizeX.setValue(((SphereShape) shape).horizontal);
            sizeY.setValue(((SphereShape) shape).vertical);

            bottomLabel.label.set(shape instanceof CylinderShape ? "mappet.gui.region.sphere_size" : "mappet.gui.region.ellipse_size");
        }

        if (hasParent()) getParentContainer().resize();
    }

    public enum Shape {
        BOX,
        SPHERE,
        CYLINDER
    }
}