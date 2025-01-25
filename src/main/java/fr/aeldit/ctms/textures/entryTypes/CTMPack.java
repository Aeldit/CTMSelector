package fr.aeldit.ctms.textures.entryTypes;

import fr.aeldit.ctms.textures.CTMSelector;
import fr.aeldit.ctms.textures.Group;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Represents a CTM pack
 *
 * @apiNote {@link #name} holds the name of the associated resource pack
 * <ul>
 *      <li>if it is a zip file : {@code "MyPackName.zip"}</li>
 *      <li>if it is a folder : {@code "MyPackName"}</li>
 * </ul>
 * <p>
 * {@link #isFolder} holds whether the pack is a folder or a zip file
 * <p>
 * {@link #ctmSelector} holds the associated {@link CTMSelector} object
 * <p>
 * identifier holds the identifier (the texture) that is
 * displayed next to the pack name in the
 * {@link fr.aeldit.ctms.gui.CTMSScreen CTMSScreen}
 * <p>
 * {@link #vanillaOnlyCtmBlocks} contains the blocks when we only have vanilla blocks. Otherwise, this is null
 * <p>
 * {@link #namespaceBlocks} contains for each namespace an ArrayList containing all {@link CTMBlock}
 * object found in the pack. If there are no modded blocks, this is null
 * <p>
 * The second part contains methods to handle the activation /
 * deactivation of each {@code CTMBlock} in this pack
 */
public class CTMPack
{
    private final String name;
    private final boolean isFolder;
    private final CTMSelector ctmSelector;
    private final ArrayList<CTMBlock> vanillaOnlyCtmBlocks;
    private final HashMap<String, ArrayList<CTMBlock>> namespaceBlocks;

    public CTMPack(@NotNull File file)
    {
        this.name     = file.getName();
        this.isFolder = true;

        this.ctmSelector = folderHasCTMSelector(file) ? new CTMSelector(this.name) : null;

        boolean isModded = isFolderModded(file);
        this.vanillaOnlyCtmBlocks = isModded ? null : new ArrayList<>();
        this.namespaceBlocks      = isModded ? new HashMap<>() : null;
    }

    public CTMPack(@NotNull ZipFile zipFile)
    {
        this.name     = zipFile.toString();
        this.isFolder = false;

        this.ctmSelector = zipHasCTMSelector(zipFile) ? new CTMSelector(this.name, zipFile) : null;

        boolean isModded = isZipModded(zipFile);
        this.vanillaOnlyCtmBlocks = isModded ? null : new ArrayList<>();
        this.namespaceBlocks      = isModded ? new HashMap<>() : null;
    }

    private boolean folderHasCTMSelector(File file)
    {
        return Files.exists(Path.of("%s/ctm_selector.json".formatted(file)));
    }

    private boolean isFolderModded(@NotNull File file)
    {
        File[] files = file.listFiles();
        if (files != null)
        {
            for (File checkModdedFile : files)
            {
                if (checkModdedFile.toString().endsWith("assets"))
                {
                    File[] assetsDirFiles = checkModdedFile.listFiles();
                    if (assetsDirFiles != null)
                    {
                        return Arrays.stream(assetsDirFiles).anyMatch(f -> !f.getName().equals("minecraft"));
                    }
                    break;
                }
            }
        }
        return false;
    }

    private boolean zipHasCTMSelector(@NotNull ZipFile zipFile)
    {
        try
        {
            return zipFile.getFileHeaders().stream().anyMatch(fh -> "ctm_selector.json".equals(fh.toString()));
        }
        catch (ZipException e)
        {
            throw new RuntimeException(e);
        }
    }

    private boolean isZipModded(@NotNull ZipFile zipFile)
    {
        try
        {
            return zipFile.getFileHeaders().stream()
                          // Gets only the fileHeaders with 2 '/', because these are the namespaces
                          .filter(fh -> fh.toString().chars().filter(c -> c == '/').count() == 2)
                          .anyMatch(fh -> !"assets/minecraft/".equals(fh.toString()));
        }
        catch (ZipException e)
        {
            throw new RuntimeException(e);
        }
    }

    /*******************************************************************************************************************
     **                                                GETTERS & SETTERS                                              **
     ******************************************************************************************************************/
    public String getName()
    {
        return name;
    }

    public Text getNameAsText()
    {
        return isFolder ? Text.of(name + " (folder)") : Text.of(name);
    }

    public boolean isFolder()
    {
        return isFolder;
    }

    public ArrayList<CTMBlock> getCTMBlocks()
    {
        return vanillaOnlyCtmBlocks != null
               ? vanillaOnlyCtmBlocks
               : namespaceBlocks.values().stream()
                                .flatMap(Collection::stream)
                                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<CTMBlock> getCTMBlocksForNamespace(String namespace)
    {
        return namespaceBlocks.containsKey(namespace) ? namespaceBlocks.get(namespace) : new ArrayList<>(0);
    }

    public @Nullable CTMBlock getCTMBlockByName(String name)
    {
        if (vanillaOnlyCtmBlocks != null)
        {
            return vanillaOnlyCtmBlocks.stream()
                                       .filter(ctmBlock -> ctmBlock.getBlockName().equals(name))
                                       .findFirst()
                                       .orElse(null);
        }
        else
        {
            return namespaceBlocks.values().stream()
                                  .flatMap(Collection::stream)
                                  .filter(ctmBlock -> ctmBlock.getBlockName().equals(name))
                                  .findFirst()
                                  .orElse(null);
        }
    }

    public void addAllBlocks(@NotNull ArrayList<CTMBlock> ctmBlockList, String namespace)
    {
        if (vanillaOnlyCtmBlocks != null)
        {
            vanillaOnlyCtmBlocks.addAll(ctmBlockList);
        }
        else
        {
            if (!namespaceBlocks.containsKey(namespace))
            {
                namespaceBlocks.put(namespace, new ArrayList<>(ctmBlockList.size()));
            }

            namespaceBlocks.get(namespace).addAll(ctmBlockList);
        }
    }

    public ArrayList<String> getNamespaces()
    {
        return new ArrayList<>(namespaceBlocks.keySet());
    }

    //=========================================================================
    // Selectors
    //=========================================================================
    public CTMSelector getCtmSelector()
    {
        return ctmSelector;
    }

    public boolean hasCtmSelector()
    {
        return ctmSelector != null;
    }

    public boolean isModded()
    {
        return vanillaOnlyCtmBlocks == null;
    }

    //=========================================================================
    // Group
    //=========================================================================
    public boolean isBlockDisabledFromGroup(CTMBlock ctmBlock)
    {
        if (ctmSelector == null)
        {
            return false;
        }

        Group group = ctmSelector.getGroupWithBlock(ctmBlock);
        return group != null && !group.isEnabled();
    }

    //=========================================================================
    // CTMBlocks
    //=========================================================================
    public void toggle(CTMBlock block)
    {
        if (!isBlockDisabledFromGroup(block))
        {
            block.toggle();
        }
    }

    public void resetOptions()
    {
        if (vanillaOnlyCtmBlocks != null)
        {
            vanillaOnlyCtmBlocks.stream()
                                .filter(ctmBlock -> !isBlockDisabledFromGroup(ctmBlock))
                                .forEach(ctmBlock -> ctmBlock.setEnabled(true));
        }
        else
        {
            namespaceBlocks.values().stream()
                           .flatMap(Collection::stream)
                           .filter(ctmBlock -> !isBlockDisabledFromGroup(ctmBlock))
                           .forEach(ctmBlock -> ctmBlock.setEnabled(true));
        }
    }

    public boolean isBlockEnabled(String blockName)
    {
        CTMBlock ctmBlock = getCTMBlockByName(blockName);
        if (ctmBlock == null)
        {
            return true;
        }

        if (!isBlockDisabledFromGroup(ctmBlock))
        {
            return ctmBlock.isEnabled();
        }
        return false;
    }
}