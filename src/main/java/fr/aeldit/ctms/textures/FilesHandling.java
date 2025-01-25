package fr.aeldit.ctms.textures;

import fr.aeldit.ctms.textures.entryTypes.CTMPack;

import java.io.File;
import java.nio.file.Files;

import static fr.aeldit.ctms.Utils.CTM_PACKS;
import static fr.aeldit.ctms.Utils.RESOURCE_PACKS_DIR;

public class FilesHandling
{
    private final String ctmPath = "optifine/ctm/";
    private final String[] types = {"matchBlocks", "matchTiles", "ctmDisabled", "ctmTilesDisabled"};

    public void load()
    {
        CTM_PACKS = new CTMPacks();

        if (!Files.exists(RESOURCE_PACKS_DIR))
        {
            return;
        }

        File[] files = RESOURCE_PACKS_DIR.toFile().listFiles();
        if (files == null)
        {
            return;
        }

        for (File file : files)
        {
            if (file.isDirectory())
            {
                CTMPack ctmPack = new CTMPack(file);
            }
            else if (file.isFile() && file.getName().endsWith(".zip"))
            {
            }
        }
    }
}