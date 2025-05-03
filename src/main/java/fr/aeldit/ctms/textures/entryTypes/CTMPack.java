package fr.aeldit.ctms.textures.entryTypes;

import fr.aeldit.ctms.textures.CTMSelector;
import fr.aeldit.ctms.textures.Group;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
    public final boolean isFolder;
    public final CTMSelector ctmSelector;
    private final List<CTMBlock> vanillaOnlyCtmBlocks;
    private final Map<String, List<CTMBlock>> namespaceBlocks;

    //******************************************************************************************************************
    //**                                                 CONSTRUCTION                                                 **
    //******************************************************************************************************************
    public CTMPack(@NotNull File file, @NotNull Map<String, List<CTMBlock>> namespacesBLocks)
    {
        this.name     = file.getName();
        this.isFolder = true;

        boolean isVanilla = namespacesBLocks.size() == 1 && namespacesBLocks.containsKey("minecraft");
        this.vanillaOnlyCtmBlocks = isVanilla ? new ArrayList<>(namespacesBLocks.get("minecraft")) : null;
        this.namespaceBlocks      = isVanilla ? null : namespacesBLocks;

        this.ctmSelector = hasCTMSelector(file) ? new CTMSelector(this.name, this) : null;
    }

    public CTMPack(@NotNull ZipFile zipFile, @NotNull Map<String, List<CTMBlock>> namespacesBLocks)
    {
        this.name     = zipFile.getName().split("/")[zipFile.getName().split("/").length - 1];
        this.isFolder = false;

        boolean isVanilla = namespacesBLocks.size() == 1 && namespacesBLocks.containsKey("minecraft");
        this.vanillaOnlyCtmBlocks = isVanilla ? new ArrayList<>(namespacesBLocks.get("minecraft")) : null;
        this.namespaceBlocks      = isVanilla ? null : namespacesBLocks;

        this.ctmSelector = hasCTMSelector(zipFile) ? new CTMSelector(this.name, zipFile, this) : null;
    }

    private boolean hasCTMSelector(File file)
    {
        return Files.exists(Path.of("%s/ctm_selector.json".formatted(file)));
    }

    private boolean hasCTMSelector(@NotNull ZipFile zipFile)
    {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements())
        {
            if (entries.nextElement().getName().equals("ctm_selector.json"))
            {
                return true;
            }
        }
        return false;
    }

    //******************************************************************************************************************
    //**                                                GETTERS & SETTERS                                             **
    //******************************************************************************************************************
    public String getName()
    {
        return name;
    }

    public Text getNameAsText()
    {
        return isFolder ? Text.of(name + " (folder)") : Text.of(name);
    }

    public List<CTMBlock> getCTMBlocks()
    {
        return vanillaOnlyCtmBlocks != null
               ? vanillaOnlyCtmBlocks
               : namespaceBlocks.values().stream()
                                .flatMap(Collection::stream)
                                .toList();
    }

    public List<CTMBlock> getCTMBlocksForNamespace(String namespace)
    {
        return namespaceBlocks.containsKey(namespace) ? namespaceBlocks.get(namespace) : new ArrayList<>(0);
    }

    public ArrayList<String> getNamespaces()
    {
        return vanillaOnlyCtmBlocks == null ? new ArrayList<>(namespaceBlocks.keySet())
                                            : new ArrayList<>(List.of("minecraft"));
    }

    //=========================================================================
    // Selectors
    //=========================================================================
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

    public void resetNamespace(String namespace)
    {
        if (namespaceBlocks != null && namespaceBlocks.containsKey(namespace))
        {
            namespaceBlocks.get(namespace).forEach(ctmBlock -> ctmBlock.setEnabled(true));
        }
    }
}