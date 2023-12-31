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
import fr.aeldit.ctms.textures.controls.Controls;
import net.fabricmc.loader.api.FabricLoader;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CTMSelector
{
    //=========================================================================
    // Static part
    //=========================================================================
    public static boolean isFolderPackEligible(Path packPath)
    {
        return Files.exists(Path.of(packPath + "/ctm_selector.json"));
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

    //=========================================================================
    // Non-static part
    //=========================================================================
    private final List<Controls> packControls = new ArrayList<>();
    private final Path path;
    private final String packName;

    public CTMSelector(String packName)
    {
        this.path = Path.of(FabricLoader.getInstance().getGameDir().resolve("resourcepacks") + "/" + packName + "/ctm_selector.json");
        this.packName = packName;

        readFile();
    }

    public List<Controls> getPackControls()
    {
        return packControls;
    }

    public void readFile()
    {
        if (Files.exists(path))
        {
            ArrayList<Controls.ControlsRecord> controlsRecords = new ArrayList<>();
            try
            {
                Gson gson = new Gson();
                Reader reader = Files.newBufferedReader(path);
                controlsRecords.addAll(Arrays.asList(gson.fromJson(reader, Controls.ControlsRecord[].class)));
                reader.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            // Adds the controls properly initialized to the packControls array
            for (Controls.ControlsRecord cr : controlsRecords)
            {
                packControls.add(new Controls(cr.type(), cr.groupName(), cr.buttonTooltip(),
                        cr.propertiesFilesPaths(), cr.screenTexture(), cr.isEnabled(),
                        Path.of(FabricLoader.getInstance().getGameDir().resolve("resourcepacks") + "/" + packName))
                );
            }
        }
    }

    //=========================================================================
    // Options handling
    //=========================================================================
    private final List<Controls> unsavedOptions = new ArrayList<>();

    public void toggle(Controls controls)
    {
        if (packControls.contains(controls))
        {
            controls.toggle();
        }

        if (unsavedOptions.contains(controls))
        {
            unsavedOptions.remove(controls);
        }
        else
        {
            unsavedOptions.add(controls);
        }
    }

    public void resetOptions()
    {
        packControls.forEach(ctmBlock -> ctmBlock.setEnabled(true));
    }

    public void restoreUnsavedOptions()
    {
        for (Controls controls : unsavedOptions)
        {
            packControls.get(packControls.indexOf(controls)).setEnabled(!packControls.contains(controls));
        }
        unsavedOptions.clear();
    }

    public void clearUnsavedOptions()
    {
        unsavedOptions.clear();
    }

    public boolean optionsChanged()
    {
        return !unsavedOptions.isEmpty();
    }

    public boolean getOptionValue(String groupName)
    {
        for (Controls controls : packControls)
        {
            if (controls.getGroupName().equals(groupName))
            {
                return controls.isEnabled();
            }
        }
        return false;
    }
}
