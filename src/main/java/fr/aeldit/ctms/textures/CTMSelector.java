package fr.aeldit.ctms.textures;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.aeldit.ctms.gui.entryTypes.CTMBlock;
import fr.aeldit.ctms.util.Utils;
import net.fabricmc.loader.api.FabricLoader;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    public static boolean hasZipPackControls(@NotNull ZipFile zipFile) throws ZipException
    {
        for (FileHeader fileHeader : zipFile.getFileHeaders())
        {
            if (fileHeader.toString().equals("ctm_selector.json"))
            {
                return true;
            }
        }
        return false;
    }

    public static byte @NotNull [] toByteArray(@NotNull ArrayList<Control.SerializableControls> controls)
    {
        ArrayList<String> s = new ArrayList<>();
        for (Control.SerializableControls sc : controls)
        {
            StringBuilder sbFiles = new StringBuilder();
            sbFiles.append("[\n");
            for (int i = 0; i < sc.propertiesFilesPaths().size(); ++i)
            {
                sbFiles.append(String.format("\t\t\t\"%s\"", sc.propertiesFilesPaths().get(i)));
                if (i != sc.propertiesFilesPaths().size() - 1)
                {
                    sbFiles.append(",\n");
                }
            }
            sbFiles.append("\n\t\t]");

            s.add(String.format("""
                            \t{
                            \t\t"type": "%s",
                            \t\t"group_name": "%s",
                            \t\t"properties_files": %s,
                            \t\t"icon_path": "%s",
                            \t\t"enabled": %b,
                            \t\t"button_tooltip": "%s"
                            \t}""", sc.type(), sc.groupName(), sbFiles, sc.iconPath(), sc.isEnabled(),
                    sc.buttonTooltip() == null ? "" : sc.buttonTooltip()
            ));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < s.size(); ++i)
        {
            sb.append(s.get(i));
            if (i != s.size() - 1)
            {
                sb.append(",\n");
            }
        }
        sb.append("\n]");
        return sb.toString().getBytes();
    }

    //=========================================================================
    // Non-static part
    //=========================================================================
    private final ArrayList<Control> packControls = new ArrayList<>();
    private final String packName;
    private final boolean isFolder;

    public CTMSelector(String packName, boolean isFolder)
    {
        this.packName = packName;
        this.isFolder = isFolder;

        readFile();
    }

    public ArrayList<Control> getControls()
    {
        return packControls;
    }

    /**
     * @param ctmBlock The {@link CTMBlock} object
     * @return The controls group that contains the block | null otherwise
     */
    public @Nullable Control getControlsGroupWithBlock(CTMBlock ctmBlock)
    {
        for (Control control : packControls)
        {
            for (CTMBlock block : control.getContainedBlocksList())
            {
                if (block == ctmBlock)
                {
                    return control;
                }
            }
        }
        return null;
    }

    public static @NotNull ArrayList<String> getCTMBlocksNamesInProperties(Path path)
    {
        Properties properties = new Properties();
        try
        {
            properties.load(new FileInputStream(String.valueOf(path)));
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

    public static @NotNull ArrayList<String> getCTMBlocksNamesInZipProperties(
            FileHeader fileHeader, @NotNull ZipFile zipFile
    )
    {
        Properties properties = new Properties();
        try
        {
            properties.load(zipFile.getInputStream(fileHeader));
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
        ArrayList<Control.SerializableControls> serializableControls = new ArrayList<>();
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
                            Control.SerializableControls[].class
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
                                Control.SerializableControls[].class
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
        for (Control.SerializableControls cr : serializableControls)
        {
            packControls.add(new Control(
                            cr.type(), cr.groupName(), cr.buttonTooltip(),
                            cr.propertiesFilesPaths(), cr.iconPath(), cr.isEnabled(),
                            Path.of(FabricLoader.getInstance().getGameDir().resolve("resourcepacks") + "/" + packName),
                            isFolder, isFolder ? null : packPathString
                    )
            );
        }
    }

    //=========================================================================
    // Options handling
    //=========================================================================
    public void toggle(Control control)
    {
        if (packControls.contains(control))
        {
            control.toggle();
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
        ArrayList<Control.SerializableControls> serializableControlsToWrite = new ArrayList<>();

        for (Control cr : packControls)
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
            HashMap<String, byte[]> h = new HashMap<>(1);
            byte[] b = toByteArray(serializableControlsToWrite);
            h.put("ctm_selector.json", b);
            Utils.writeBytesToZip(Path.of(FabricLoader.getInstance().getGameDir().resolve("resourcepacks") + "/" + packName).toString(), h);
        }
    }
}
