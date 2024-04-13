# How create Controls for your pack

## Introduction

First thing first, create a file named `ctm_selector.json` in the root folder of your resource pack.

Its content is a list of `Controls`, which is composed as follows:

```json
[
  {
    "type": "ctm",
    "group_name": "Logs",
    "properties_files": [
      "minecraft/optifine/ctm/connect/organics/"
    ],
    "icon_path": "minecraft:optifine/ctm/connect/organics/oak/0.png",
    "enabled": true,
    "button_tooltip": ""
  },
  {
    "type": "ctm",
    "group_name": "Polished Stones",
    "properties_files": [
      "minecraft/optifine/ctm/connect/stones/polished_stones/polished_andesite.properties",
      "minecraft/optifine/ctm/connect/stones/polished_stones/polished_blackstone.properties",
      "minecraft/optifine/ctm/connect/stones/polished_stones/polished_deepslate.properties",
      "minecraft/optifine/ctm/connect/stones/polished_stones/polished_diorite.properties",
      "minecraft/optifine/ctm/connect/stones/polished_stones/polished_granite.properties"
    ],
    "icon_path": "minecraft:optifine/ctm/connect/stones/polished_stones/0.png",
    "enabled": true,
    "button_tooltip": ""
  }
]
```

## Fields

### Type

The `type` field contains the type of modification that will be enabled or disabled by the mod.

As of now, it can only be `ctm`, but I plan to add more in the future.

### Group Name

The `group_name` field is the name that will be displayed in the Controls screen.

For example with the above configuration, we would have:

![group_name_ex](https://github.com/Aeldit/Aeldit/blob/main/ctm_selector/group_name_ex.png?raw=true)

### Properties Fields

The `properties_files` field is an array of string, and each of these string is the path to either a directory
containing all the blocks you want to include in your group, or the path to each individual `.properties` file.

Its paths must start at the identifier (here it is `minecraft`, but if you have modded
textures it could be `create` for example, so `create/optifine/ctm/connect/...`).

### Icon Path

The `icon_path` field is very similar but there is one major difference: the identifier (here `minecraft`) MUST be
followed by a `:` instead of a `/`. If you don't do this, the game will most likely crash.

It must point to a `.png` file

### Enabled (optional)

The `enabled` defines whether the group will be disabled at first, but it can be modified by the user so this is just a
default value, and it can be omitted

### Button Tooltip (optional)

The `button_tooltip` field contains the tooltip that will be rendered when hovering the ON/OFF button in the controls
screen
