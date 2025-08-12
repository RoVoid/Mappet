package mchorse.mappet.api.scripts.code;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.user.IScriptResourcePack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ScriptResourcePack implements IScriptResourcePack {
    String name;
    byte[] pack;

    public ScriptResourcePack(String name) {
        this.name = name;

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        File folder = server.getFile("config/mappet/packs");
        folder.mkdir();
        if (folder.exists() && folder.isDirectory()) {
            File packFile = server.getFile("config/mappet/packs/" + name + ".zip");
            if (!packFile.exists() || packFile.isDirectory()) {
                pack = null;
                return;
            }
            try {
                pack = Files.readAllBytes(packFile.toPath());
            } catch (IOException e) {
                Mappet.logger.error("Resource Pack \""+name+"\" not be found");
                pack = null;
            }
        }
    }

    public String getName() {
        return name;
    }

    public byte[] getPack() {
        return pack;
    }
}
