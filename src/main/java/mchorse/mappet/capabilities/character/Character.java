package mchorse.mappet.capabilities.character;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.dialogues.Dialogue;
import mchorse.mappet.api.dialogues.DialogueContext;
import mchorse.mappet.api.huds.HUDMorph;
import mchorse.mappet.api.huds.HUDScene;
import mchorse.mappet.api.quests.Quests;
import mchorse.mappet.api.states.States;
import mchorse.mappet.api.ui.UIContext;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.huds.PacketHUDMorph;
import mchorse.mappet.network.common.huds.PacketHUDScene;
import mchorse.mappet.utils.CurrentSession;
import mchorse.mappet.utils.PositionCache;
import mchorse.metamorph.api.Morph;
import mchorse.metamorph.api.MorphManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.time.Instant;
import java.util.*;

public class Character implements ICharacter {
    private EntityPlayer player;

    public static Character get(EntityPlayer player) {
        ICharacter characterCapability = player == null ? null : player.getCapability(CharacterProvider.CHARACTER, null);
        if (characterCapability instanceof Character) {
            Character character = (Character) characterCapability;
            character.player = player;
            return character;
        }
        return null;
    }

    private final Quests quests = new Quests();
    private final States states = new States();

    private Dialogue dialogue;
    private DialogueContext dialogueContext;

    private Instant lastClear = Instant.now();

    private final PositionCache positionCache = new PositionCache();
    private final CurrentSession session = new CurrentSession();

    private UIContext uiContext;

    private Map<String, List<HUDScene>> displayedHUDs = new HashMap<>();

    private UUID cameraUuid;

    @Override
    public States getStates() {
        return states;
    }

    @Override
    public Quests getQuests() {
        return quests;
    }

    @Override
    public void setDialogue(Dialogue dialogue, DialogueContext context) {
        if (dialogue == null && this.dialogue != null) {
            this.dialogue.onClose.trigger(dialogueContext.data);
        }

        this.dialogue = dialogue;
        dialogueContext = context;
    }

    @Override
    public Dialogue getDialogue() {
        return dialogue;
    }

    @Override
    public DialogueContext getDialogueContext() {
        return dialogueContext;
    }

    @Override
    public Instant getLastClear() {
        return lastClear;
    }

    @Override
    public void updateLastClear(Instant instant) {
        lastClear = instant;
    }

    @Override
    public PositionCache getPositionCache() {
        return positionCache;
    }

    @Override
    public CurrentSession getCurrentSession() {
        return session;
    }

    @Override
    public void copy(ICharacter character, EntityPlayer player) {
        quests.copy(character.getQuests());
        states.copy(character.getStates());
        lastClear = character.getLastClear();
        displayedHUDs = character.getDisplayedHUDs();
        cameraUuid = character.getCamera();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setTag("Quests", quests.serializeNBT());
        tag.setTag("States", states.serializeNBT());
        tag.setString("LastClear", lastClear.toString());
        tag.setTag("DisplayedHUDs", serializeDisplayedHUDs());
        if(cameraUuid != null) tag.setUniqueId("Camera", cameraUuid);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        if (tag.hasKey("Quests")) {
            quests.deserializeNBT(tag.getCompoundTag("Quests"));
        }

        if (tag.hasKey("States")) {
            states.deserializeNBT(tag.getCompoundTag("States"));
        }

        if (tag.hasKey("LastClear")) {
            try {
                lastClear = Instant.parse(tag.getString("LastClear"));
            } catch (Exception ignored) {
            }
        }

        if (tag.hasKey("DisplayedHUDs")) {
            deserializeDisplayedHUDs(tag.getCompoundTag("DisplayedHUDs"));
        }

        if (tag.hasUniqueId("Camera")) {
            cameraUuid = tag.getUniqueId("Camera");
        }
    }

    /* GUIs */

    @Override
    public UIContext getUIContext() {
        return uiContext;
    }

    @Override
    public void setUIContext(UIContext context) {
        uiContext = context;
    }

    /* HUDs */

    public boolean setupHUD(String id, boolean addToDisplayedList) {
        HUDScene scene = Mappet.huds.load(id);

        if (scene != null) {
            Dispatcher.sendTo(new PacketHUDScene(scene.getId(), scene.serializeNBT()), (EntityPlayerMP) player);

            //if the hud is global, display it to all players as well
            if (scene.global) {
                for (EntityPlayer player : player.world.playerEntities) {
                    if (player != this.player) {
                        Dispatcher.sendTo(new PacketHUDScene(scene.getId(), scene.serializeNBT()), (EntityPlayerMP) player);
                    }
                }
            }

            // Adds the morph to the displayedHUDs list
            if (addToDisplayedList) {
                getDisplayedHUDs().put(id, Collections.singletonList(scene));
            }
            return true;
        }

        return false;
    }

    @Override
    public void changeHUDMorph(String id, int index, NBTTagCompound tag) {
        Dispatcher.sendTo(new PacketHUDMorph(id, index, tag), (EntityPlayerMP) player);

        //if the hud is global, display change it for all players as well
        HUDScene scene = Mappet.huds.load(id);

        if (scene.global) {
            for (EntityPlayer player : player.world.playerEntities) {
                if (player != this.player) {
                    Dispatcher.sendTo(new PacketHUDMorph(id, index, tag), (EntityPlayerMP) player);
                }
            }
        }

        // Changing the HUDMorph in the displayedHUDs list
        for (Map.Entry<String, List<HUDScene>> entry : getDisplayedHUDs().entrySet()) {
            if (entry.getKey().equals(id)) {
                List<HUDScene> scenes = entry.getValue();
                if (!scenes.isEmpty()) {
                    scene = scenes.get(0);
                    if (scene.morphs.size() > index) {
                        HUDMorph newMorph = scene.morphs.get(index).copy();
                        newMorph.morph = new Morph(MorphManager.INSTANCE.morphFromNBT(tag));
                        scene.morphs.set(index, newMorph);
                    }
                }
            }
        }
    }

    @Override
    public void closeHUD(String id) {
        HUDScene scene = Mappet.huds.load(id);
        if (scene.global) {
            for (EntityPlayer player : player.world.playerEntities)
                Dispatcher.sendTo(new PacketHUDScene(id == null ? "" : id, null), (EntityPlayerMP) player);
        }
        else Dispatcher.sendTo(new PacketHUDScene(id == null ? "" : id, null), (EntityPlayerMP) player);
        getDisplayedHUDs().remove(id);
    }

    @Override
    public void closeAllHUDs() {
        closeAllHUDs(new ArrayList<>());
    }

    @Override
    public void closeAllHUDs(List<String> ignores) {
        for (Map.Entry<String, List<HUDScene>> entry : getDisplayedHUDs().entrySet()) {
            if (ignores.contains(entry.getKey())) continue;
            if (entry.getValue().get(0).global) {
                for (EntityPlayer player : player.world.playerEntities)
                    Dispatcher.sendTo(new PacketHUDScene(entry.getKey(), null), (EntityPlayerMP) player);
            }
            else Dispatcher.sendTo(new PacketHUDScene(entry.getKey(), null), (EntityPlayerMP) player);
        }
        getDisplayedHUDs().clear();
    }

    @Override
    public Map<String, List<HUDScene>> getDisplayedHUDs() {
        return displayedHUDs;
    }

    private NBTTagCompound serializeDisplayedHUDs() {
        return getDisplayedHUDsTag();
    }

    private void deserializeDisplayedHUDs(NBTTagCompound tag) {
        displayedHUDs.clear();
        for (String key : tag.getKeySet()) {
            NBTTagList sceneList = tag.getTagList(key, Constants.NBT.TAG_COMPOUND);
            List<HUDScene> scenes = new ArrayList<>();
            for (int i = 0; i < sceneList.tagCount(); i++) {
                NBTTagCompound sceneTag = sceneList.getCompoundTagAt(i);
                HUDScene scene = new HUDScene();
                scene.deserializeNBT(sceneTag);
                scenes.add(scene);
            }
            displayedHUDs.put(key, scenes);
        }
    }

    public NBTTagCompound getDisplayedHUDsTag() {
        NBTTagCompound tag = new NBTTagCompound();
        for (Map.Entry<String, List<HUDScene>> entry : displayedHUDs.entrySet()) {
            NBTTagList sceneList = new NBTTagList();
            for (HUDScene scene : entry.getValue()) {
                sceneList.appendTag(scene.serializeNBT());
            }
            tag.setTag(entry.getKey(), sceneList);
        }
        return tag;
    }

    public NBTTagCompound getGlobalDisplayedHUDsTag() {
        NBTTagCompound tag = new NBTTagCompound();
        for (Map.Entry<String, List<HUDScene>> entry : displayedHUDs.entrySet()) {
            if (entry.getValue().get(0).global) {
                NBTTagList sceneList = new NBTTagList();
                for (HUDScene scene : entry.getValue()) {
                    sceneList.appendTag(scene.serializeNBT());
                }
                tag.setTag(entry.getKey(), sceneList);
            }
        }
        return tag;
    }

    /**
     * This method checks on character's tick if it has
     * a scene with an HUDMorph with `expire` in one of its displayedHUDs.
     * If it has, and it's not 0, then it should decrement it.
     * If it's 0, then it should remove the HUDMorph from the scene.
     * If the scene is empty, then it should remove the scene from the displayedHUDs.
     */
    public void updateDisplayedHUDsList() {
        Iterator<Map.Entry<String, List<HUDScene>>> iterator = getDisplayedHUDs().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<HUDScene>> entry = iterator.next();
            List<HUDScene> scenes = entry.getValue();
            boolean removeScene = false;
            for (HUDScene scene : scenes) {
                List<HUDMorph> morphs = scene.morphs;
                boolean updated = false;
                for (int i = 0; i < morphs.size(); i++) {
                    HUDMorph morph = morphs.get(i);
                    if (morph.expire > 0) {
                        morph.expire--;
                        if (morph.expire == 0) {
                            morphs.remove(i);
                            i--;
                            updated = true;
                        }
                    }
                }
                if (updated && morphs.isEmpty()) {
                    removeScene = true;
                    break;
                }
            }
            if (removeScene) {
                iterator.remove();
            }
        }
    }

    @Override
    public UUID getCamera() {
        return cameraUuid;
    }
}