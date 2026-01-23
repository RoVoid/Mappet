package mchorse.mappet.capabilities.character;

import mchorse.mappet.api.dialogues.Dialogue;
import mchorse.mappet.api.dialogues.DialogueContext;
import mchorse.mappet.api.huds.HUDScene;
import mchorse.mappet.api.quests.Quests;
import mchorse.mappet.api.states.States;
import mchorse.mappet.api.ui.UIContext;
import mchorse.mappet.utils.CurrentSession;
import mchorse.mappet.utils.PositionCache;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ICharacter extends INBTSerializable<NBTTagCompound> {
    UUID getCamera();

    States getStates();

    Quests getQuests();

    /* Dialogue */

    void setDialogue(Dialogue dialogue, DialogueContext context);

    Dialogue getDialogue();

    DialogueContext getDialogueContext();

    /* Last clear */

    Instant getLastClear();

    void updateLastClear(Instant instant);

    /* Prev position */

    PositionCache getPositionCache();

    /* Admin editing */

    CurrentSession getCurrentSession();

    void copy(ICharacter character, EntityPlayer player);

    /* GUIs */

    UIContext getUIContext();

    void setUIContext(UIContext context);

    /* HUDs */
    boolean setupHUD(String id, boolean addToDisplayedList);

    void changeHUDMorph(String id, int index, NBTTagCompound tag);

    void closeHUD(String id);

    void closeAllHUDs();

    void closeAllHUDs(List<String> ignores);

    Map<String, List<HUDScene>> getDisplayedHUDs();
}