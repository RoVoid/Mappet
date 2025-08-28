package mchorse.mappet.capabilities.camera;

import mchorse.mappet.capabilities.character.ICharacter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CameraProvider implements ICapabilitySerializable<NBTTagCompound> {
    @CapabilityInject(ICharacter.class)
    public static final Capability<ICamera> CAMERA = null;

    private final Camera instance = new Camera();

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CAMERA;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == CAMERA ? CAMERA.cast(instance) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        instance.deserializeNBT(nbt);
    }
}
