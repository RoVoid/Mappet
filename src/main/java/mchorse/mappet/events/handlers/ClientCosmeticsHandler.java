package mchorse.mappet.events.handlers;

import mchorse.mappet.client.CameraReflect;
import mchorse.mappet.client.RenderingHandler;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.client.ClientHandlerBlackAndWhiteShader;
import mchorse.mappet.network.client.ClientHandlerPlayerPerspective;
import mchorse.mappet.network.packets.npc.PacketNpcJump;
import mchorse.mclib.utils.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

/**
 * Purely client-side visual/input glue: third-person perspective
 * lock + black-and-white shader toggling, periodic skin texture refresh,
 * the NPC-steering jump key, and camera setup. None of this has any
 * server-side behaviour, unlike the rest of what used to be
 * {@code EventHandler} — that's the whole reason it's split out into its
 * own {@code @SideOnly(Side.CLIENT)}-flavoured class.
 */
public class ClientCosmeticsHandler {
    private static int previousPerspective = 0;

    private int skinCounter;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        if (event.player.world.isRemote) RenderingHandler.update();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (ClientHandlerPlayerPerspective.locked()) {
            if (mc.gameSettings.thirdPersonView != ClientHandlerPlayerPerspective.getPerspective()) {
                mc.gameSettings.thirdPersonView = ClientHandlerPlayerPerspective.getPerspective();
            }
        }
        if (previousPerspective != mc.gameSettings.thirdPersonView) {
            previousPerspective = mc.gameSettings.thirdPersonView;
            ClientHandlerBlackAndWhiteShader.update();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (skinCounter++ >= 250) {
            updateSkins();
            skinCounter = 0;
        }
    }

    @SideOnly(Side.CLIENT)
    private void updateSkins() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return;

        Map<ResourceLocation, ITextureObject> map = ReflectionUtils.getTextures(mc.renderEngine);
        for (EntityPlayer player : mc.world.playerEntities) {
            if (player instanceof EntityOtherPlayerMP) {
                map.put(AbstractClientPlayer.getLocationSkin(player.getName()), map.get(((EntityOtherPlayerMP) player).getLocationSkin()));
            }
            else if (player instanceof EntityPlayerSP) {
                map.put(AbstractClientPlayer.getLocationSkin(player.getName()), map.get(((EntityPlayerSP) player).getLocationSkin()));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!Minecraft.getMinecraft().gameSettings.keyBindJump.isPressed()) return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player.isRiding() && player.getRidingEntity() instanceof EntityNpc && ((EntityNpc) player.getRidingEntity()).getState().canBeSteered.get()) {
            float jumpPower = ((EntityNpc) player.getRidingEntity()).getState().jumpPower.get();
            Dispatcher.sendToServer(new PacketNpcJump(player.getRidingEntity().getEntityId(), jumpPower));
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        CameraReflect.onSetup(event);
    }
}
