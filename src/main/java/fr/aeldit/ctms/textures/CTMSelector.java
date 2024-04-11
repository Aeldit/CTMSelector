package fr.aeldit.ctms.textures;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.aeldit.ctms.gui.entryTypes.CTMBlock;
import net.fabricmc.loader.api.FabricLoader;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

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
    private final ArrayList<Controls> packControls = new ArrayList<>();
    private final String packName;
    private final boolean isFolder;

    public CTMSelector(String packName, boolean isFolder)
    {
        this.packName = packName;
        this.isFolder = isFolder;

        readFile();
    }

    public ArrayList<Controls> getControls()
    {
        return packControls;
    }

    /**
     * @param ctmBlock The {@link CTMBlock} object
     * @return The controls group that contains the block | null otherwise
     */
    public @Nullable Controls getControlsGroupWithBlock(CTMBlock ctmBlock)
    {
        for (Controls controls : packControls)
        {
            for (CTMBlock block : controls.getContainedBlocksList())
            {
                if (block == ctmBlock)
                {
                    return controls;
                }
            }
        }
        return null;
    }

    public static @NotNull ArrayList<String> getCTMBlocksNamesInProperties(Path pathArg)
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
                    serializableControls.addAll(Arrays.asList(gson.fromJson(reader,
                            Controls.SerializableControls[].class
                    )));
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
                    if (fileHeader.toString().endsWith("ctm_selector.json"))
                    {
                        Gson gson = new Gson();
                        Reader reader = new InputStreamReader(zipFile.getInputStream(fileHeader));
                        serializableControls.addAll(Arrays.asList(gson.fromJson(reader,
                                Controls.SerializableControls[].class
                        )));
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
                            cr.propertiesFilesPaths(), cr.icon(), cr.isEnabled(),
                            Path.of(FabricLoader.getInstance().getGameDir().resolve("resourcepacks") + "/" + packName)
                    )
            );
        }
    }

    //=========================================================================
    // Options handling
    //=========================================================================
    public void toggle(Controls controls)
    {
        if (packControls.contains(controls))
        {
            controls.toggle();
        }
    }

    public void resetOptions()
    {
        packControls.forEach(controls -> controls.setEnabled(true));
    }

    /**
     * Updates the 'enabled' field in the controls file
     */
    public void updateControlsStates()
    {
        ArrayList<Controls.SerializableControls> serializableControlsToWrite = new ArrayList<>();

        for (Controls cr : packControls)
        {
            cr.getContainedBlocksList().forEach(ctmBlock -> ctmBlock.setEnabled(cr.isEnabled()));
            serializableControlsToWrite.add(cr.getAsRecord());
        }

        Path ctmSelectorPath =
                Path.of(FabricLoader.getInstance().getGameDir().resolve("resourcepacks") + "/" + packName +
                        "/ctm_selector.json");

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
