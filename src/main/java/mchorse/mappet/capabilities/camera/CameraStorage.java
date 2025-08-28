package mchorse.mappet.capabilities.camera;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class CameraStorage implements IStorage<ICamera> {
    @Override
    public NBTBase writeNBT(Capability<ICamera> capability, ICamera instance, net.minecraft.util.EnumFacing side) {
        return ((Camera) instance).serializeNBT();
    }

    @Override
    public void readNBT(Capability<ICamera> capability, ICamera instance, net.minecraft.util.EnumFacing side, NBTBase nbt) {
        if (nbt instanceof NBTTagCompound) ((Camera) instance).deserializeNBT((NBTTagCompound) nbt);
    }
}