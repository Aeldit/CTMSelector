/*
 * Copyright (c) 2023  -  Made by Aeldit
 *
 *              GNU LESSER GENERAL PUBLIC LICENSE
 *                  Version 3, 29 June 2007
 *
 *  Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 *
 *
 * This version of the GNU Lesser General Public License incorporates
 * the terms and conditions of version 3 of the GNU General Public
 * License, supplemented by the additional permissions listed in the LICENSE.txt file
 * in the repo of this mod (https://github.com/Aeldit/CTMSelector)
 */

package fr.aeldit.ctms.textures;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CTMSelector
{
    private static final Map<String, List<Controls>> packsControlsMap = new HashMap<>();
    private final List<Controls> controls = new ArrayList<>();
    private final Path path;

    public CTMSelector(String packName)
    {
        this.path = Path.of(FabricLoader.getInstance().getGameDir().resolve("resourcepacks") + "\\" + packName + "\\ctm_selector.json");

        readFile();
        packsControlsMap.put(packName, controls);
    }

    public static List<Controls> getControls(String packName)
    {
        return packsControlsMap.getOrDefault(packName, new ArrayList<>(0));
    }

    public void readFile()
    {
        if (Files.exists(path))
        {
            try
            {
                Gson gson = new Gson();
                Reader reader = Files.newBufferedReader(path);
                controls.addAll(Arrays.asList(gson.fromJson(reader, Controls[].class)));
                reader.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean isFolderPackEligible(Path packPath)
    {
        return Files.exists(Path.of(packPath + "\\ctm_selector.json"));
    }

    public static boolean isZipPackEligible(String packPath)
    {
        try (ZipFile tmpZipFile = new ZipFile(packPath))
        {
            for (FileHeader fileHeader : tmpZipFile.getFileHeaders())
            {
                if (fileHeader.toString().equals("ctm_selector.json"))
                {
                    return true;
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return false;
    }
}
