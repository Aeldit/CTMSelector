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

package fr.aeldit.ctms.util;

import fr.aeldit.ctms.textures.CTMPacks;
import fr.aeldit.ctms.textures.FilesHandling;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Utils
{
    public static final String CTMS_MODID = "ctms";
    public static final Logger CTMS_LOGGER = LoggerFactory.getLogger(CTMS_MODID);

    public static final FilesHandling TEXTURES_HANDLING = new FilesHandling();
    public static CTMPacks CTM_PACKS;

    public static final Path RESOURCE_PACKS_DIR = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");

    public static int ICON_INDEX = 0;
}
