/*
 * Copyright (c) 2023-2024  -  Made by Aeldit
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
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CTMSelector
{
    //=========================================================================
    // Static part
    //=========================================================================
    public static boolean hasFolderPackControls(Path packPath)
    {
        return Files.exists(Path.of(packPath + "/ctm_selector.json"));
    }

    public static boolean hasZipPackControls(String packPath)
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
    private final String ctmSelectorJsonFilePath;
    private final String packName;
    private final boolean isFolder;

    // Map<Controls, Map<Properties file, blocksNames>>
    private final Map<String, Map<String, ArrayList<String>>> blocksInControlsMap = new HashMap<>();

    public CTMSelector(String packName, boolean isFolder)
    {
        if (isFolder)
        {
            this.ctmSelectorJsonFilePath = FabricLoader.getInstance().getGameDir().resolve("resourcepacks") + "/" + packName + "/ctm_selector.json";
        }
        else
        {
            this.ctmSelectorJsonFilePath = "/ctm_selector.json";
        }
        this.packName = packName;
        this.isFolder = isFolder;

        readFile();
    }

    /**
     * Adds to the {@code blocksInControlsMap} all the blocks names contained by each Properties file found in each {@code Controls}
     */
    public void initBlocksInControlsMap()
    {
        for (Controls controls : packControls)
        {
            blocksInControlsMap.putIfAbsent(controls.getGroupName(), new HashMap<>());

            // Iterates over the properties files contained by the current controls object
            for (Path path1 : controls.getPropertiesFilesPaths())
            {
                blocksInControlsMap
                        .get(controls.getGroupName())
                        .put(path1.toString(), getCTMBlocksNamesInProperties(path1));
            }
        }
    }

    public List<Controls> getPackControls()
    {
        return packControls;
    }

    /**
     * @param blockName The name of the block
     * @return The controls group that contains the block | null otherwise
     */
    public Controls getControlsGroupWithBlock(String blockName)
    {
        for (Controls controls : packControls)
        {
            Map<String, ArrayList<String>> currentControls = blocksInControlsMap.getOrDefault(controls.getGroupName(), null);
            if (currentControls == null)
            {
                continue;
            }

            for (Path path1 : controls.getPropertiesFilesPaths())
            {
                if (currentControls.get(path1.toString()).contains(blockName))
                {
                    return controls;
                }
            }
        }
        return null;
    }

    private @NotNull ArrayList<String> getCTMBlocksNamesInProperties(Path pathArg)
    {
        Properties properties = new Properties();
        try
        {
            properties.load(new FileInputStream(String.valueOf(pathArg)));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        if (properties.isEmpty())
        {
            return new ArrayList<>();
        }

        ArrayList<String> ctmBlocks = new ArrayList<>();

        if (properties.containsKey("matchBlocks"))
        {
            ctmBlocks.addAll(Arrays.asList(properties.getProperty("matchBlocks").split(" ")));
        }
        else if (properties.containsKey("matchTiles"))
        {
            ctmBlocks.addAll(Arrays.asList(properties.getProperty("matchTiles").split(" ")));
        }

        if (properties.containsKey("ctmDisabled"))
        {
            ctmBlocks.addAll(Arrays.asList(properties.getProperty("ctmDisabled").split(" ")));
        }
        else if (properties.containsKey("ctmTilesDisabled"))
        {
            ctmBlocks.addAll(Arrays.asList(properties.getProperty("ctmTilesDisabled").split(" ")));
        }
        return ctmBlocks;
    }

    public void readFile()
    {
        ArrayList<Controls.SerializableControls> serializableControls = new ArrayList<>();
        String packPathString = FabricLoader.getInstance().getGameDir().resolve("resourcepacks") + "/" + packName;

        if (isFolder)
        {
            Path ctmSelectorPath = Path.of(packPathString + "/ctm_selector.json");

            if (Files.exists(ctmSelectorPath))
            {
                try
                {
                    Gson gson = new Gson();
                    Reader reader = Files.newBufferedReader(ctmSelectorPath);
                    serializableControls.addAll(Arrays.asList(gson.fromJson(reader, Controls.SerializableControls[].class)));
                    reader.close();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        else
        {
            try (ZipFile zipFile = new ZipFile(packPathString))
            {
                for (FileHeader fileHeader : zipFile.getFileHeaders())
                {
                    if (fileHeader.toString().endsWith(".json"))
                    {
                        Gson gson = new Gson();
                        Reader reader = new InputStreamReader(zipFile.getInputStream(fileHeader));
                        serializableControls.addAll(Arrays.asList(gson.fromJson(reader, Controls.SerializableControls[].class)));
                        reader.close();
                        break;
                    }
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        // Adds the controls properly initialized to the packControls array
        for (Controls.SerializableControls cr : serializableControls)
        {
            packControls.add(new Controls(
                            cr.type(), cr.groupName(), cr.buttonTooltip(),
                            cr.propertiesFilesPaths(), cr.screenTexture(), cr.isEnabled(),
                            Path.of(FabricLoader.getInstance().getGameDir().resolve("resourcepacks") + "/" + packName)
                    )
            );
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
        packControls.forEach(controls -> controls.setEnabled(true));
    }

    public void restoreUnsavedOptions()
    {
        for (Controls controls : unsavedOptions)
        {
            packControls.get(packControls.indexOf(controls)).toggle();
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

    /**
     * Updates the 'enabled' field in the controls file
     */
    public void updateControlsStates()
    {
        ArrayList<Controls.SerializableControls> serializableControlsToWrite = new ArrayList<>();

        for (Controls cr : packControls)
        {
            cr.getContainedBLocksList().forEach(ctmBlock -> ctmBlock.setEnabled(cr.isEnabled()));
            serializableControlsToWrite.add(cr.getAsRecord());
        }

        String packPathString = FabricLoader.getInstance().getGameDir().resolve("resourcepacks") + "/" + packName;
        Path ctmSelectorPath = Path.of(packPathString + "/ctm_selector.json");

        if (isFolder)
        {
            try
            {
                Gson gsonWriter = new GsonBuilder().setPrettyPrinting().create();
                Writer writer = Files.newBufferedWriter(ctmSelectorPath);
                gsonWriter.toJson(serializableControlsToWrite, writer);
                writer.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            // TODO -> Implement with Zip
        }
    }
}
