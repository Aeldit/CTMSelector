/*
 * Copyright (c) 2023  -  Made by Aeldit
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
 * in the repo of this mod (https://github.com/Aeldit/Cyan)
 */

package fr.aeldit.ctms.util;

import fr.aeldit.ctms.textures.Textures;
import fr.aeldit.cyanlib.lib.CyanLib;
import fr.aeldit.cyanlib.lib.commands.CyanLibConfigCommands;
import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Utils
{
    public static final String CTMS_MODID = "ctms";
    public static final Logger CTMS_LOGGER = LoggerFactory.getLogger(CTMS_MODID);

    public static final Textures TEXTURES = new Textures();

    public static CyanLibOptionsStorage CTMS_OPTIONS_STORAGE = new CyanLibOptionsStorage(CTMS_MODID, TEXTURES.getTextureOptions(), new ArrayList<>());
    public static CyanLib CTMS_LIB_UTILS = new CyanLib(CTMS_MODID, CTMS_OPTIONS_STORAGE);
    public static CyanLibConfigCommands CTMS_CONFIG_COMMANDS = new CyanLibConfigCommands(CTMS_MODID, CTMS_LIB_UTILS);
}
