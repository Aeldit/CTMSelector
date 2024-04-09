package fr.aeldit.ctms.util;

import fr.aeldit.ctms.textures.CTMPacks;
import fr.aeldit.ctms.textures.FilesHandling;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

public class Utils
{
    public static final String CTMS_MODID = "ctms";
    public static final Logger CTMS_LOGGER = LoggerFactory.getLogger(CTMS_MODID);

    public static final FilesHandling TEXTURES_HANDLING = new FilesHandling();
    public static CTMPacks CTM_PACKS;

    public static final Path RESOURCE_PACKS_DIR = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");

    public static @Nullable Path getFirstPropertiesFileInDir(@NotNull Path dir)
    {
        File[] files = dir.toFile().listFiles();
        System.out.println(dir);
        if (files == null)
        {
            return null;
        }

        for (File file : files)
        {
            System.out.println("test : " + file.getName());
            if (file.isFile() && file.getName().endsWith(".properties"))
            {
                System.out.println("file : " + file.getName());
                return file.toPath();
            }

            if (file.isDirectory())
            {
                System.out.println("dir : " + file.getName());
                return getFirstPropertiesFileInDir(file.toPath());
            }
        }
        return null;
    }

    //public static int ICON_INDEX = 0;
}
