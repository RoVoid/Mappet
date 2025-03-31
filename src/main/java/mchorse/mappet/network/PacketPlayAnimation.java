package mchorse.mappet.network;

import io.netty.buffer.ByteBuf;
import mchorse.chameleon.animation.ActionConfig;
import mchorse.chameleon.animation.Animator;
import mchorse.chameleon.metamorph.ChameleonMorph;
import mchorse.mappet.Mappet;
import mchorse.mclib.network.ClientMessageHandler;
import mchorse.metamorph.api.models.IMorphProvider;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class PacketPlayAnimation implements IMessage {

    String animation;
    String uuid;

    public PacketPlayAnimation(String animation, String uuid) {
        this.animation = animation;
        this.uuid = uuid;
    }

    public PacketPlayAnimation() {

    }


    @Override
    public void fromBytes(ByteBuf buf) {
        this.animation = ByteBufUtils.readUTF8String(buf);
        this.uuid = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.animation);
        ByteBufUtils.writeUTF8String(buf, this.uuid);
    }

    public static class ClientHandler extends ClientMessageHandler<PacketPlayAnimation> {

        @Override
        @SideOnly(Side.CLIENT)
        public void run(EntityPlayerSP player, PacketPlayAnimation message) {
            player.world.getLoadedEntityList().stream().filter(entity -> entity.getUniqueID().equals(UUID.fromString(message.uuid))).forEach(entity -> {
                AbstractMorph morph = getMorph(entity);
                if (!(morph instanceof ChameleonMorph)) return;

                ChameleonMorph chameleonMorph = (ChameleonMorph) morph;
                try {
                    Method method = ChameleonMorph.class.getDeclaredMethod("getAnimator");
                    method.setAccessible(true);
                    Animator animator = (Animator) method.invoke(chameleonMorph);
                    ActionConfig config = chameleonMorph.actions.getConfig(message.animation);
                    animator.addAction(animator.createAction(animator.animation, config, false));
                } catch (NoSuchMethodException e) {
                    Mappet.logger.error("Method 'getAnimator' not found in ChameleonMorph. Ensure compatibility with Chameleon mod!");
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Mappet.logger.error(e.getMessage());
                }
            });
        }

        public AbstractMorph getMorph(Entity entity) {
            if (entity instanceof IMorphProvider) return ((IMorphProvider) entity).getMorph();
            else if (entity instanceof EntityPlayer) return Morphing.get((EntityPlayer) entity).getCurrentMorph();
            else return null;
        }
    }
}
