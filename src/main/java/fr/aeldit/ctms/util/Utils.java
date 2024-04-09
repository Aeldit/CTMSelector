package fr.aeldit.ctms.util;

import fr.aeldit.ctms.textures.CTMPacks;
import fr.aeldit.ctms.textures.FilesHandling;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Utils
{
    public static final String CTMS_MODID = "ctms";
    public static final Logger CTMS_LOGGER = LoggerFactory.getLogger(CTMS_MODID);

    public static final FilesHandling TEXTURES_HANDLING = new FilesHandling();
    public static CTMPacks CTM_PACKS;

    public static final Path RESOURCE_PACKS_DIR = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");

    //public static int ICON_INDEX = 0;
}
