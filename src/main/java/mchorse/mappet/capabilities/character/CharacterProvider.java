package mchorse.mappet.capabilities.character;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CharacterProvider implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(ICharacter.class)
    public static final Capability<ICharacter> CHARACTER = null;

    private final ICharacter instance = CHARACTER.getDefaultInstance();

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CHARACTER;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == CHARACTER ? CHARACTER.cast(instance) : null;
    }

    @Override
    public NBTBase serializeNBT() {
        return CHARACTER.getStorage().writeNBT(CHARACTER, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        CHARACTER.getStorage().readNBT(CHARACTER, instance, null, nbt);
    }
}