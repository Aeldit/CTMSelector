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

package fr.aeldit.ctms.gui.entryTypes;

import com.google.common.hash.Hashing;
import fr.aeldit.ctms.textures.CTMSelector;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static fr.aeldit.ctms.util.Utils.RESOURCE_PACKS_DIR;

/**
 * Represents a a CTM pack
 *
 * @apiNote {@link #name} holds the name of the associated resource pack
 *         <ul>
 *              <li>if it is a zip file : {@code "MyPackName.zip"}</li>
 *              <li>if it is a folder : {@code "MyPackName"}</li>
 *         </ul>
 *         <p>
 *         {@link #isFolder} holds whether the pack is a folder or a zip file
 *         <p>
 *         {@link #ctmSelector} holds the associated {@link CTMSelector} object
 *         <p>
 *         {@link #identifier} holds the identifier (the texture) that is
 *         displayed next to the pack name in the
 *         {@link fr.aeldit.ctms.gui.CTMSScreen CTMSScreen}
 *         <p>
 *         The {@link #ctmBlocks} ArrayList contains a {@link CTMBlock} object
 *         of each block with CTM properties found in the pack
 *         <p>
 *         The {@link #unsavedOptions} ArrayList contains a {@link CTMBlock}
 *         object of each changed options
 *         <p>
 *         The second part contains methods to handle the activation /
 *         deactivation of each {@code CTMBlock} in this pack
 */
public class CTMPack
{
    private final String name;
    private final boolean isFolder;
    private CTMSelector ctmSelector;
    private Identifier identifier;

    private final List<CTMBlock> ctmBlocks = new ArrayList<>();
    private final List<CTMBlock> unsavedOptions = new ArrayList<>();

    public CTMPack(@NotNull String name, boolean isFolder)
    {
        this.name = name;
        this.isFolder = isFolder;

        //String var10003 = Util.replaceInvalidChars("file/" + name, Identifier::isPathCharacterValid);
        //this.identifier = new Identifier("minecraft", "pack/" + var10003 + "/" + Hashing.sha1().hashUnencodedChars("file/" + name) + "/icon");
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

    public CTMSelector getCtmSelector()
    {
        return ctmSelector;
    }

    public void createCtmSelector(boolean isFolder)
    {
        this.ctmSelector = new CTMSelector(this.name, isFolder);
    }

    public Identifier getIdentifier()
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
                id = new Identifier("minecraft", "pack/" + var10003 + "/" + Hashing.sha1().hashUnencodedChars("file/" + name) + "/icon");

                /*try (InputStream stream = new FileInputStream(packPath + "/pack.png"))
                {
                    MinecraftClient.getInstance().getTextureManager().registerTexture(id, new NativeImageBackedTexture(NativeImage.read(stream)));
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }*/
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
            }*/
        }
        this.identifier = id;
    }

    //=========================================================================
    // CTMBlocks
    //=========================================================================
    public void addBlock(CTMBlock ctmBlock)
    {
        ctmBlocks.add(ctmBlock);
    }

    public void addAllBlocks(@NotNull List<CTMBlock> ctmBlockList)
    {
        ctmBlockList.forEach(this::addBlock);
    }

    public List<CTMBlock> getCtmBlocks()
    {
        return ctmBlocks;
    }

    public void toggle(CTMBlock block)
    {
        if (ctmBlocks.contains(block))
        {
            ctmBlocks.get(ctmBlocks.indexOf(block)).toggle();
        }

        if (unsavedOptions.contains(block))
        {
            unsavedOptions.remove(block);
        }
        else
        {
            unsavedOptions.add(block);
        }
    }

    public void resetOptions()
    {
        ctmBlocks.forEach(ctmBlock -> ctmBlock.setEnabled(true));
    }

    public void restoreUnsavedOptions()
    {
        for (CTMBlock block : unsavedOptions)
        {
            ctmBlocks.get(ctmBlocks.indexOf(block)).setEnabled(!ctmBlocks.contains(block));
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

    public boolean getOptionValue(String blockName)
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
