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

import fr.aeldit.ctms.config.CTMSOptionsStorage;
import fr.aeldit.ctms.textures.TexturesHandling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils
{
    public static final String CTMS_MODID = "ctms";
    public static final Logger CTMS_LOGGER = LoggerFactory.getLogger(CTMS_MODID);

    public static final TexturesHandling TEXTURES_HANDLING = new TexturesHandling();

    public static final CTMSOptionsStorage CTMS_OPTIONS_STORAGE = new CTMSOptionsStorage();
}