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
      "minecraft:optifine/ctm/connect/organics/"
    ],
    "icon_path": "minecraft:optifine/ctm/connect/organics/oak/0.png",
    "enabled": true,
    "button_tooltip": ""
  },
  {
    "type": "ctm",
    "group_name": "Polished Stones",
    "properties_files": [
      "minecraft:optifine/ctm/connect/stones/polished_stones/polished_andesite.properties",
      "minecraft:optifine/ctm/connect/stones/polished_stones/polished_blackstone.properties",
      "minecraft:optifine/ctm/connect/stones/polished_stones/polished_deepslate.properties",
      "minecraft:optifine/ctm/connect/stones/polished_stones/polished_diorite.properties",
      "minecraft:optifine/ctm/connect/stones/polished_stones/polished_granite.properties"
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

Each of these must contain the namespace followed by a `:`, followed by the path to the block from the namespace

Its paths must start at the identifier (here it is `minecraft`, but if you have modded
textures it could be `create` for example, so `create:optifine/ctm/connect/...`).

### Icon Path

The path to the texture Identifier, it must point to a `.png` file

### Enabled (optional)

The `enabled` defines whether the group will be disabled at first, but it can be modified by the user so this is just a
default value, and it can be omitted

### Button Tooltip (optional)

The `button_tooltip` field contains the tooltip that will be rendered when hovering the ON/OFF button in the group
screen
