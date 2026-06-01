package mchorse.mappet.api.scripts.code.entities;

import mchorse.mappet.api.scripts.code.items.ScriptItemStack;
import mchorse.mappet.api.scripts.code.items.ScriptItemStack;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

public class ScriptEntityItem extends ScriptEntity<EntityItem> {

    public ScriptEntityItem(EntityItem entity)
    {
        super(entity);
    }

    public int getAge()
    {
        return this.entity.age;
    }

    public void setAge(int age)
    {
        this.entity.age = age;
    }

    public int getPickupDelay()
    {
        return this.entity.pickupDelay;
    }

    public void setPickupDelay(int delay)
    {
        this.entity.pickupDelay = delay;
    }

    public int getLifespan()
    {
        return this.entity.lifespan;
    }

    public void setLifespan(int lifespan)
    {
        this.entity.lifespan = lifespan;
    }

    public String getItemOwner()
    {
        return this.entity.getOwner();
    }

    public void setItemOwner(String owner)
    {
        this.entity.setOwner(owner);
    }

    public String getThrower()
    {
        return this.entity.getThrower();
    }

    public void setThrower(String thrower)
    {
        this.entity.setThrower(thrower);
    }

    public ScriptItemStack getItem()
    {
        return ScriptItemStack.create(this.entity.getItem());
    }

    public void setItem(ScriptItemStack itemStack)
    {
        this.entity.setItem(itemStack == null ? ItemStack.EMPTY : itemStack.getMinecraftItemStack());
    }

    public void setInfinitePickupDelay()
    {
        this.entity.setInfinitePickupDelay();
    }

    public void setDefaultPickupDelay()
    {
        this.entity.setDefaultPickupDelay();
    }

    public void setNoDespawn()
    {
        this.entity.setNoDespawn();
    }

    public boolean canPickup()
    {
        return !this.entity.cannotPickup();
    }
}
