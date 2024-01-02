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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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
    private final Path ctmSelectorJsonFilePath;
    private final String packName;

    // Map<Controls, Map<Properties file, blocksNames>>
    private final Map<String, Map<String, ArrayList<String>>> blocksInControlsMap = new HashMap<>();

    public CTMSelector(String packName)
    {
        this.ctmSelectorJsonFilePath = Path.of(FabricLoader.getInstance().getGameDir().resolve("resourcepacks") + "/" + packName + "/ctm_selector.json");
        this.packName = packName;

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
     * @return An arraylist containing all the controls that contain the block | an empty ArrayList otherwise
     */
    public ArrayList<Controls> getControlsWithBlock(String blockName)
    {
        ArrayList<Controls> controlsArrayList = new ArrayList<>();

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
                    controlsArrayList.add(controls);
                }
            }
        }
        return controlsArrayList;
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
        if (Files.exists(ctmSelectorJsonFilePath))
        {
            ArrayList<Controls.ControlsRecord> controlsRecords = new ArrayList<>();
            try
            {
                Gson gson = new Gson();
                Reader reader = Files.newBufferedReader(ctmSelectorJsonFilePath);
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
                packControls.add(new Controls(
                                cr.type(), cr.groupName(), cr.buttonTooltip(),
                                cr.propertiesFilesPaths(), cr.screenTexture(), cr.isEnabled(), cr.priority(),
                                Path.of(FabricLoader.getInstance().getGameDir().resolve("resourcepacks") + "/" + packName)
                        )
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

    public void restoreUnsavedOptions() // TODO -> restore the enabled state of the controls
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

    public void updateControlsStates()
    {
        ArrayList<Controls.ControlsRecord> controlsRecordsToWrite = new ArrayList<>();

        for (Controls cr : packControls)
        {
            controlsRecordsToWrite.add(cr.getRecord());
        }

        if (ctmSelectorJsonFilePath.endsWith(".zip"))
        {
            // implement
        }
        else
        {
            try
            {
                Gson gsonWriter = new GsonBuilder().setPrettyPrinting().create();
                Writer writer = Files.newBufferedWriter(ctmSelectorJsonFilePath);
                gsonWriter.toJson(controlsRecordsToWrite, writer);
                writer.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
