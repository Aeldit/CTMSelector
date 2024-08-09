package fr.aeldit.ctms.textures.entryTypes;

import fr.aeldit.ctms.textures.CTMSelector;
import fr.aeldit.ctms.textures.Group;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

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
 * {@link #namespacesBlocks} contains for each namespace an ArrayList containing all {@link CTMBlock}
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
    // HashMap<namespace, blocks in the namespace>
    private final HashMap<String, ArrayList<CTMBlock>> namespacesBlocks;

    public CTMPack(@NotNull String name, boolean isFolder, boolean hasSelectorFile, boolean isModded)
    {
        this.name = name;
        this.isFolder = isFolder;

        this.ctmSelector = new CTMSelector(this.name, isFolder, hasSelectorFile);

        // We either use only the vanilla array, or the hashmap
        this.vanillaOnlyCtmBlocks = isModded ? null : new ArrayList<>();
        this.namespacesBlocks = isModded ? new HashMap<>() : null;
    }

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

    public ArrayList<CTMBlock> getAllCTMBlocks()
    {
        if (vanillaOnlyCtmBlocks != null)
        {
            return vanillaOnlyCtmBlocks;
        }

        ArrayList<CTMBlock> blocks = new ArrayList<>();

        for (ArrayList<CTMBlock> ctmBlocks : namespacesBlocks.values())
        {
            blocks.addAll(ctmBlocks);
        }
        return blocks;
    }

    public ArrayList<CTMBlock> getCTMBlocksForNamespace(String namespace)
    {
        return namespacesBlocks.containsKey(namespace) ? namespacesBlocks.get(namespace) : new ArrayList<>(0);
    }

    public @Nullable CTMBlock getCTMBlockByName(String name)
    {
        if (vanillaOnlyCtmBlocks != null)
        {
            for (CTMBlock ctmBlock : vanillaOnlyCtmBlocks)
            {
                if (ctmBlock.getBlockName().equals(name))
                {
                    return ctmBlock;
                }
            }
        }
        else
        {
            for (ArrayList<CTMBlock> ctmBlocks : namespacesBlocks.values())
            {
                for (CTMBlock ctmBlock : ctmBlocks)
                {
                    if (ctmBlock.getBlockName().equals(name))
                    {
                        return ctmBlock;
                    }
                }
            }
        }
        return null;
    }

    public void addAllBlocks(@NotNull ArrayList<CTMBlock> ctmBlockList, String namespace)
    {
        if (vanillaOnlyCtmBlocks != null)
        {
            vanillaOnlyCtmBlocks.addAll(ctmBlockList);
        }
        else
        {
            if (!namespacesBlocks.containsKey(namespace))
            {
                namespacesBlocks.put(namespace, new ArrayList<>(ctmBlockList.size()));
            }

            namespacesBlocks.get(namespace).addAll(ctmBlockList);
        }
    }

    public ArrayList<String> getNamespaces()
    {
        return new ArrayList<>(namespacesBlocks.keySet());
    }

    //=========================================================================
    // Selectors
    //=========================================================================
    public CTMSelector getCtmSelector()
    {
        return ctmSelector;
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
            for (CTMBlock ctmBlock : vanillaOnlyCtmBlocks)
            {
                if (!isBlockDisabledFromGroup(ctmBlock))
                {
                    ctmBlock.setEnabled(true);
                }
            }
        }
        else
        {
            for (ArrayList<CTMBlock> ctmBlocks : namespacesBlocks.values())
            {
                for (CTMBlock ctmBlock : ctmBlocks)
                {
                    if (!isBlockDisabledFromGroup(ctmBlock))
                    {
                        ctmBlock.setEnabled(true);
                    }
                }
            }
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