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
 * in the repo of this mod (https://github.com/Aeldit/Cyan)
 */

package fr.aeldit.ctms.textures;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CTMSelector
{
    private final List<Controls> controls = new ArrayList<>();
    private final Path path;

    public CTMSelector(String packName)
    {
        this.path = Path.of(FabricLoader.getInstance().getGameDir().resolve("resourcepacks") + "\\" + packName + "\\ctm_selector.json");

        readFile();
    }

    public static boolean isPackEligible(Path packPath)
    {
        return Files.exists(Path.of(packPath + "\\ctm_selector.json"));
    }

    public List<Controls> getControls()
    {
        return controls;
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
}
