package mchorse.mappet.network.server.logs;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.utils.logs.MappetLogger;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.logs.PacketLogs;
import mchorse.mappet.network.common.logs.PacketRequestLogs;
import mchorse.mclib.network.ServerMessageHandler;
import mchorse.mclib.utils.OpHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.DimensionManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public class ServerHandlerLogs extends ServerMessageHandler<PacketRequestLogs> {
    @Override
    public void run(EntityPlayerMP player, PacketRequestLogs message) {
        if (!OpHelper.isPlayerOp(player)) return;


        File mappetWorldFolder = new File(DimensionManager.getCurrentSaveRootDirectory(), Mappet.MOD_ID);
        File logFile = new File(mappetWorldFolder, "logs/latest.log");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(logFile));

            int stringEncodingLimit = 16384; // Because of forge :(

            StringBuilder stringBuilder = new StringBuilder();

            String line;
            if (!message.lastLogTime.isEmpty()) {
                LocalDateTime lastLogTime = Instant.parse(message.lastLogTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
                while ((line = reader.readLine()) != null) if (isNewLine(lastLogTime, line)) break;
            } else line = reader.readLine();

            int byteCount = 0;
            while (line != null) {
                int lineByteCount = line.getBytes(StandardCharsets.UTF_8).length + 1;
                if (byteCount + lineByteCount >= stringEncodingLimit) break;

                stringBuilder.append(line).append("\n");
                byteCount += lineByteCount;

                line = reader.readLine();
            }

            String str = stringBuilder.toString();
            if (!str.isEmpty()) Dispatcher.sendTo(new PacketLogs(str), player);
        } catch (IOException ignored) {
        }
    }

    public boolean isNewLine(LocalDateTime date, String line) {
        int bracketIndex = line.indexOf("]");
        if (bracketIndex == -1) return false;
        String logDateString = line.substring(1, bracketIndex);
        try {
            return LocalDateTime.parse(logDateString, MappetLogger.dtf).isAfter(date);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}