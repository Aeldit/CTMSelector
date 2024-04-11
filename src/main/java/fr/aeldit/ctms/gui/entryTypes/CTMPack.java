package fr.aeldit.ctms.gui.entryTypes;

import fr.aeldit.ctms.textures.CTMSelector;
import fr.aeldit.ctms.textures.Controls;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

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
 * The {@link #ctmBlocks} ArrayList contains a {@link CTMBlock} object
 * of each block with CTM properties found in the pack
 * <p>
 * The second part contains methods to handle the activation /
 * deactivation of each {@code CTMBlock} in this pack
 */
public class CTMPack
{
    private final String name;
    private final boolean isFolder;
    private final CTMSelector ctmSelector;
    //private Identifier identifier;
    private final ArrayList<CTMBlock> ctmBlocks = new ArrayList<>();

    public CTMPack(@NotNull String name, boolean isFolder, boolean hasSelector)
    {
        this.name = name;
        this.isFolder = isFolder;

        this.ctmSelector = hasSelector ? new CTMSelector(this.name, isFolder) : null;

        /*String var10003 = Util.replaceInvalidChars("file/" + name, Identifier::isPathCharacterValid);
        this.identifier = new Identifier("minecraft", "pack/" + var10003 + "/" + Hashing.sha1().hashUnencodedChars
        ("file/" + name) + "/icon");*/
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

    //=========================================================================
    // Controls
    //=========================================================================
    public boolean isBlockDisabledFromGroup(CTMBlock ctmBlock)
    {
        Controls controls = ctmSelector.getControlsGroupWithBlock(ctmBlock);
        return controls != null && !controls.isEnabled();
    }

    /*public Identifier getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(int icon_index)
    {
        String packPath = RESOURCE_PACKS_DIR + "/" + name;
        Identifier id = new Identifier("ctms", String.valueOf(icon_index));

        if (isFolder)
        {
            if (Files.exists(Path.of(packPath + "/pack.png")))
            {
                String var10003 = Util.replaceInvalidChars("file/" + name, Identifier::isPathCharacterValid);
                id = new Identifier("minecraft",
                        "pack/" + var10003 + "/" + Hashing.sha1().hashUnencodedChars("file/" + name) + "/icon"
                );

                /*try (InputStream stream = new FileInputStream(packPath + "/pack.png"))
                {
                    MinecraftClient.getInstance().getTextureManager().registerTexture(id, new
                    NativeImageBackedTexture(NativeImage.read(stream)));
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        else
        {
            /*try (ZipFile zipFile = new ZipFile(packPath))
            {
                for (FileHeader fileHeader : zipFile.getFileHeaders())
                {
                    if (fileHeader.toString().equals("pack.png"))
                    {
                        MinecraftClient.getInstance().getTextureManager().registerTexture(id,
                                new NativeImageBackedTexture(NativeImage.read(zipFile.getInputStream(fileHeader)))
                        );
                        break;
                    }
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        this.identifier = id;
    }*/

    //=========================================================================
    // CTMBlocks
    //=========================================================================
    public void addAllBlocks(@NotNull ArrayList<CTMBlock> ctmBlockList)
    {
        ctmBlocks.addAll(ctmBlockList);
    }

    public ArrayList<CTMBlock> getCtmBlocks()
    {
        return ctmBlocks;
    }

    public @Nullable CTMBlock getCtmBlockByName(String name)
    {
        for (CTMBlock ctmBlock : ctmBlocks)
        {
            if (ctmBlock.getBlockName().equals(name))
            {
                return ctmBlock;
            }
        }
        return null;
    }

    public void toggle(CTMBlock block)
    {
        if (ctmBlocks.contains(block))
        {
            ctmBlocks.get(ctmBlocks.indexOf(block)).toggle();
        }
    }

    public void resetOptions()
    {
        ctmBlocks.forEach(ctmBlock -> ctmBlock.setEnabled(true));
    }

    public boolean isBlockEnabled(String blockName)
    {
        for (CTMBlock block : ctmBlocks)
        {
            if (block.getBlockName().equals(blockName))
            {
                return block.isEnabled();
            }
        }
        return false;
    }
}
